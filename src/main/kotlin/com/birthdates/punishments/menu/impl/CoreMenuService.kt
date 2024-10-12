package com.birthdates.punishments.menu.impl

import com.birthdates.punishments.PunishmentsPlugin
import com.birthdates.punishments.menu.HistoryMenu
import com.birthdates.punishments.menu.MenuService
import com.birthdates.service.register.Register
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import java.util.UUID

@Register
class CoreMenuService : MenuService, Listener {
    val inMenu: MutableMap<UUID, HistoryMenu> = mutableMapOf()

    constructor() {
        Bukkit.getPluginManager().registerEvents(this, PunishmentsPlugin.getInstance())
    }

    override fun openMenu(player: Player, menu: HistoryMenu) {
        inMenu[player.uniqueId] = menu
        menu.open(player)
    }

    @EventHandler
    fun onInventoryClick(event: InventoryClickEvent) {
        if (!inMenu.containsKey(event.whoClicked.uniqueId)) return;
        event.isCancelled = true
        // check if player is clicking in the top inventory in slot 48 or 50 and change page accordingly
        val menu = inMenu[event.whoClicked.uniqueId] ?: return
        if (event.clickedInventory != menu.inventory) return
        if (event.slot == 48) {
            menu.changePage(menu.page - 1)
        } else if (event.slot == 50) {
            menu.changePage(menu.page + 1)
        }
    }

    @EventHandler
    fun onInventoryClose(event: InventoryCloseEvent) {
        val player = event.player as? Player ?: return
        inMenu -= player.uniqueId
    }
}