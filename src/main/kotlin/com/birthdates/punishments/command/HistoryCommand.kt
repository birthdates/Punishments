package com.birthdates.punishments.command

import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandPermission
import co.aikar.commands.annotation.Default
import co.aikar.commands.annotation.Name
import com.birthdates.punishments.PunishmentsPlugin
import com.birthdates.punishments.database.DatabaseService
import com.birthdates.punishments.menu.HistoryMenu
import com.birthdates.punishments.menu.MenuService
import com.birthdates.punishments.util.Executors
import com.birthdates.service.Services
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.entity.Player
import java.util.UUID

@CommandAlias("history|h")
@CommandPermission("punishments.history")
class HistoryCommand : BaseCommand() {
    val databaseService: DatabaseService = Services.fetch()
    val menuService: MenuService = Services.fetch()

    @Default
    fun history(player: Player, @Name("player") targetName: String) {
        Executors.IO.execute {
            val id = Bukkit.getOfflinePlayer(targetName).uniqueId
            val punishments = databaseService.getAllPunishments(id).get()
            if (punishments.isEmpty()) {
                player.sendMessage("${ChatColor.RED}No punishments found for $targetName.")
                return@execute
            }

            val names: MutableMap<UUID, String> = mutableMapOf()
            // map all punishments givers to bukkit.getOfflinePlayer(it).name and skip if null
            punishments.forEach {
                if (it.giver != null) {
                    names[it.giver] = Bukkit.getOfflinePlayer(it.giver).name ?: "UNKNOWN"
                }
            }

            val menu = HistoryMenu(punishments, names)
            Bukkit.getScheduler().runTask(PunishmentsPlugin.getInstance(), Runnable {
                menuService.openMenu(player, menu)
            })
        }
    }

}