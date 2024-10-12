package com.birthdates.punishments.punishment

import com.birthdates.service.Service
import org.bukkit.command.CommandSender

/**
 * A service for managing punishments.
 */
interface PunishmentService : Service {
    /**
     * Adds a punishment for a player.
     *
     * @param playerName The name of the player to add the punishment for.
     * @param punishment The punishment to add.
     * @see Punishment
     */
    fun addPunishment(playerName: String, punishment: Punishment)

    /**
     * Deactivates a punishment for a player.
     *
     * @param revoker The command sender that is revoking the punishment.
     * @param playerName The name of the player to deactivate the punishment for.
     * @param punishmentType The type of punishment to deactivate.
     */
    fun deactivatePunishment(revoker: CommandSender, playerName: String, punishmentType: PunishmentType)

}