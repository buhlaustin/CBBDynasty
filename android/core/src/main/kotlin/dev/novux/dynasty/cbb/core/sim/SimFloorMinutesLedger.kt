package dev.novux.dynasty.cbb.core.sim

/** Accumulates floor seconds per player during possession sim (MEGA-SIM-103). */
class SimFloorMinutesLedger(
    initialFloorSeconds: Map<String, Int> = emptyMap(),
) {
    private val floorSeconds: MutableMap<String, Int> = initialFloorSeconds.toMutableMap()

    fun addFloorTime(playerIds: Collection<String>, seconds: Int) {
        if (seconds <= 0) return
        playerIds.forEach { id ->
            floorSeconds[id] = (floorSeconds[id] ?: 0) + seconds
        }
    }

    fun floorMinutes(playerId: String): Double = (floorSeconds[playerId] ?: 0) / 60.0

    fun snapshot(): Map<String, Int> = floorSeconds.toMap()
}
