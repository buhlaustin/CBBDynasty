package dev.novux.dynasty.cbb.core.recruiting

import dev.novux.dynasty.cbb.core.balance.BalanceLoadResult
import dev.novux.dynasty.cbb.core.balance.BalanceRecruitingParser
import dev.novux.dynasty.cbb.core.balance.BalanceRulesetCodec
import dev.novux.dynasty.cbb.core.calendar.CalendarPhase
import java.nio.file.Path
import kotlin.io.path.readText
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RecruitingWeekGrantTest {
    private fun economy() = loadEconomy()

    @Test
    fun prestigeMapsToTierWeeklyPool() {
        val economy = economy()
        val pool = RecruitingPrestigePool.weeklyPoolPoints(economy, programPrestige = 40)
        assertTrue(pool in 1500..2000)
    }

    @Test
    fun prestige_lowMajorVsBlueBlood_weeklyPoolMatchesSchoolTier() {
        val economy = economy()
        val lowMajor = RecruitingPrestigePool.weeklyPoolPoints(economy, programPrestige = 20)
        val blueBlood = RecruitingPrestigePool.weeklyPoolPoints(economy, programPrestige = 92)
        assertTrue(lowMajor in 1000..1250)
        assertTrue(blueBlood in 4500..5000)
        assertTrue(blueBlood > lowMajor + 2000)
        val lowLedger = RecruitingWeekGrant.openCycle(economy, cycleSeasonIndex = 1, programPrestige = 20)
        val eliteLedger = RecruitingWeekGrant.openCycle(economy, cycleSeasonIndex = 1, programPrestige = 92)
        assertEquals(lowMajor, lowLedger.weeklyPoolBase)
        assertEquals(blueBlood, eliteLedger.weeklyPoolBase)
        assertEquals(20, lowLedger.programPrestige)
        assertEquals(92, eliteLedger.programPrestige)
    }

    @Test
    fun carryover_isHalfOfUnspent() {
        val economy = economy()
        var ledger = RecruitingWeekGrant.openCycle(economy, cycleSeasonIndex = 1, programPrestige = 40)
        ledger = ledger.withPointsAvailable(1000)
        val (next, breakdown) = RecruitingWeekGrant.applyWeekAdvanceGrant(
            economy,
            ledger,
            phase = CalendarPhase.RegularSeason,
            offseasonWeekIndex = null,
            pointsSpentThisWeek = 0,
        )
        assertEquals(500, breakdown.carryoverFromPrior)
        assertEquals(ledger.weeklyPoolBase + 500, next.pointsAvailable)
    }

    @Test
    fun offseasonWeek3_appliesCrunchMultiplier() {
        val economy = economy()
        val ledger = RecruitingWeekGrant.openCycle(economy, 1, 40)
        val (_, breakdown) = RecruitingWeekGrant.applyWeekAdvanceGrant(
            economy,
            ledger,
            phase = CalendarPhase.Offseason,
            offseasonWeekIndex = 3,
            pointsSpentThisWeek = 0,
        )
        assertEquals(1.5, breakdown.crunchMultiplier, 0.001)
        assertEquals((ledger.weeklyPoolBase * 1.5).toInt(), breakdown.grantedThisWeek)
    }

    private fun loadEconomy(): dev.novux.dynasty.cbb.core.balance.BalanceRecruitingEconomy {
        var dir = Path.of(System.getProperty("user.dir")).toAbsolutePath()
        while (dir != null && !dir.resolve("assets/balance/balance-2026.json").toFile().exists()) {
            dir = dir.parent
        }
        val raw = dir!!.resolve("assets/balance/balance-2026.json").readText()
        val ruleset = (BalanceRulesetCodec.parseAndValidate(raw) as BalanceLoadResult.Success).ruleset
        return BalanceRecruitingParser.parse(ruleset.recruiting)
    }
}
