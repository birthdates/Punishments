package com.birthdates.punishments

import co.aikar.commands.PaperCommandManager
import com.birthdates.punishments.command.BanCommand
import com.birthdates.punishments.command.BlacklistCommand
import com.birthdates.punishments.command.HistoryCommand
import com.birthdates.punishments.command.KickCommand
import com.birthdates.punishments.command.MuteCommand
import com.birthdates.punishments.command.TempBanCommand
import com.birthdates.punishments.command.TempMuteCommand
import com.birthdates.punishments.command.UnBanCommand
import com.birthdates.punishments.command.UnBlacklistCommand
import com.birthdates.punishments.command.UnMuteCommand
import com.birthdates.punishments.player.PlayerListener
import com.birthdates.punishments.util.Executors
import com.birthdates.service.Services
import org.bukkit.Bukkit
import org.bukkit.plugin.java.JavaPlugin

class PunishmentsPlugin : JavaPlugin() {
    companion object {
        fun getInstance(): PunishmentsPlugin {
            return getPlugin(PunishmentsPlugin::class.java)
        }
    }

    override fun onEnable() {
        saveDefaultConfig()
        Services.load()
        Bukkit.getPluginManager().registerEvents(PlayerListener(), this)

        val commandManager = PaperCommandManager(this)
        // create a constant array of command classes
        arrayOf(
            BanCommand(),
            BlacklistCommand(),
            MuteCommand(),
            TempBanCommand(),
            TempMuteCommand(),
            UnBanCommand(),
            UnBlacklistCommand(),
            UnMuteCommand(),
            HistoryCommand(),
            KickCommand()
        ).forEach { commandManager.registerCommand(it) }
    }

    override fun onDisable() {
        Services.unload()
        Executors.IO.shutdown()
    }
}