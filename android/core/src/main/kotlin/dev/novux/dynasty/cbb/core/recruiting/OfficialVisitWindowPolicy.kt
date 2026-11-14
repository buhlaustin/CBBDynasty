package dev.novux.dynasty.cbb.core.recruiting

import dev.novux.dynasty.cbb.core.balance.BalanceVisitAllowedThrough
import dev.novux.dynasty.cbb.core.calendar.CalendarPhase

/**
 * Official visits open regular season through postseason round 4 and offseason weeks 2–N
 * ([MEGA-RECR-040], [RecruitingActionWindows]).
 */
object OfficialVisitWindowPolicy {
    const val MEGA_ID: String = "MEGA-RECR-040"

    fun visitsOpen(
        phase: CalendarPhase,
        offseasonWeekIndex: Int?,
        allowedThrough: BalanceVisitAllowedThrough = BalanceVisitAllowedThrough(),
    ): Boolean = RecruitingActionWindows.officialVisitOpen(phase, offseasonWeekIndex, allowedThrough)
}