package com.birthdates.punishments.command

import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandPermission
import co.aikar.commands.annotation.Default
import co.aikar.commands.annotation.Name
import com.birthdates.punishments.punishment.Punishment
import com.birthdates.punishments.punishment.PunishmentService
import com.birthdates.punishments.punishment.PunishmentType
import com.birthdates.service.Services
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

@CommandAlias("ban")
@CommandPermission("punishments.ban")
class BanCommand : BaseCommand() {
    @Default
    fun banPlayer(
        sender: CommandSender,
        @Name("player") targetName: String,
        @Name("reason") @Default("No reason provided.") reason: String
    ) {
        Services.get(PunishmentService::class.java).addPunishment(
            targetName,
            Punishment(PunishmentType.BAN, reason, -1, if (sender is Player) sender.uniqueId else null)
        )
    }
}