package com.birthdates.punishments.menu

import com.birthdates.service.Service
import org.bukkit.entity.Player

/**
 * A service for managing menus.
 */
interface MenuService : Service {
    /**
     * Opens a menu for a player.
     *
     * @param player The player to open the menu for.
     * @param menu The menu to open.
     */
    fun openMenu(player: Player, menu: HistoryMenu)
}