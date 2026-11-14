package dev.novux.dynasty.cbb.core.calendar

import dev.novux.dynasty.cbb.core.calendar.checkpoint.WeekCheckpointState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SeasonReviewGateTest {
    @Test
    fun enterOffseason_fromSeasonReview_rollsToNextYearOffseasonWeek1() {
        val state = WeekCheckpointState.bootstrap(1, CalendarPhase.SeasonReview)
        val transition = SeasonReviewGate.enterOffseason(state, seasonReviewAcknowledged = true)
        val next = transition.advanceResult.nextState.activePosition
        assertEquals(CalendarPhase.Offseason, next.phase)
        assertEquals(2, next.seasonIndex)
        assertEquals(1, next.weekIndex)
        assertEquals(OffseasonEntryKind.CoachingCarouselFirst, transition.offseasonEntry)
    }

    @Test
    fun gate_activeOnlyInSeasonReviewPhase() {
        val review = WeekCheckpointState.bootstrap(1, CalendarPhase.SeasonReview)
        val off = WeekCheckpointState.bootstrap(1, CalendarPhase.Offseason)
        assertTrue(SeasonReviewGate.isActive(review))
        assertEquals(false, SeasonReviewGate.isActive(off))
    }

    @Test
    fun canEnterOffseason_requiresAcknowledgement() {
        val review = WeekCheckpointState.bootstrap(1, CalendarPhase.SeasonReview)
        assertEquals(false, SeasonReviewGate.canEnterOffseason(review, seasonReviewAcknowledged = false))
        assertTrue(SeasonReviewGate.canEnterOffseason(review, seasonReviewAcknowledged = true))
    }
}
