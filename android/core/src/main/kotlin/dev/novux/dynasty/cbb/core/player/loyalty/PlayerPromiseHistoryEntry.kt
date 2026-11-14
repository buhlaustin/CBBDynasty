package dev.novux.dynasty.cbb.core.player.loyalty

/** One season promise evaluation on the player card History tab ([MEGA-COACH-086]). */
data class PlayerPromiseHistoryEntry(
    val seasonIndex: Int,
    val status: PromiseStatus,
)
