package dev.novux.dynasty.cbb.core.recruiting

import dev.novux.dynasty.cbb.core.balance.BalanceLoadResult
import dev.novux.dynasty.cbb.core.balance.BalanceRecruitingActionsParser
import dev.novux.dynasty.cbb.core.balance.BalanceRecruitingRecommendedParser
import dev.novux.dynasty.cbb.core.balance.BalanceRulesetCodec
import dev.novux.dynasty.cbb.core.qa.RecruitInterestTwoSurfacesPath
import dev.novux.dynasty.cbb.core.roster.PlayerPosition
import java.nio.file.Path
import kotlin.io.path.readText
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RecruitInterestTwoSurfacesTest {
    @Test
    fun megaRecr083_spend_movesLiveMeter_notSignLikelihood() {
        assertEquals(RecruitInterestTwoSurfacesPath.RECR_ID, "MEGA-RECR-083")
        val recommended = BalanceRecruitingRecommendedParser.parse(loadRecruitingJson())
        val actions = BalanceRecruitingActionsParser.parse(loadRecruitingJson())
        val prospect = RecruitProspect(
            prospectId = "p-nil",
            displayName = "Nil Test",
            stars = 4,
            position = PlayerPosition.ShootingGuard,
            homeState = "OH",
            nilAskUsd = 100_000,
            scoutLetterOverall = "B+",
        )
        val school = SchoolRecruitingContext(programPrestige = 60, schoolHomeState = "OH", nilPoolAnnualUsd = 2_000_000)
        val baseline = SignLikelihoodEngine.evaluate(prospect, school, recommended)
        val delta = RecruitInterestSpendDelta.forPointAction(RecruitingPointAction.Dm, actions, null)
        val afterSpend = SignLikelihoodEngine.evaluate(prospect, school, recommended, spendDelta = delta)
        assertEquals(baseline.baselineInterest, afterSpend.baselineInterest)
        assertEquals(baseline.badge, afterSpend.badge)
        assertEquals(baseline.adjustedScore, afterSpend.adjustedScore, 0.001)
        assertTrue(afterSpend.liveInterest > baseline.liveInterest)
    }

    private fun loadRuleset() = {
        var dir = Path.of(System.getProperty("user.dir")).toAbsolutePath()
        while (dir != null && !dir.resolve("assets/balance/balance-2026.json").toFile().exists()) {
            dir = dir.parent
        }
        val raw = dir!!.resolve("assets/balance/balance-2026.json").readText()
        (BalanceRulesetCodec.parseAndValidate(raw) as BalanceLoadResult.Success).ruleset
    }.invoke()

    private fun loadRecruitingJson() = loadRuleset().recruiting
}
