package dev.novux.dynasty.cbb.core.advance

import dev.novux.dynasty.cbb.core.calendar.CalendarPhase
import dev.novux.dynasty.cbb.core.offseason.weekeight.OffseasonRosterLockConstants
import dev.novux.dynasty.cbb.core.offseason.weekten.OffseasonWeekTenConstants
import dev.novux.dynasty.cbb.core.signingday.SigningDayConstants

/** Offseason advances that should always show destination confirm ([MEGA-FRAME-108]). */
object OffseasonAdvanceConfirmPolicy {
    fun requiresLightweightDialog(phase: CalendarPhase, offseasonWeekIndex: Int?): Boolean {
        if (phase != CalendarPhase.Offseason || offseasonWeekIndex == null) return false
        return when (offseasonWeekIndex) {
            1,
            SigningDayConstants.OFFSEASON_WEEK_INDEX,
            OffseasonRosterLockConstants.OFFSEASON_WEEK_INDEX,
            OffseasonWeekTenConstants.OFFSEASON_WEEK_INDEX,
            -> true
            else -> false
        }
    }
}