package dev.novux.dynasty.cbb.core.recruiting

import dev.novux.dynasty.cbb.core.balance.BalanceLoadResult
import dev.novux.dynasty.cbb.core.balance.BalanceRecruitingActionsParser
import dev.novux.dynasty.cbb.core.balance.BalanceRulesetCodec
import dev.novux.dynasty.cbb.core.calendar.CalendarPhase
import java.nio.file.Path
import kotlin.io.path.readText
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RecruitingHubContactActionGateTest {
    private val actions = loadActions()

  @Test
    fun dm_allowed_whenPointsSufficient() {
        val gates = RecruitingHubActionGateEvaluator.forProspect(
            prospectId = "p1",
            prospectHomeState = "OH",
            phase = CalendarPhase.RegularSeason,
            offseasonWeekIndex = null,
            pointsAvailable = 200,
            actions = actions,
            school = SchoolRecruitingContext(
                programPrestige = 40,
                schoolHomeState = "OH",
                nilPoolAnnualUsd = 900_000,
            ),
            consumption = RecruitingSeasonConsumption(1),
            activeReservations = emptyList(),
        )
        assertTrue(gates.dm.allowed)
        assertTrue(gates.phoneCall.allowed)
        assertFalse(gates.sendTheHouse.allowed)
        assertTrue(gates.bonusPoints.allowed)
        assertEquals(200, gates.bonusPoints.pointCost)
    }

    @Test
    fun dm_blocked_whenZeroPoints() {
        val gates = RecruitingHubActionGateEvaluator.forProspect(
            prospectId = "p1",
            prospectHomeState = "OH",
            phase = CalendarPhase.RegularSeason,
            offseasonWeekIndex = null,
            pointsAvailable = 0,
            actions = actions,
            school = SchoolRecruitingContext(
                programPrestige = 40,
                schoolHomeState = "OH",
                nilPoolAnnualUsd = 900_000,
            ),
            consumption = RecruitingSeasonConsumption(1),
            activeReservations = emptyList(),
        )
        assertFalse(gates.dm.allowed)
        assertEquals(RecruitingActionBlockReason.InsufficientPoints, gates.dm.blockReason)
        assertFalse(gates.phoneCall.allowed)
        assertEquals(RecruitingActionBlockReason.InsufficientPoints, gates.phoneCall.blockReason)
    }

    @Test
    fun scholarshipBlocked_whenOfferReservationActive() {
        val active = listOf(
            RecruitingWeekReservation("1", "p1", RecruitingRefundableAction.ScholarshipOffer, 150),
        )
        val gates = RecruitingHubActionGateEvaluator.forProspect(
            prospectId = "p1",
            prospectHomeState = "OH",
            phase = CalendarPhase.RegularSeason,
            offseasonWeekIndex = null,
            pointsAvailable = 400,
            actions = actions,
            school = SchoolRecruitingContext(
                programPrestige = 40,
                schoolHomeState = "OH",
                nilPoolAnnualUsd = 900_000,
            ),
            consumption = RecruitingSeasonConsumption(1),
            activeReservations = active,
        )
        assertTrue(gates.hasActiveScholarshipOffer)
        assertFalse(gates.scholarshipOffer.allowed)
    }

    private fun loadActions() =
        BalanceRecruitingActionsParser.parse(loadRuleset().recruiting)

    private fun loadRuleset() =
        (BalanceRulesetCodec.parseAndValidate(loadBalanceJson()) as BalanceLoadResult.Success).ruleset

    private fun loadBalanceJson(): String {
        var dir = Path.of(System.getProperty("user.dir")).toAbsolutePath()
        while (dir != null && !dir.resolve("assets/balance/balance-2026.json").toFile().exists()) {
            dir = dir.parent
        }
        return dir!!.resolve("assets/balance/balance-2026.json").readText()
    }
}
