package dev.novux.dynasty.cbb.core.awards

import dev.novux.dynasty.cbb.core.calendar.CalendarPhase

/** Weekly player and conference honors during regular-season conference play ([MEGA-GDD-131]). */
object ConferenceWeeklyAwardsPolicy {
    const val MEGA_ID: String = "MEGA-GDD-131"

    fun honorsDuringConferencePlay(phase: CalendarPhase, weekIndex: Int): Boolean =
        phase == CalendarPhase.RegularSeason && weekIndex >= 1
}