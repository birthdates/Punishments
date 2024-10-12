package com.birthdates.punishments.lang

import com.birthdates.service.Service

/**
 * A service for managing the language and formatting.
 */
interface LanguageService : Service {
    /**
     * Gets a message from the language file.
     *
     * @param key The key of the message to get.
     * @param args The placeholders to replace in the message.
     * @return The formatted message.
     */
    fun getMessage(key: String, vararg args: Placeholder): String
}