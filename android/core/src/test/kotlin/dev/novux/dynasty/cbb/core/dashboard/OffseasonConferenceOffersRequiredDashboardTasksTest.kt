package dev.novux.dynasty.cbb.core.dashboard

import dev.novux.dynasty.cbb.core.offseason.weekten.ConferenceOfferCard
import dev.novux.dynasty.cbb.core.offseason.weekten.ConferenceOfferDecision
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class OffseasonConferenceOffersRequiredDashboardTasksTest {
    private val offer = ConferenceOfferCard(
        offerId = "o1",
        invitingConferenceId = "mw",
        invitingConferenceName = "Mountain West",
        currentConferenceName = "WAC",
        inviteScore = 80,
        prosSummary = "pros",
        consSummary = "cons",
    )

    @Test
    fun seasonOne_skipsRequiredTasks() {
        assertTrue(
            OffseasonConferenceOffersRequiredDashboardTasks.build(
                listOf(offer),
                emptyMap(),
                seasonIndex = 1,
            ).isEmpty(),
        )
        assertTrue(
            OffseasonConferenceOffersRequiredDashboardTasks.canAdvanceWeek(
                listOf(offer),
                emptyMap(),
                seasonIndex = 1,
            ),
        )
    }

    @Test
    fun pendingOffer_emitsRequiredCard() {
        val tasks = OffseasonConferenceOffersRequiredDashboardTasks.build(
            listOf(offer),
            emptyMap(),
            seasonIndex = 2,
        )
        assertEquals(1, tasks.size)
        assertEquals(DashboardTaskId.OffseasonConferenceOffer, tasks.single().id)
        assertFalse(
            OffseasonConferenceOffersRequiredDashboardTasks.canAdvanceWeek(
                listOf(offer),
                emptyMap(),
                seasonIndex = 2,
            ),
        )
    }

    @Test
    fun resolvedOffer_clearsTasks() {
        val tasks = OffseasonConferenceOffersRequiredDashboardTasks.build(
            listOf(offer),
            mapOf(offer.offerId to ConferenceOfferDecision.Declined),
            seasonIndex = 2,
        )
        assertTrue(tasks.isEmpty())
        assertTrue(
            OffseasonConferenceOffersRequiredDashboardTasks.canAdvanceWeek(
                listOf(offer),
                mapOf(offer.offerId to ConferenceOfferDecision.Declined),
                seasonIndex = 2,
            ),
        )
    }
}
