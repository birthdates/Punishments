package com.birthdates.punishments.database.impl

import com.birthdates.punishments.PunishmentsPlugin
import com.birthdates.punishments.database.DatabaseService
import com.birthdates.punishments.punishment.Punishment
import com.birthdates.punishments.punishment.PunishmentType
import com.birthdates.punishments.util.Executors
import com.birthdates.service.register.Register
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.bukkit.Bukkit
import java.util.Collections
import java.util.UUID
import java.util.concurrent.CompletableFuture
import java.util.logging.Level

@Register
class CoreDatabaseService : DatabaseService {
    val dataSource: HikariDataSource
    val cachedActiveChatPunishments: MutableMap<UUID, Punishment> =
        Collections.synchronizedMap<UUID, Punishment>(mutableMapOf())
    val cachedChatPunishmentChecks: MutableSet<UUID> = Collections.synchronizedSet<UUID>(mutableSetOf())

    constructor() {
        val config = PunishmentsPlugin.getInstance().config
        val host = config.getString("database.host")
        val port = config.getInt("database.port")
        val database = config.getString("database.database")
        val username = config.getString("database.username")
        val password = config.getString("database.password")
        val jdbcUrl = String.format("jdbc:mysql://%s:%d/%s", host, port, database)
        val hikariConfig = HikariConfig()
        hikariConfig.jdbcUrl = jdbcUrl
        hikariConfig.username = username
        hikariConfig.password = password
        dataSource = HikariDataSource(hikariConfig)

        // Attempt to connect to the database
        dataSource.connection.use { }
        tryCreateTable()
    }

    override fun unload() {
        dataSource.close()
    }

    private fun tryCreateTable() {
        dataSource.connection.use { connection ->
            connection.prepareStatement(
                "CREATE TABLE IF NOT EXISTS punishments (" +
                        "id VARCHAR(36) NOT NULL," +
                        "punishmentType VARCHAR(16) NOT NULL," +
                        "reason TEXT NOT NULL," +
                        "duration BIGINT NOT NULL," +
                        "createdAt BIGINT NOT NULL," +
                        "giver VARCHAR(36) NULL," +
                        "active BOOLEAN DEFAULT TRUE," +
                        "address VARCHAR(15) NULL" +
                        ")"
            ).use { statement ->
                statement.execute()
            }
        }
    }

    private fun getPunishmentFromResultSet(resultSet: java.sql.ResultSet): Punishment {
        val type = PunishmentType.valueOf(resultSet.getString("punishmentType"))
        val reason = resultSet.getString("reason")
        val duration = resultSet.getLong("duration")
        val createdAt = resultSet.getLong("createdAt")
        val giver = resultSet.getString("giver").let { if (it == "null") null else UUID.fromString(it) }
        val active = resultSet.getBoolean("active")
        val address = resultSet.getString("address")
        return Punishment(type, reason, duration, giver, address, active, createdAt)
    }

    override fun deactivatePunishments(id: UUID, punishmentType: PunishmentType) {
        Executors.IO.execute {
            if (punishmentType.isChatPunishment()) {
                cachedActiveChatPunishments.remove(id)
                cachedChatPunishmentChecks -= id
            }
            dataSource.connection.use { connection ->
                connection.prepareStatement("UPDATE punishments SET active = false WHERE id = ? AND punishmentType = ?")
                    .use { statement ->
                        statement.setString(1, id.toString())
                        statement.setString(2, punishmentType.name)
                        statement.execute()
                    }
            }
        }
    }

    override fun getAllPunishments(id: UUID): CompletableFuture<List<Punishment>> {
        val future = CompletableFuture<List<Punishment>>()
        Executors.IO.execute {
            dataSource.connection.use { connection ->
                connection.prepareStatement("SELECT * FROM punishments WHERE id = ?").use { statement ->
                    statement.setString(1, id.toString())
                    statement.executeQuery().use { resultSet ->
                        val punishments = mutableListOf<Punishment>()
                        while (resultSet.next()) {
                            punishments.add(getPunishmentFromResultSet(resultSet))
                        }
                        future.complete(punishments)
                    }
                }
            }
        }

        return future
    }

    override fun addPunishment(id: UUID, punishment: Punishment) {
        Executors.IO.execute {
            if (punishment.type.isChatPunishment()) {
                cachedActiveChatPunishments[id] = punishment
                cachedChatPunishmentChecks -= id
            }
            dataSource.connection.use { connection ->
                connection.prepareStatement("INSERT INTO punishments (id, punishmentType, reason, duration, createdAt, giver, active, address) VALUES (?, ?, ?, ?, ?, ?, ?, ?)")
                    .use { statement ->
                        statement.setString(1, id.toString())
                        statement.setString(2, punishment.type.name)
                        statement.setString(3, punishment.reason)
                        statement.setLong(4, punishment.duration)
                        statement.setLong(5, punishment.createdAt)
                        statement.setString(6, punishment.giver.toString())
                        statement.setBoolean(7, punishment.active)
                        statement.setString(8, punishment.address)
                        statement.execute()
                    }
            }
        }
    }

    override fun getActivePunishment(address: String, punishmentType: PunishmentType): CompletableFuture<Punishment> {
        val future = CompletableFuture<Punishment>()
        Executors.IO.execute {
            dataSource.connection.use { connection ->
                connection.prepareStatement("SELECT * FROM punishments WHERE address = ? AND punishmentType = ? AND (createdAt + duration > ? OR duration = -1) AND active = true ORDER BY duration DESC LIMIT 1")
                    .use { statement ->
                        statement.setString(1, address)
                        statement.setString(2, punishmentType.name)
                        statement.setLong(3, System.currentTimeMillis())
                        statement.executeQuery().use { resultSet ->
                            if (resultSet.next()) {
                                future.complete(getPunishmentFromResultSet(resultSet))
                            } else {
                                future.complete(null)
                            }
                        }
                    }
            }
        }

        return future
    }

    override fun getActivePunishments(id: UUID, vararg types: PunishmentType): CompletableFuture<List<Punishment>> {
        val future = CompletableFuture<List<Punishment>>()
        val isChatPunishment = types.any { it.isChatPunishment() }
        if (isChatPunishment) {
            if (cachedChatPunishmentChecks.contains(id)) {
                future.complete(emptyList())
                return future
            }
            cachedActiveChatPunishments[id]?.let {
                // check if expired
                if (it.isNotExpired()) {
                    future.complete(listOf(it))
                    return future
                } else {
                    cachedActiveChatPunishments.remove(id)
                }
            }
        }

        Executors.IO.execute {
            dataSource.connection.use { connection ->
                connection.prepareStatement("SELECT * FROM punishments WHERE id = ? AND punishmentType IN (${types.joinToString { "?" }}) AND (createdAt + duration > ? OR duration = -1) AND active = true ORDER BY duration DESC LIMIT 1")
                    .use { statement ->
                        statement.setString(1, id.toString())
                        types.forEachIndexed { index, type ->
                            statement.setString(index + 2, type.name)
                        }
                        statement.setLong(types.size + 2, System.currentTimeMillis())
                        statement.executeQuery().use { resultSet ->
                            val set = mutableListOf<Punishment>()
                            while (resultSet.next()) {
                                val punishment = getPunishmentFromResultSet(resultSet)
                                if (punishment.type == PunishmentType.MUTE) {
                                    cachedActiveChatPunishments[id] = punishment
                                }
                                set.add(punishment)
                            }

                            if (isChatPunishment && set.isEmpty()) {
                                cachedChatPunishmentChecks += id
                            }
                            future.complete(set)
                        }
                    }
            }
        }

        return future
    }
}