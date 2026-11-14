package dev.novux.dynasty.cbb.core.league

import dev.novux.dynasty.cbb.core.pack.ConferenceDisplayOverlay
import dev.novux.dynasty.cbb.core.pack.TeamDisplayOverlay
import dev.novux.dynasty.cbb.core.preseason.tournament.RegularSeasonSlotBudget
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class LeagueScheduleGeneratorTest {
    @Test
    fun schedulesEveryTeamInLeague() {
        val confA = ConferenceDisplayOverlay("conf-a", "A", 10, "tpl")
        val confB = ConferenceDisplayOverlay("conf-b", "B", 10, "tpl")
        val conferences = mapOf(confA.conferenceId to confA, confB.conferenceId to confB)
        val teams = buildList {
            repeat(3) { i ->
                add(team("a$i", "conf-a"))
            }
            repeat(4) { i ->
                add(team("b$i", "conf-b"))
            }
        }
        val season = LeagueScheduleGenerator.generate(teams, conferences, seasonIndex = 1, dynastySeed = 99)
        assertEquals(7, season.teamsScheduled)
        assertEquals(7, season.schedules.size)
        season.schedules.forEach { schedule ->
            assertTrue(schedule.rsGameCount <= RegularSeasonSlotBudget.RS_CAP)
            assertTrue(schedule.rsGameCount >= 8)
        }
        val audit = LeagueScheduleIntegrityAudit.audit(season)
        assertTrue(audit.isClean)
    }

  @Test
  fun scalesToFullLeagueCount() {
        val conf = ConferenceDisplayOverlay("conf-big", "Big", 18, "tpl")
        val conferences = mapOf(conf.conferenceId to conf)
        val teams = List(365) { index ->
            team("team-$index", conf.conferenceId)
        }
        val season = LeagueScheduleGenerator.generate(teams, conferences, 1, 42)
        assertEquals(365, season.teamsScheduled)
    }

    private fun team(id: String, conferenceId: String): TeamDisplayOverlay =
        TeamDisplayOverlay(
            teamId = id,
            conferenceId = conferenceId,
            displayName = id,
            shortName = id,
            homeCity = "City",
            homeState = "ST",
            primaryColorHex = "#000",
            secondaryColorHex = "#FFF",
            logoUri = null,
            postseasonEligible = true,
            programPrestige = 55,
        )
}
