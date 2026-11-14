package dev.novux.dynasty.cbb.core.hubs

import dev.novux.dynasty.cbb.core.calendar.CalendarPhase
import dev.novux.dynasty.cbb.core.calendar.CalendarPosition
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DynastyHubMenuPolicyTest {
    @Test
    fun marchMayhemVisibleAfterSelectionSundayInPostseason() {
        val position = CalendarPosition(1, CalendarPhase.Postseason, 1)
        assertTrue(DynastyHubMenuPolicy.marchMayhemMenuVisible(position, selectionSundayPublished = true))
    }

    @Test
    fun marchMayhemHiddenInPostseasonBeforeSelectionSunday() {
        val position = CalendarPosition(1, CalendarPhase.Postseason, 1)
        assertFalse(DynastyHubMenuPolicy.marchMayhemMenuVisible(position, selectionSundayPublished = false))
    }

    @Test
    fun marchMayhemHiddenInRegularSeasonEvenIfSelectionPublished() {
        val position = CalendarPosition(1, CalendarPhase.RegularSeason, 10)
        assertFalse(DynastyHubMenuPolicy.marchMayhemMenuVisible(position, selectionSundayPublished = true))
    }

    @Test
    fun confTourneyVisibleInConferencePhase() {
        val position = CalendarPosition(1, CalendarPhase.ConferenceTournament, 1)
        assertTrue(DynastyHubMenuPolicy.conferenceTournamentMenuVisible(position))
    }

    @Test
    fun confTourneyHiddenInRegularSeason() {
        val position = CalendarPosition(1, CalendarPhase.RegularSeason, 10)
        assertFalse(DynastyHubMenuPolicy.conferenceTournamentMenuVisible(position))
    }
}
