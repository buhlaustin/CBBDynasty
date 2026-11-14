package dev.novux.dynasty.cbb.core.advance

import dev.novux.dynasty.cbb.core.calendar.checkpoint.WeekCheckpointState

/** User may re-watch or re-sim staged games before week advance ([MEGA-GDD-012]). */
object ProvisionalGameReplayPolicy {
    const val MEGA_GDD_ID = "MEGA-GDD-012"

    fun hasStagedUserGame(checkpoint: WeekCheckpointState): Boolean =
        checkpoint.provisionalGames.isNotEmpty()

    fun canReplayGame(checkpoint: WeekCheckpointState, gameId: String): Boolean =
        checkpoint.canReplay(gameId)
}