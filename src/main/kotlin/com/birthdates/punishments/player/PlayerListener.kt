package com.birthdates.punishments.player

import com.birthdates.punishments.PunishmentsPlugin
import com.birthdates.punishments.database.DatabaseService
import com.birthdates.punishments.lang.LanguageService
import com.birthdates.punishments.lang.Placeholder
import com.birthdates.punishments.punishment.Punishment
import com.birthdates.punishments.punishment.PunishmentType
import com.birthdates.punishments.util.Format
import com.birthdates.service.Services
import io.papermc.paper.event.player.AsyncChatEvent
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.AsyncPlayerPreLoginEvent
import org.bukkit.event.player.PlayerCommandPreprocessEvent

class PlayerListener : Listener {
    val disallowedMuteCommands = PunishmentsPlugin.getInstance().config.getStringList("disallowed-mute-commands")
    val databaseService = Services.get(DatabaseService::class.java)
    val languageService = Services.get(LanguageService::class.java)

    @EventHandler
    fun preJoinEvent(event: AsyncPlayerPreLoginEvent) {
        val address = event.address.hostAddress
        val addressPunishment = databaseService.getActivePunishment(address, PunishmentType.BLACKLIST).get()
        if (addressPunishment != null) {
            denyJoin(event, addressPunishment)
            return
        }
        val punishments =
            databaseService.getActivePunishments(event.uniqueId, PunishmentType.BLACKLIST, PunishmentType.BAN).get()
        if (punishments.isNotEmpty()) {
            val punishment = punishments.minByOrNull { it.type.ordinal }!!
            denyJoin(event, punishment)
        }
    }

    fun denyJoin(event: AsyncPlayerPreLoginEvent, punishment: Punishment) {
        val message = languageService.getMessage(
            "${punishment.type.name.lowercase()}-disconnected",
            Placeholder("reason", punishment.reason),
            Placeholder("duration", Format.formatDuration(punishment.getTimeRemaining()))
        )
        event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, message)
    }

    @EventHandler
    fun onChat(event: AsyncChatEvent) {
        val punishments = databaseService.getActivePunishments(event.player.uniqueId, PunishmentType.MUTE).get()
        if (punishments.isEmpty()) {
            return
        }
        event.isCancelled = true
        event.player.sendMessage(
            languageService.getMessage(
                "muted",
                Placeholder("reason", punishments.first().reason),
                Placeholder("duration", Format.formatDuration(punishments.first().getTimeRemaining()))
            )
        )
    }

    @EventHandler
    fun preProcessCommand(event: PlayerCommandPreprocessEvent) {
        val command = event.message.split(" ")[0].removePrefix("/")
        if (command !in disallowedMuteCommands) {
            return
        }

        val punishments = databaseService.getActivePunishments(event.player.uniqueId, PunishmentType.MUTE).get()
        if (punishments.isEmpty()) {
            return
        }
        event.isCancelled = true
        event.player.sendMessage(
            languageService.getMessage(
                "muted-command-use",
                Placeholder("reason", punishments.first().reason),
                Placeholder("duration", Format.formatDuration(punishments.first().getTimeRemaining()))
            )
        )
    }
}