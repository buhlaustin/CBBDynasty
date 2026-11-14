package dev.novux.dynasty.cbb.core.dashboard

import dev.novux.dynasty.cbb.core.offseason.weekten.ConferenceOfferCard
import dev.novux.dynasty.cbb.core.offseason.weekten.ConferenceOfferDecision
import dev.novux.dynasty.cbb.core.offseason.weekeight.OffseasonRosterLockConstants
import dev.novux.dynasty.cbb.core.offseason.weekten.OffseasonWeekTenConstants
import dev.novux.dynasty.cbb.core.roster.RosterPlayer
import dev.novux.dynasty.cbb.core.signingday.SigningDayConstants
import dev.novux.dynasty.cbb.core.signingday.SigningDayProgress

/**
 * Required offseason dashboard tasks by calendar week ([planning/dashboard-tasks.md#offseason]).
 */
object OffseasonRequiredDashboardTasks {
    data class WeekContext(
        val weekIndex: Int,
        val seasonIndex: Int,
        val weekOnePlayers: List<RosterPlayer> = emptyList(),
        val signingProgress: SigningDayProgress? = null,
        val rosterScholarshipCount: Int = 0,
        val conferenceOffers: List<ConferenceOfferCard> = emptyList(),
        val conferenceDecisions: Map<String, ConferenceOfferDecision> = emptyMap(),
    )

    fun build(ctx: WeekContext): List<DashboardTask> =
        when (ctx.weekIndex) {
            1 -> OffseasonWeekOneRequiredDashboardTasks.build(ctx.weekOnePlayers)
            SigningDayConstants.OFFSEASON_WEEK_INDEX ->
                ctx.signingProgress?.let { SigningDayShellDashboardTasks.build(it) }.orEmpty()
            OffseasonRosterLockConstants.OFFSEASON_WEEK_INDEX ->
                OffseasonRosterLockRequiredDashboardTasks.build(ctx.rosterScholarshipCount)
            OffseasonWeekTenConstants.OFFSEASON_WEEK_INDEX ->
                OffseasonConferenceOffersRequiredDashboardTasks.build(
                    ctx.conferenceOffers,
                    ctx.conferenceDecisions,
                    ctx.seasonIndex,
                )
            else -> emptyList()
        }
}
