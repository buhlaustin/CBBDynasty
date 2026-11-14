package dev.novux.dynasty.cbb.core.preseason

import dev.novux.dynasty.cbb.core.schedule.ScheduleGameKind
import org.junit.Assert.assertEquals
import org.junit.Test

class FirstPlayableGameDateLabelTest {
    @Test
    fun regularSeasonWeek_mapsToDisplayDate() {
        val game = FirstPlayableGame(
            gameId = "rs-4-x",
            opponentTeamId = "x",
            opponentDisplayName = "Opponent",
            homeGame = true,
            kind = ScheduleGameKind.NonConference,
            calendarWeekIndex = 4,
        )
        assertEquals("Dec 4", game.gameDateLabel(seasonIndex = 1))
    }
}
