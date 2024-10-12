package com.birthdates.punishments.punishment

enum class PunishmentType(val verb: String) {
    BLACKLIST("blacklisted"),
    BAN("banned"),
    MUTE("muted"),
    KICK("kicked");

    fun isChatPunishment(): Boolean {
        return this == MUTE
    }
}