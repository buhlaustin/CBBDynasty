package dev.novux.dynasty.cbb.core.dashboard

import dev.novux.dynasty.cbb.core.calendar.CalendarPhase
import dev.novux.dynasty.cbb.core.calendar.checkpoint.ProvisionalGameBoxArchive
import kotlinx.serialization.Serializable

/** Dashboard “last game” row ([planning/game-frame.md#last-game], [MEGA-FRAME-038]). */
@Serializable
data class DashboardLastGameSnapshot(
    val opponentDisplayName: String,
    val userScore: Int,
    val opponentScore: Int,
    val userWon: Boolean,
    val weekIndex: Int,
    val phase: CalendarPhase,
    val userTeamIsHome: Boolean,
    val userTeamDisplayName: String,
    val boxArchive: ProvisionalGameBoxArchive? = null,
)

data class DashboardLastGameSummary(
    val snapshot: DashboardLastGameSnapshot,
    val hasBoxScore: Boolean,
)
