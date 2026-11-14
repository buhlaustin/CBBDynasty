package dev.novux.dynasty.cbb.core.recruiting

import dev.novux.dynasty.cbb.core.balance.BalanceLoadResult
import dev.novux.dynasty.cbb.core.balance.BalanceRecruitingActionsParser
import dev.novux.dynasty.cbb.core.balance.BalanceRulesetCodec
import java.nio.file.Path
import kotlin.io.path.readText
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RecruitInterestSpendDeltaTest {
  private val actions = loadActions()

    @Test
    fun dm_phone_house_deltas_from_balance() {
        assertTrue(RecruitInterestSpendDelta.forPointAction(RecruitingPointAction.Dm, actions, null) > 0)
        assertTrue(
            RecruitInterestSpendDelta.forPointAction(RecruitingPointAction.SendTheHouse, actions, null) >
                RecruitInterestSpendDelta.forPointAction(RecruitingPointAction.Dm, actions, null),
        )
    }

    @Test
    fun spendDelta_moves_live_interest_not_badge_baseline() {
        val recommended = dev.novux.dynasty.cbb.core.balance.BalanceRecruitingRecommendedParser
            .parse(loadRecruitingJson())
        val prospect = RecruitProspect(
            prospectId = "p1",
            displayName = "Test",
            stars = 3,
            position = dev.novux.dynasty.cbb.core.roster.PlayerPosition.PointGuard,
            homeState = "OH",
            nilAskUsd = 40_000,
            scoutLetterOverall = "B",
        )
        val school = SchoolRecruitingContext(programPrestige = 55, schoolHomeState = "OH", nilPoolAnnualUsd = 900_000)
        val zero = SignLikelihoodEngine.evaluate(prospect, school, recommended, spendDelta = 0)
        val delta = RecruitInterestSpendDelta.forPointAction(RecruitingPointAction.Dm, actions, null)
        val after = SignLikelihoodEngine.evaluate(prospect, school, recommended, spendDelta = delta)
        assertEquals(zero.baselineInterest, after.baselineInterest)
        assertEquals(zero.adjustedScore, after.adjustedScore, 0.001)
        assertTrue(after.liveInterest > zero.liveInterest)
    }

    private fun loadActions() = BalanceRecruitingActionsParser.parse(loadRecruitingJson())

    private fun loadRecruitingJson(): kotlinx.serialization.json.JsonElement {
        var dir = Path.of(System.getProperty("user.dir")).toAbsolutePath()
        while (dir != null && !dir.resolve("assets/balance/balance-2026.json").toFile().exists()) {
            dir = dir.parent
        }
        val raw = dir!!.resolve("assets/balance/balance-2026.json").readText()
        val ruleset = (BalanceRulesetCodec.parseAndValidate(raw) as BalanceLoadResult.Success).ruleset
        return ruleset.recruiting
    }
}
