package com.birthdates.punishments.punishment.impl

import com.birthdates.punishments.PunishmentsPlugin
import com.birthdates.punishments.database.DatabaseService
import com.birthdates.punishments.lang.LanguageService
import com.birthdates.punishments.lang.Placeholder
import com.birthdates.punishments.punishment.Punishment
import com.birthdates.punishments.punishment.PunishmentService
import com.birthdates.punishments.punishment.PunishmentType
import com.birthdates.punishments.util.Executors
import com.birthdates.punishments.util.Format
import com.birthdates.service.Services
import com.birthdates.service.register.Depends
import com.birthdates.service.register.Register
import org.bukkit.Bukkit
import org.bukkit.OfflinePlayer
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

@Register
@Depends([DatabaseService::class, LanguageService::class])
class CorePunishmentService : PunishmentService {
    val databaseService: DatabaseService = Services.get(DatabaseService::class.java)
    val languageService: LanguageService = Services.get(LanguageService::class.java)

    override fun addPunishment(playerName: String, punishment: Punishment) {
        Executors.IO.execute {
            addPunishment(Bukkit.getOfflinePlayer(playerName), punishment)
        }
    }

    override fun deactivatePunishment(revoker: CommandSender, playerName: String, punishmentType: PunishmentType) {
        Executors.IO.execute {
            val player = Bukkit.getOfflinePlayer(playerName)
            databaseService.deactivatePunishments(player.uniqueId, punishmentType)
            Bukkit.broadcastMessage(
                languageService.getMessage(
                    "punishment-removed-broadcast",
                    Placeholder("verb", "un${punishmentType.verb}"),
                    Placeholder("player", player.name),
                    Placeholder("revoker", revoker.name)
                )
            )
        }
    }

    private fun addPunishment(player: OfflinePlayer, punishment: Punishment) {
        val giver = punishment.giver?.let { Bukkit.getPlayer(it) } ?: Bukkit.getConsoleSender()
        if (!player.isOnline && punishment.type == PunishmentType.KICK) {
            giver.sendMessage(languageService.getMessage("player-not-online", Placeholder("player", player.name)))
            return
        }

        if (punishment.type == PunishmentType.BLACKLIST && player is Player) {
            punishment.address = player.address?.address?.hostAddress
        }

        if (player is Player && punishment.type != PunishmentType.MUTE) {
            Bukkit.getScheduler().runTask(PunishmentsPlugin.getInstance(), Runnable {
                player.kickPlayer(
                    languageService.getMessage(
                        "${punishment.type.name.lowercase()}-disconnected",
                        Placeholder("reason", punishment.reason),
                        Placeholder("duration", Format.formatDuration(punishment.duration))
                    )
                )
            })
        }

        Bukkit.broadcastMessage(
            languageService.getMessage(
                if (punishment.type == PunishmentType.KICK) "punishment-broadcast-kick" else "punishment-broadcast",
                Placeholder("verb", punishment.type.verb),
                Placeholder("player", player.name),
                Placeholder("reason", punishment.reason),
                Placeholder("giver", giver.name),
                Placeholder("duration", Format.formatDuration(punishment.duration))
            )
        )

        databaseService.addPunishment(player.uniqueId, punishment)
    }
}