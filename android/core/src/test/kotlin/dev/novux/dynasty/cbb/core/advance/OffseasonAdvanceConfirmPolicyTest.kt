package dev.novux.dynasty.cbb.core.advance

import dev.novux.dynasty.cbb.core.calendar.CalendarPhase
import dev.novux.dynasty.cbb.core.offseason.weekeight.OffseasonRosterLockConstants
import dev.novux.dynasty.cbb.core.signingday.SigningDayConstants
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** [MEGA-FRAME-108] */
class OffseasonAdvanceConfirmPolicyTest {
    @Test
    fun signingDayWeek_requiresDialog() {
        assertTrue(
            OffseasonAdvanceConfirmPolicy.requiresLightweightDialog(
                CalendarPhase.Offseason,
                SigningDayConstants.OFFSEASON_WEEK_INDEX,
            ),
        )
    }

    @Test
    fun midPortalWeek_doesNotForceDialog() {
        assertFalse(
            OffseasonAdvanceConfirmPolicy.requiresLightweightDialog(CalendarPhase.Offseason, 3),
        )
    }

    @Test
    fun rosterLockWeek_requiresDialog() {
        assertTrue(
            OffseasonAdvanceConfirmPolicy.requiresLightweightDialog(
                CalendarPhase.Offseason,
                OffseasonRosterLockConstants.OFFSEASON_WEEK_INDEX,
            ),
        )
    }
}