package dev.novux.dynasty.cbb.core.recruiting

import dev.novux.dynasty.cbb.core.balance.BalanceLoadResult
import dev.novux.dynasty.cbb.core.balance.BalanceRecruitingRecommendedParser
import dev.novux.dynasty.cbb.core.balance.BalanceRulesetCodec
import dev.novux.dynasty.cbb.core.roster.PlayerPosition
import java.nio.file.Path
import kotlin.io.path.readText
import org.junit.Assert.assertTrue
import org.junit.Test

class SignLikelihoodEngineTest {
    private val recommended = loadRecommended()

    @Test
    fun baselineInterest_ignoresSpendChannel() {
        val prospect = RecruitProspect(
            prospectId = "a",
            displayName = "Test Player",
            stars = 3,
            position = PlayerPosition.SmallForward,
            homeState = "OH",
            nilAskUsd = 50_000,
            scoutLetterOverall = "B",
        )
        val school = SchoolRecruitingContext(
            programPrestige = 40,
            schoolHomeState = "OH",
            nilPoolAnnualUsd = 1_200_000,
        )
        val row = SignLikelihoodEngine.evaluate(prospect, school, recommended)
        assertTrue(row.baselineInterest in 40..95)
        assertTrue(row.badge != SignLikelihoodBadge.LongShot || row.adjustedScore < recommended.bands.reachMin)
    }

    @Test
    fun eliteNationalHidden_forLowPrestigeOutOfPipeline() {
        val elite = RecruitProspect(
            prospectId = "elite",
            displayName = "Elite HS",
            stars = 5,
            position = PlayerPosition.PointGuard,
            homeState = "TX",
            nationalHsRank = 3,
            nilAskUsd = 200_000,
            scoutLetterOverall = "A",
        )
        val school = SchoolRecruitingContext(
            programPrestige = 22,
            schoolHomeState = "CA",
            pipelineStates = setOf("NV"),
            nilPoolAnnualUsd = 250_000,
        )
        val rows = RecommendedRecruitList.build(listOf(elite), school, recommended)
        assertTrue(rows.isEmpty())
    }

    @Test
    fun lowPrestige_hidesEliteNationalReach() {
        val pool = RecruitingClassSeed.demoPool(1)
        val school = SchoolRecruitingContext(
            programPrestige = 22,
            schoolHomeState = "CA",
            pipelineStates = setOf("NV"),
            nilPoolAnnualUsd = 250_000,
        )
        val list = RecommendedRecruitList.build(pool, school, recommended)
        assertTrue(list.none { it.prospect.nationalHsRank != null && it.prospect.nationalHsRank <= 5 })
        assertTrue(list.size <= recommended.listCap)
    }

  private fun loadRecommended(): dev.novux.dynasty.cbb.core.balance.BalanceRecruitingRecommended {
        var dir = Path.of(System.getProperty("user.dir")).toAbsolutePath()
        while (dir != null && !dir.resolve("assets/balance/balance-2026.json").toFile().exists()) {
            dir = dir.parent
        }
        val raw = dir!!.resolve("assets/balance/balance-2026.json").readText()
        val ruleset = (BalanceRulesetCodec.parseAndValidate(raw) as BalanceLoadResult.Success).ruleset
        return BalanceRecruitingRecommendedParser.parse(ruleset.recruiting)
    }
}
