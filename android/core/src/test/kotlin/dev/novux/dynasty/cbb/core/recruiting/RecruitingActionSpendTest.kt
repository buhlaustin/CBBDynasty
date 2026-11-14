package dev.novux.dynasty.cbb.core.recruiting

import dev.novux.dynasty.cbb.core.balance.BalanceLoadResult
import dev.novux.dynasty.cbb.core.balance.BalanceRecruitingActionsParser
import dev.novux.dynasty.cbb.core.balance.BalanceRulesetCodec
import java.nio.file.Path
import kotlin.io.path.readText
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RecruitingActionSpendTest {
    private val actions = loadActions()

    @Test
    fun dm_and_call_costs_matchBalance() {
        assertEquals(50, RecruitingActionSpend.catalogCost(RecruitingPointAction.Dm, actions))
        assertEquals(100, RecruitingActionSpend.catalogCost(RecruitingPointAction.PhoneCall, actions))
        assertEquals(500, RecruitingActionSpend.catalogCost(RecruitingPointAction.SendTheHouse, actions))
        assertEquals(150, RecruitingActionSpend.catalogCost(RecruitingPointAction.ScholarshipOffer, actions))
    }

    @Test
    fun nonRefundedActions_reduceBalance() {
        val dm = RecruitingActionSpend.spend(RecruitingPointAction.Dm, pointsAvailable = 200, actions)
        assertTrue(dm.success)
        assertFalse(dm.countsAsWeekReservation)
        assertEquals(150, dm.pointsRemaining)
    }

    @Test
    fun offer_countsAsReservation_butChargesImmediately() {
        val offer = RecruitingActionSpend.spend(RecruitingPointAction.ScholarshipOffer, 200, actions)
        assertTrue(offer.success)
        assertTrue(offer.countsAsWeekReservation)
        assertEquals(50, offer.pointsRemaining)
    }

    @Test
    fun bonusPoints_spendPartialBalance() {
        val bonus = RecruitingActionSpend.spend(
            RecruitingPointAction.BonusPoints,
            pointsAvailable = 420,
            actions,
            bonusAmount = 175,
        )
        assertTrue(bonus.success)
        assertFalse(bonus.countsAsWeekReservation)
        assertEquals(245, bonus.pointsRemaining)
    }

    @Test
    fun insufficientPoints_fails() {
        val house = RecruitingActionSpend.spend(RecruitingPointAction.SendTheHouse, 400, actions)
        assertFalse(house.success)
        assertEquals(RecruitingSpendFailure.InsufficientPoints, house.failure)
    }

    @Test
    fun dm_atZeroPoints_failsInsufficient() {
        val dm = RecruitingActionSpend.spend(RecruitingPointAction.Dm, pointsAvailable = 0, actions)
        assertFalse(dm.success)
        assertEquals(RecruitingSpendFailure.InsufficientPoints, dm.failure)
        assertEquals(0, dm.pointsRemaining)
    }

    private fun loadActions(): dev.novux.dynasty.cbb.core.balance.BalanceRecruitingActions {
        var dir = Path.of(System.getProperty("user.dir")).toAbsolutePath()
        while (dir != null && !dir.resolve("assets/balance/balance-2026.json").toFile().exists()) {
            dir = dir.parent
        }
        val raw = dir!!.resolve("assets/balance/balance-2026.json").readText()
        val ruleset = (BalanceRulesetCodec.parseAndValidate(raw) as BalanceLoadResult.Success).ruleset
        return BalanceRecruitingActionsParser.parse(ruleset.recruiting)
    }
}
