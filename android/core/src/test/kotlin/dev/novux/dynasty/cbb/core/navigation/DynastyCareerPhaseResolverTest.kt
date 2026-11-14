package dev.novux.dynasty.cbb.core.navigation

import dev.novux.dynasty.cbb.core.calendar.CalendarPhase
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class DynastyCareerPhaseResolverTest {
    @Test
    fun mapsCareerPhasesToBodies() {
        assertEquals(
            DynastyCareerPhaseResolver.CareerBody.Offseason,
            DynastyCareerPhaseResolver.careerBody(CalendarPhase.Offseason),
        )
        assertEquals(
            DynastyCareerPhaseResolver.CareerBody.Preseason,
            DynastyCareerPhaseResolver.careerBody(CalendarPhase.Preseason),
        )
        assertEquals(
            DynastyCareerPhaseResolver.CareerBody.InSeason,
            DynastyCareerPhaseResolver.careerBody(CalendarPhase.Exhibition),
        )
        assertEquals(
            DynastyCareerPhaseResolver.CareerBody.InSeason,
            DynastyCareerPhaseResolver.careerBody(CalendarPhase.RegularSeason),
        )
    }

    @Test
    fun seasonReviewIsNotCareerBody() {
        assertNull(DynastyCareerPhaseResolver.careerBody(CalendarPhase.SeasonReview))
    }
}
