package dev.novux.dynasty.cbb.core.portal

import dev.novux.dynasty.cbb.core.calendar.CalendarPhase

object PortalRecruitingHub {
    const val MEGA_ID: String = "MEGA-GDD-029"

    /** Portal sub-tab active in offseason weeks 2–5 ([planning/transfer-portal.md]). */
    fun isRecruitingWindowOpen(phase: CalendarPhase, offseasonWeekIndex: Int?): Boolean =
        phase == CalendarPhase.Offseason && (offseasonWeekIndex ?: 0) >= 2

    fun rankedEntrants(entrants: List<PortalEntrant>): List<PortalEntrant> =
        PortalTop40.rankedList(entrants)
}
