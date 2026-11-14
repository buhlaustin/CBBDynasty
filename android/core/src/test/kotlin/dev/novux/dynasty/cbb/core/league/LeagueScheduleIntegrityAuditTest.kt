package dev.novux.dynasty.cbb.core.league

import dev.novux.dynasty.cbb.core.pack.ConferenceDisplayOverlay
import dev.novux.dynasty.cbb.core.pack.TeamDisplayOverlay
import org.junit.Assert.assertTrue
import org.junit.Test

class LeagueScheduleIntegrityAuditTest {
    @Test
    fun symmetricEnginePassesScratchAudit() {
        val conf = ConferenceDisplayOverlay("conf", "Conf", 18, "tpl")
        val conferences = mapOf(conf.conferenceId to conf)
        val teams = List(12) { index ->
            TeamDisplayOverlay(
                teamId = "t$index",
                conferenceId = conf.conferenceId,
                displayName = "T$index",
                shortName = "T$index",
                homeCity = "City",
                homeState = "ST",
                primaryColorHex = "#000",
                secondaryColorHex = "#FFF",
                logoUri = null,
                postseasonEligible = true,
                programPrestige = 50,
            )
        }
        val season = LeagueScheduleSymmetricEngine.generate(teams, conferences, 1, 77)
        val report = LeagueScheduleIntegrityAudit.audit(season)
        assertTrue(report.isClean)
    }
}
