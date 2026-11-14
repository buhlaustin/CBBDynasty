package dev.novux.dynasty.cbb.core.strategy

import dev.novux.dynasty.cbb.core.roster.PlayerPosition
import dev.novux.dynasty.cbb.core.sim.GameSimRosterInput

/**
 * Perimeter / Post presets tilt weighted shot shares toward perimeter or post slots (MEGA-SIM-148).
 */
object SchemeOffensiveSlotUsageBias {
    private const val BIAS_MULTIPLIER = 1.28

    fun apply(
        scheme: OffensiveSchemePreset,
        roster: GameSimRosterInput?,
        offenseFloor: List<String>,
        baseWeights: Map<String, Int>,
    ): Map<String, Int> {
        val biasSlots = biasedSlots(scheme)
        if (biasSlots.isEmpty() || offenseFloor.isEmpty()) return baseWeights
        val slotByPlayer = roster?.participants?.associate { it.playerId to it.slotPlayed }.orEmpty()
        val merged = if (baseWeights.isEmpty()) {
            offenseFloor.associateWith { 20 }
        } else {
            baseWeights
        }
        return merged.mapValues { (id, weight) ->
            val slot = slotByPlayer[id]
            if (id in offenseFloor && slot != null && slot in biasSlots) {
                (weight * BIAS_MULTIPLIER).toInt().coerceAtLeast(1)
            } else {
                weight
            }
        }
    }

    private fun biasedSlots(scheme: OffensiveSchemePreset): Set<PlayerPosition> = when (scheme) {
        OffensiveSchemePreset.Perimeter -> setOf(
            PlayerPosition.PointGuard,
            PlayerPosition.ShootingGuard,
            PlayerPosition.SmallForward,
        )
        OffensiveSchemePreset.Post -> setOf(
            PlayerPosition.PowerForward,
            PlayerPosition.Center,
        )
        else -> emptySet()
    }
}
