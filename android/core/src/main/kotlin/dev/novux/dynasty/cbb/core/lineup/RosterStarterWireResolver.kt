package dev.novux.dynasty.cbb.core.lineup

import dev.novux.dynasty.cbb.core.lineup.DefaultStarterAlgorithm
import dev.novux.dynasty.cbb.core.roster.PlayerPosition
import dev.novux.dynasty.cbb.core.roster.RosterPlayer

/** Load persisted starter slots for Strategy / sim prep (MEGA-SIM-141). */
object RosterStarterWireResolver {
    fun resolve(
        players: List<RosterPlayer>,
        starterBlockStamina: Int,
    ): Map<PlayerPosition, String> {
        val wired = PlayerPosition.STARTER_ORDER.mapNotNull { slot ->
            val player = players.firstOrNull {
                it.isStarter && it.starterSlotWire == slot.wire
            } ?: return@mapNotNull null
            slot to player.playerId
        }.toMap()
        return if (wired.size == PlayerPosition.STARTER_ORDER.size) {
            wired
        } else {
            DefaultStarterAlgorithm.pickStarters(players, starterBlockStamina)
        }
    }

    fun applyToRoster(
        players: List<RosterPlayer>,
        starters: Map<PlayerPosition, String>,
    ): List<RosterPlayer> {
        val starterIds = starters.values.toSet()
        return players.map { player ->
            val slot = starters.entries.firstOrNull { it.value == player.playerId }?.key
            when {
                slot != null -> player.copy(isStarter = true, starterSlotWire = slot.wire)
                player.isStarter || player.starterSlotWire != null ->
                    player.copy(isStarter = false, starterSlotWire = null)
                else -> player
            }
        }
    }
}
