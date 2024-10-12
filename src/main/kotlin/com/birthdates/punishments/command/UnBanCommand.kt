package com.birthdates.punishments.command

import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandPermission
import co.aikar.commands.annotation.Default
import co.aikar.commands.annotation.Name
import com.birthdates.punishments.database.DatabaseService
import com.birthdates.punishments.lang.LanguageService
import com.birthdates.punishments.lang.Placeholder
import com.birthdates.punishments.punishment.PunishmentService
import com.birthdates.punishments.punishment.PunishmentType
import com.birthdates.punishments.util.Offline
import com.birthdates.service.Services
import org.bukkit.ChatColor
import org.bukkit.command.CommandSender

@CommandAlias("unban")
@CommandPermission("punishments.unban")
class UnBanCommand : BaseCommand() {
    val databaseService = Services.get(DatabaseService::class.java)
    val languageService = Services.get(LanguageService::class.java)

    @Default
    fun unbanPlayer(sender: CommandSender, @Name("player") targetName: String) {
        // check if player has a ban
        Offline.getPlayer(targetName).thenAccept { offlinePlayer ->
            databaseService.getActivePunishments(offlinePlayer.uniqueId, PunishmentType.BAN).thenAccept { punishments ->
                if (punishments.isEmpty()) {
                    sender.sendMessage("${ChatColor.RED}Player is not banned.")
                    return@thenAccept
                }

                Services.get(PunishmentService::class.java).deactivatePunishment(
                    sender,
                    targetName,
                    PunishmentType.BAN
                )
                sender.sendMessage(languageService.getMessage("unban-success", Placeholder("player", targetName)))
            }
        }
    }
}