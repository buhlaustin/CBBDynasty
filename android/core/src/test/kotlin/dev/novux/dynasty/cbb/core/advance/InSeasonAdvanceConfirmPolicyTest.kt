package dev.novux.dynasty.cbb.core.advance

import dev.novux.dynasty.cbb.core.calendar.CalendarPhase
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** [MEGA-FRAME-115] */
class InSeasonAdvanceConfirmPolicyTest {
    @Test
    fun regularSeason_requiresConfirm() {
        assertTrue(InSeasonAdvanceConfirmPolicy.requiresDestinationAcknowledgment(CalendarPhase.RegularSeason))
    }

    @Test
    fun offseason_doesNotForceInSeasonAck() {
        assertFalse(InSeasonAdvanceConfirmPolicy.requiresDestinationAcknowledgment(CalendarPhase.Offseason))
    }
}