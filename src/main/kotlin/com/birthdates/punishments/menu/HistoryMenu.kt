package com.birthdates.punishments.menu

import com.birthdates.punishments.punishment.Punishment
import com.birthdates.punishments.punishment.PunishmentType
import com.birthdates.punishments.util.Format
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import java.util.UUID

class HistoryMenu {
    val buttons: MutableMap<Int, ItemStack> = mutableMapOf()
    val inventory: Inventory = Bukkit.createInventory(null, 54)
    var page: Int = 0
    val punishments: List<Punishment>
    val names: Map<UUID, String>
    val maxPage: Int

    constructor(punishments: List<Punishment>, names: Map<UUID, String>) {
        this.punishments = punishments
        this.names = names
        this.maxPage = (punishments.size / 45) + 1
        changePage(0)
    }

    fun changePage(page: Int) {
        if (page < 0 || page >= maxPage) {
            return
        }
        this.page = page
        for (i in 0 until
                45) {
            val index = i + (page * 45)
            if (index >= punishments.size) {
                break
            }

            val punishment = punishments[index]
            val button = ItemStack(if (punishment.active && punishment.isNotExpired() && punishment.type != PunishmentType.KICK) Material.GREEN_WOOL else Material.RED_WOOL)
            button.editMeta {
                it.setDisplayName("${ChatColor.RED}${punishment.type.name}")
                it.lore = mutableListOf(
                    "",
                    "${ChatColor.GOLD}Status: ${ChatColor.WHITE}${if (punishment.active) "Active" else "Inactive"}",
                    "${ChatColor.GOLD}Reason: ${ChatColor.WHITE}${punishment.reason}",
                    "${ChatColor.GOLD}Duration: ${ChatColor.WHITE}${Format.formatDuration(punishment.duration)} (${if (punishment.isNotExpired()) "${ChatColor.GREEN}ACTIVE" else "${ChatColor.RED}EXPIRED"}${ChatColor.WHITE})",
                    "${ChatColor.GOLD}When: ${ChatColor.WHITE}${Format.formatDuration(System.currentTimeMillis()-punishment.createdAt)} ago",
                    "${ChatColor.GOLD}Issued by: ${ChatColor.WHITE}${
                        punishment.giver?.let {
                            names[it]
                        } ?: "CONSOLE"
                    }",
                )
            }
            buttons[i] = button
            inventory.setItem(i, button)
        }
        // set slot 48 to prev page and 50 to next page
        val prevPage = ItemStack(Material.ARROW)
        prevPage.editMeta { it.setDisplayName("${ChatColor.GOLD}Previous Page") }
        val nextPage = ItemStack(Material.ARROW)
        nextPage.editMeta { it.setDisplayName("${ChatColor.GOLD}Next Page") }
        inventory.setItem(48, prevPage)
        inventory.setItem(50, nextPage)
    }

    fun open(player: Player) {
        player.openInventory(inventory)
    }
}