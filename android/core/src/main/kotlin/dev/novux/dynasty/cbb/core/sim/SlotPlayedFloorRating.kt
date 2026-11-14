package dev.novux.dynasty.cbb.core.sim

import dev.novux.dynasty.cbb.core.roster.PlayerPosition
import dev.novux.dynasty.cbb.core.sim.tuning.SimPositionComfortModifiers

/** Per-possession team rating from the five **slot played** positions on the floor (MEGA-SIM-167). */
object SlotPlayedFloorRating {
    fun offensiveFromFloor(
        floor: List<GameSimParticipant>,
        fallback: Int,
        comfort: SimPositionComfortModifiers = SimPositionComfortModifiers(),
    ): Int = fromFloor(floor, fallback, comfort) { PositionComfortNudge.offensiveRating(it, comfort) }

    fun defensiveFromFloor(
        floor: List<GameSimParticipant>,
        fallback: Int,
        comfort: SimPositionComfortModifiers = SimPositionComfortModifiers(),
    ): Int = fromFloor(floor, fallback, comfort) { PositionComfortNudge.defensiveRating(it, comfort) }

    fun meanThreePointTendency(floor: List<GameSimParticipant>): Int {
        if (floor.isEmpty()) return 50
        return floor.map { it.shooting.threePointTendency }.average().toInt().coerceIn(1, 99)
    }

    private fun fromFloor(
        floor: List<GameSimParticipant>,
        fallback: Int,
        comfort: SimPositionComfortModifiers,
        pick: (GameSimParticipant) -> Int,
    ): Int {
        if (floor.isEmpty()) return fallback
        val bySlot = floor.groupBy { it.slotPlayed }
        val slotRatings = PlayerPosition.STARTER_ORDER.map { slot ->
            val candidates = bySlot[slot].orEmpty()
            when {
                candidates.isEmpty() -> fallback
                candidates.size == 1 -> pick(candidates.first())
                else -> {
                    val best = candidates.maxByOrNull { pick(it) } ?: candidates.first()
                    pick(best)
                }
            }
        }
        return slotRatings.average().toInt().coerceIn(1, 99)
    }
}
