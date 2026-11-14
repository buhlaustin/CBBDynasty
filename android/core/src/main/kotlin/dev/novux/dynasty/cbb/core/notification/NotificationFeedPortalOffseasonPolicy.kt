package dev.novux.dynasty.cbb.core.notification

import dev.novux.dynasty.cbb.core.calendar.CalendarPhase

/** Transfer-portal commit feed rows are offseason-only ([MEGA-SHELL-130], [MEGA-SHELL-131]). */
object NotificationFeedPortalOffseasonPolicy {
    const val MEGA_ID: String = "MEGA-SHELL-130"

    fun portalNotificationWindow(phase: CalendarPhase, offseasonWeekIndex: Int): Boolean =
        phase == CalendarPhase.Offseason && offseasonWeekIndex in 1..5
}