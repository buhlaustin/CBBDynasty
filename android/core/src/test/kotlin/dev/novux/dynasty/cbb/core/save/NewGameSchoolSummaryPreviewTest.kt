package dev.novux.dynasty.cbb.core.save

import dev.novux.dynasty.cbb.core.balance.BalanceLoadResult
import dev.novux.dynasty.cbb.core.balance.BalanceNilAnnualPoolParser
import dev.novux.dynasty.cbb.core.balance.BalanceRecruitingParser
import dev.novux.dynasty.cbb.core.balance.BalanceRulesetCodec
import dev.novux.dynasty.cbb.core.pack.TeamDisplayOverlay
import dev.novux.dynasty.cbb.core.qa.NewGameSchoolSummaryPreviewPath
import java.nio.file.Path
import kotlin.io.path.readText
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class NewGameSchoolSummaryPreviewTest {
    @Test
    fun megaRecr072_previewWeeklyAndNil_fromBalanceTables() {
        assertEquals(NewGameSchoolSummaryPreviewPath.RECR_ID, "MEGA-RECR-072")
        val ruleset = loadRuleset()
        val economy = BalanceRecruitingParser.parse(ruleset.recruiting)
        val nilTables = BalanceNilAnnualPoolParser.parse(ruleset.nil)
        val low = overlay(prestige = 25, conferenceId = "horizon")
        val elite = overlay(prestige = 94, conferenceId = "sec")
        val lowPreview = NewGameSchoolSummaryPreviewEngine.build(low, economy, nilTables)
        val elitePreview = NewGameSchoolSummaryPreviewEngine.build(elite, economy, nilTables)
        assertTrue(lowPreview.weeklyRecruitingPoints in 1_000..2_500)
        assertTrue(elitePreview.weeklyRecruitingPoints > lowPreview.weeklyRecruitingPoints)
        assertTrue(elitePreview.annualNilUsd > lowPreview.annualNilUsd)
        assertTrue(lowPreview.pipelineStates.contains("IN"))
    }

    private fun overlay(prestige: Int, conferenceId: String) = TeamDisplayOverlay(
        teamId = "t",
        conferenceId = conferenceId,
        displayName = "T",
        shortName = "T",
        homeCity = "Town",
        homeState = "IN",
        primaryColorHex = "#000",
        secondaryColorHex = "#fff",
        logoUri = null,
        postseasonEligible = true,
        programPrestige = prestige,
    )

    private fun loadRuleset() = {
        var dir = Path.of(System.getProperty("user.dir")).toAbsolutePath()
        while (dir != null && !dir.resolve("assets/balance/balance-2026.json").toFile().exists()) {
            dir = dir.parent
        }
        val raw = dir!!.resolve("assets/balance/balance-2026.json").readText()
        (BalanceRulesetCodec.parseAndValidate(raw) as BalanceLoadResult.Success).ruleset
    }.invoke()
}
