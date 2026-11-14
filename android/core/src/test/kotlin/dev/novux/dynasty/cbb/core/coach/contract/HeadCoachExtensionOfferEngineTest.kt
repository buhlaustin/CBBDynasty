package dev.novux.dynasty.cbb.core.coach.contract

import dev.novux.dynasty.cbb.core.coach.CoachArchetype
import dev.novux.dynasty.cbb.core.coach.card.HeadCoachCard
import dev.novux.dynasty.cbb.core.coach.card.HeadCoachRatings
import dev.novux.dynasty.cbb.core.coach.security.AdTrustBand
import dev.novux.dynasty.cbb.core.coach.security.AdTrustSnapshot
import dev.novux.dynasty.cbb.core.coach.security.CoachSecuritySnapshot
import dev.novux.dynasty.cbb.core.coach.security.JobSecuritySnapshot
import dev.novux.dynasty.cbb.core.coach.security.JobSecurityTier
import dev.novux.dynasty.cbb.core.coach.security.JobSecurityTrend
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class HeadCoachExtensionOfferEngineTest {
    private val card = HeadCoachCard(
        dynastyId = "d1",
        displayName = "Coach",
        age = 45,
        schoolDisplayName = "State",
        seasonRecordLabel = "10-2",
        archetype = CoachArchetype.Balanced,
        offensiveSchemeLabel = "Balanced",
        defensiveSchemeLabel = "Man",
        contractYearsLeft = 1,
        contractLengthYears = 4,
        annualSalaryUsd = 1_000_000,
        buyoutUsd = 500_000,
        ratings = HeadCoachRatings(80, 80, 80, 80),
        almaMaterLabel = null,
    )

  private val secureSnapshot = CoachSecuritySnapshot(
        jobSecurity = JobSecuritySnapshot(85, JobSecurityTier.Secure, JobSecurityTrend.Flat, emptyList()),
        adTrust = AdTrustSnapshot(80, AdTrustBand.Strong),
    )

    @Test
    fun offerWhenTrustAndTierQualify() {
        assertNotNull(
            HeadCoachExtensionOfferEngine.incoming(card, secureSnapshot, dismissedForSeasonIndex = null, 3),
        )
    }

    @Test
    fun noOfferWhenDeclinedThisSeason() {
        assertNull(
            HeadCoachExtensionOfferEngine.incoming(card, secureSnapshot, dismissedForSeasonIndex = 3, currentSeasonIndex = 3),
        )
    }
}
