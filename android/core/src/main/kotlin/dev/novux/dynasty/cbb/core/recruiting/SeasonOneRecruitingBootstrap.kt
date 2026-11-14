package dev.novux.dynasty.cbb.core.recruiting

import dev.novux.dynasty.cbb.core.calendar.CalendarPhase

/**
 * Season 1 ships the enrolling class at dynasty sign so pools survive Offseason W1 and Top 60
 * is ready before exhibitions ([planning/recruiting-engine.md], MEGA-RECR-053).
 */
object SeasonOneRecruitingBootstrap {
    const val MEGA_RECR_053: String = "MEGA-RECR-053"

    fun appliesToMembershipYear(seasonIndex: Int): Boolean = seasonIndex == 1

    /** Persisted pool key matches [dev.novux.dynasty.cbb.data.preseason.IncomingClassSeedRepository]. */
    fun poolStorageSeasonIndex(membershipSeasonIndex: Int): Int =
        SigningDayPreseasonRecruitingTransition.enrollingPoolSeasonIndex(membershipSeasonIndex)

    /**
     * Top 60 must be published before the user reaches exhibition play (preseason task or wizard bootstrap).
     */
    fun requiresTop60BeforePhase(phase: CalendarPhase): Boolean =
        phase == CalendarPhase.Exhibition ||
            phase == CalendarPhase.RegularSeason ||
            phase == CalendarPhase.ConferenceTournament ||
            phase == CalendarPhase.Postseason
}
