package dev.novux.dynasty.cbb.core.awards

import dev.novux.dynasty.cbb.core.calendar.CalendarPhase

/** Weekly awards available during regular season weeks ([MEGA-GDD-049]). */
object AwardsWeeklyCadencePolicy {
    const val MEGA_ID: String = "MEGA-GDD-049"

    fun weeklyAwardsLive(phase: CalendarPhase): Boolean = AwardsHubPolicy.weeklyAwardsLive(phase)
}