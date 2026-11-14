package dev.novux.dynasty.cbb.core.coach.contract

import dev.novux.dynasty.cbb.core.coach.CoachArchetype
import dev.novux.dynasty.cbb.core.coach.card.HeadCoachCard
import dev.novux.dynasty.cbb.core.coach.card.HeadCoachRatings
import dev.novux.dynasty.cbb.core.coach.contract.HeadCoachContractBuyout
import org.junit.Assert.assertTrue
import org.junit.Test

class HeadCoachContractEngineTest {
    @Test
    fun deriveSalaryFromCardModel() {
        val lowSalary = HeadCoachContractSalary.annualUsd(CoachArchetype.Balanced, programPrestige = 20)
        val highSalary = HeadCoachContractSalary.annualUsd(CoachArchetype.Balanced, programPrestige = 90)
        val low = HeadCoachContractEngine.derive(sampleCard(lowSalary), null)
        val high = HeadCoachContractEngine.derive(sampleCard(highSalary), null)
        assertTrue(high.annualSalaryUsd > low.annualSalaryUsd)
        assertTrue(high.buyoutUsd > low.buyoutUsd)
        assertTrue(low.annualSalaryUsd == lowSalary)
    }

    private fun sampleCard(annualSalaryUsd: Long, contractYearsLeft: Int = 3) = HeadCoachCard(
        dynastyId = "d1",
        displayName = "Test Coach",
        age = 45,
        schoolDisplayName = "State U",
        seasonRecordLabel = "12-5",
        archetype = CoachArchetype.Balanced,
        offensiveSchemeLabel = "Balanced",
        defensiveSchemeLabel = "Man",
        contractYearsLeft = contractYearsLeft,
        contractLengthYears = 4,
        annualSalaryUsd = annualSalaryUsd,
        buyoutUsd = HeadCoachContractBuyout.usd(annualSalaryUsd, contractYearsLeft),
        ratings = HeadCoachRatings(80, 80, 80, 80),
        almaMaterLabel = null,
    )
}
