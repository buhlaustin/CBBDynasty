package dev.novux.dynasty.cbb.core.carousel

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CarouselLeagueBackgroundSimEngineTest {
    @Test
    fun behaviorTable_flagsHighPortalExodusAsRisky() {
        val row = CarouselLeagueCoachRow(
            teamId = "t1",
            programPrestige = 70,
            wins = 11,
            losses = 21,
            madePostseason = false,
            deepPostseasonRun = false,
            coachAge = 52,
            tenureYears = 6,
            portalDepartures = 7,
            contractYearsLeft = 2,
            jobSecurityScore = 40,
            isUserProgram = false,
        )
        assertTrue(CarouselAiBehaviorTable.totalFiringRisk(row) >= CarouselAiBehaviorTable.FIRING_RISK_FIRING_THRESHOLD)
    }

    @Test
    fun backgroundSim_recordsLeagueChurn() {
        val context = CarouselSimContextBootstrap.minimalLeague("dynasty-a", seasonIndex = 2)
        val result = CarouselLeagueBackgroundSimEngine.run(context)
        assertTrue(result.summary.aiFired >= 0)
        assertTrue(result.summary.aiExtended >= 0)
    }

    @Test
    fun engine_noLongerUsesStubApproval_onlyPlanner() {
        val context = CarouselSimContextBootstrap.minimalLeague("dynasty-b", seasonIndex = 2)
        var state = CoachingCarouselEngine.initialState(2)
        state = CoachingCarouselEngine.runBackgroundSim(state, context)
        assertFalse(state.status == CarouselStatus.Complete && state.backgroundSimSummary == null)
        assertTrue(state.backgroundSimGeneration == 1)
    }
}
