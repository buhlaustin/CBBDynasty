package dev.novux.dynasty.cbb.core.calendar

import dev.novux.dynasty.cbb.core.calendar.checkpoint.WeekCheckpointState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CalendarWeekLabelTest {
    @Test
    fun exhibitionWeeks_useExhibitionLabels() {
        assertEquals("Exhibition Week 1", CalendarWeekLabel.display(CalendarPosition(1, CalendarPhase.Exhibition, 1)))
        assertEquals("Exhibition Week 3", CalendarWeekLabel.display(CalendarPosition(1, CalendarPhase.Exhibition, 3)))
    }

    @Test
    fun postseasonLabelIncludesNcbaRound() {
        val label = CalendarWeekLabel.display(CalendarPosition(1, CalendarPhase.Postseason, 1))
        assertEquals("Postseason Week 1 — Opening Round", label)
    }

    @Test
    fun conferenceTournamentLabelIncludesPackMaxWhenProvided() {
        val label = CalendarWeekLabel.display(
            CalendarPosition(1, CalendarPhase.ConferenceTournament, 2),
            leagueMaxConfTourneyWeeks = 4,
        )
        assertEquals("Conference Tournament Week 2 of 4", label)
    }

    @Test
    fun advancingEmptyExhibitionWeek_succeeds() {
        val state = WeekCheckpointState.bootstrap(1, CalendarPhase.Exhibition)
        val result = state.advanceWeek()
        assertEquals(CalendarPhase.Exhibition, result.committedWeek.phase)
        assertEquals(1, result.committedWeek.weekIndex)
        assertEquals(CalendarPhase.Exhibition, result.phaseAfterAdvance)
        assertEquals(2, result.nextState.activePosition.weekIndex)
        assertTrue(result.gamesCommitted.isEmpty())
    }
}
