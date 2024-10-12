package com.birthdates.punishments.punishment

import java.util.UUID

data class Punishment(
    val type: PunishmentType,
    val reason: String,
    val duration: Long,
    val giver: UUID?,
    var address: String? = null,
    val active: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
) {

    fun getTimeRemaining(): Long {
        return if (duration == -1L) {
            -1
        } else {
            createdAt + duration - System.currentTimeMillis()
        }
    }

    fun isNotExpired(): Boolean {
        return duration == -1L || getTimeRemaining() > 0
    }

}
