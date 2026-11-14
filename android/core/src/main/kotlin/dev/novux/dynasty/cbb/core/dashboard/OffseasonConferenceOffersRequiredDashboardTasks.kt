package dev.novux.dynasty.cbb.core.dashboard

import dev.novux.dynasty.cbb.core.offseason.weekten.ConferenceOfferCard
import dev.novux.dynasty.cbb.core.offseason.weekten.ConferenceOfferDecision
import dev.novux.dynasty.cbb.core.offseason.weekten.OffseasonConferenceOffersPolicy

/** Offseason Week 10 required dashboard ([planning/dashboard-tasks.md#offseason], [MEGA-FRAME-028]). */
object OffseasonConferenceOffersRequiredDashboardTasks {
    fun build(
        offers: List<ConferenceOfferCard>,
        decisions: Map<String, ConferenceOfferDecision>,
        seasonIndex: Int,
    ): List<DashboardTask> {
        if (OffseasonConferenceOffersPolicy.skipConferenceOffersWeek(seasonIndex)) return emptyList()
        return offers.mapNotNull { offer ->
            when (decisions[offer.offerId]) {
                ConferenceOfferDecision.Accepted, ConferenceOfferDecision.Declined -> null
                else ->
                    DashboardTask(
                        id = DashboardTaskId.OffseasonConferenceOffer,
                        kind = DashboardTaskKind.Required,
                        deepLink = DashboardDeepLink.OffseasonConferenceOffers,
                        stringFormatArgs = listOf(offer.invitingConferenceName),
                    )
            }
        }
    }

    fun canAdvanceWeek(
        offers: List<ConferenceOfferCard>,
        decisions: Map<String, ConferenceOfferDecision>,
        seasonIndex: Int,
    ): Boolean {
        if (OffseasonConferenceOffersPolicy.skipConferenceOffersWeek(seasonIndex)) return true
        return OffseasonConferenceOffersPolicy.canAdvanceWeek(offers, decisions)
    }
}
