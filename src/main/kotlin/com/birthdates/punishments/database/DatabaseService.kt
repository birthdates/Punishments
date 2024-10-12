package com.birthdates.punishments.database

import com.birthdates.punishments.punishment.Punishment
import com.birthdates.punishments.punishment.PunishmentType
import com.birthdates.service.Service
import java.util.UUID
import java.util.concurrent.CompletableFuture

/**
 * A service for managing the database.
 */
interface DatabaseService : Service {
    /**
     * Gets all punishments for a player.
     *
     * @param id The ID of the player to get the punishments for.
     * @return A future containing a list of punishments.
     */
    fun getAllPunishments(id: UUID): CompletableFuture<List<Punishment>>

    /**
     * Gets all active punishments for a player.
     *
     * @param id The ID of the player to get the active punishments for.
     * @param types The types of punishments to get.
     * @return A future containing a list of punishments.
     */
    fun getActivePunishments(id: UUID, vararg types: PunishmentType): CompletableFuture<List<Punishment>>

    /**
     * Adds a punishment for a player.
     *
     * @param id The ID of the player to add the punishment for.
     * @param punishment The punishment to add.
     */
    fun addPunishment(id: UUID, punishment: Punishment)

    /**
     * Deactivates all punishments of a certain type for a player.
     *
     * @param id The ID of the player to deactivate the punishments for.
     * @param punishmentType The type of punishment to deactivate.
     */
    fun deactivatePunishments(id: UUID, punishmentType: PunishmentType)

    /**
     * Gets the active punishment of a certain type for a player.
     *
     * @param address The address of the player to get the punishment for.
     * @param punishmentType The type of punishment to get.
     * @return A future containing the punishment.
     */
    fun getActivePunishment(address: String, punishmentType: PunishmentType): CompletableFuture<Punishment?>
}