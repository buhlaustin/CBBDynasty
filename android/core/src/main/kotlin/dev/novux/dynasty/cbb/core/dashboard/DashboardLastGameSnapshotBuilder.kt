package dev.novux.dynasty.cbb.core.dashboard

import dev.novux.dynasty.cbb.core.calendar.CalendarPhase
import dev.novux.dynasty.cbb.core.calendar.CalendarPosition
import dev.novux.dynasty.cbb.core.calendar.checkpoint.ProvisionalGameMark

object DashboardLastGameSnapshotBuilder {
    fun fromCommittedMark(
        mark: ProvisionalGameMark,
        committedWeek: CalendarPosition,
        userTeamIsHome: Boolean,
        opponentDisplayName: String,
        userTeamDisplayName: String,
    ): DashboardLastGameSnapshot {
        val userScore = if (userTeamIsHome) mark.homeScore else mark.awayScore
        val oppScore = if (userTeamIsHome) mark.awayScore else mark.homeScore
        return DashboardLastGameSnapshot(
            opponentDisplayName = opponentDisplayName,
            userScore = userScore,
            opponentScore = oppScore,
            userWon = userScore > oppScore,
            weekIndex = committedWeek.weekIndex,
            phase = committedWeek.phase,
            userTeamIsHome = userTeamIsHome,
            userTeamDisplayName = userTeamDisplayName,
            boxArchive = mark.boxArchive,
        )
    }
}
