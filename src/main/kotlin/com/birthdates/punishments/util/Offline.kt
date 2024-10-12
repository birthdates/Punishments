package com.birthdates.punishments.util

import org.bukkit.Bukkit
import org.bukkit.OfflinePlayer
import java.util.concurrent.CompletableFuture

class Offline {
    companion object {
        fun getPlayer(name: String): CompletableFuture<OfflinePlayer> {
            return CompletableFuture.supplyAsync {
                val player = Bukkit.getOfflinePlayer(name)
                player
            }
        }
    }
}