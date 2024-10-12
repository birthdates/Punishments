package com.birthdates.punishments.lang.impl

import com.birthdates.punishments.PunishmentsPlugin
import com.birthdates.punishments.lang.LanguageService
import com.birthdates.punishments.lang.Placeholder
import com.birthdates.service.register.Register
import org.bukkit.ChatColor

@Register
class CoreLanguageService : LanguageService {
    val config = PunishmentsPlugin.getInstance().config.getConfigurationSection("messages")!!

    override fun getMessage(key: String, vararg args: Placeholder): String {
        // get the key from the config then replace the placeholders with args (the varargs param args will be like %player%, playerName, %level%, level). NOTE: placeholders can be in the message multiple times in any order
        return ChatColor.translateAlternateColorCodes('&',
            config.getString(key).let { message ->
                args.fold(message) { acc, (placeholder, value) ->
                    acc!!.replace("%$placeholder%", value?.toString() ?: "UNKNOWN")
                }.toString()
            })
    }
}