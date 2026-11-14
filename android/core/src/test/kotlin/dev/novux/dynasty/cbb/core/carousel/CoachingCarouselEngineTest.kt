package dev.novux.dynasty.cbb.core.carousel

import dev.novux.dynasty.cbb.core.coach.CoachArchetype
import dev.novux.dynasty.cbb.core.coach.card.HeadCoachCard
import dev.novux.dynasty.cbb.core.coach.card.HeadCoachRatings
import dev.novux.dynasty.cbb.core.coach.security.AdTrustBand
import dev.novux.dynasty.cbb.core.coach.security.AdTrustSnapshot
import dev.novux.dynasty.cbb.core.coach.security.CoachSecuritySnapshot
import dev.novux.dynasty.cbb.core.coach.security.JobSecuritySnapshot
import dev.novux.dynasty.cbb.core.coach.security.JobSecurityTier
import dev.novux.dynasty.cbb.core.coach.security.JobSecurityTrend
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CoachingCarouselEngineTest {
    @Test
    fun season1_skipsCarousel() {
        val state = CoachingCarouselEngine.initialState(1)
        assertEquals(CarouselStatus.NotRequired, state.status)
        assertTrue(state.canEnterOffseasonWeekOne())
        assertFalse(CoachingCarouselGate.blocksOffseasonShell(1, state))
    }

    @Test
    fun season2_requiresBackgroundThenApproval() {
        val headCoach = HeadCoachCard(
            dynastyId = "d-season2",
            displayName = "Coach",
            age = 45,
            schoolDisplayName = "State",
            seasonRecordLabel = "22-10",
            archetype = CoachArchetype.Balanced,
            offensiveSchemeLabel = "Balanced",
            defensiveSchemeLabel = "Man",
            contractYearsLeft = 2,
            contractLengthYears = 4,
            annualSalaryUsd = 900_000,
            buyoutUsd = 400_000,
            ratings = HeadCoachRatings(80, 80, 80, 80),
            almaMaterLabel = null,
        )
        val security = CoachSecuritySnapshot(
            jobSecurity = JobSecuritySnapshot(88, JobSecurityTier.Secure, JobSecurityTrend.Flat, emptyList()),
            adTrust = AdTrustSnapshot(82, AdTrustBand.Strong),
        )
        val context = CarouselSimContextBootstrap.minimalLeague("d-season2", 2).copy(
            userHeadCoach = headCoach,
            userSecurity = security,
        )
        var state = CoachingCarouselEngine.initialState(2)
        assertTrue(CoachingCarouselGate.blocksOffseasonShell(2, state))
        state = CoachingCarouselEngine.runBackgroundSim(state, context)
        assertEquals(CarouselStatus.AwaitingUserApprovals, state.status)
        assertEquals(CarouselApprovalKind.ContractExtension, state.pendingApprovals.single().kind)
        val cardId = state.pendingApprovals.single().id
        state = CoachingCarouselEngine.resolveApproval(state, cardId, accepted = true)
        assertEquals(CarouselStatus.Complete, state.status)
        assertTrue(state.canEnterOffseasonWeekOne())
    }
}
