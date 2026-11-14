package dev.novux.dynasty.cbb.core.recruiting

import dev.novux.dynasty.cbb.core.calendar.CalendarPhase
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SeasonOneRecruitingBootstrapTest {
    @Test
    fun appliesOnlyToSeasonOne() {
        assertTrue(SeasonOneRecruitingBootstrap.appliesToMembershipYear(1))
        assertFalse(SeasonOneRecruitingBootstrap.appliesToMembershipYear(2))
    }

    @Test
    fun top60RequiredBeforeExhibitionsAndInSeason() {
        assertFalse(SeasonOneRecruitingBootstrap.requiresTop60BeforePhase(CalendarPhase.Offseason))
        assertFalse(SeasonOneRecruitingBootstrap.requiresTop60BeforePhase(CalendarPhase.Preseason))
        assertTrue(SeasonOneRecruitingBootstrap.requiresTop60BeforePhase(CalendarPhase.Exhibition))
        assertTrue(SeasonOneRecruitingBootstrap.requiresTop60BeforePhase(CalendarPhase.RegularSeason))
    }
}
