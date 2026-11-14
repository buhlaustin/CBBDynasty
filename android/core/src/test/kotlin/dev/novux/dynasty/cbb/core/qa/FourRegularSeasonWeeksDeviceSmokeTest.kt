package dev.novux.dynasty.cbb.core.qa

import dev.novux.dynasty.cbb.core.calendar.CalendarPhase
import dev.novux.dynasty.cbb.core.calendar.checkpoint.WeekCheckpointState
import dev.novux.dynasty.cbb.core.league.LeagueScheduleGenerator
import dev.novux.dynasty.cbb.core.league.LeagueWeekBatchSimEngine
import dev.novux.dynasty.cbb.core.league.LeagueWeekBatchWeeklyRankings
import dev.novux.dynasty.cbb.core.pack.ConferenceDisplayOverlay
import dev.novux.dynasty.cbb.core.pack.TeamDisplayOverlay
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * JVM substitute for on-device four RS sim weeks ([MEGA-QA-127]); manual device gate
 * is documented in [docs/PHYSICAL_DEVICE_SMOKE.md].
 */
class FourRegularSeasonWeeksDeviceSmokeTest {
    @Test
    fun fourConsecutiveRegularSeasonAdvances_withLeagueBatch() {
        var checkpoint = WeekCheckpointState.bootstrap(1, CalendarPhase.RegularSeason)
        val schedule = buildMiniSchedule(teamCount = 32)
        val userTeamId = schedule.schedules.first().teamId
        val prestige = schedule.schedules.associate { it.teamId to 50 }
        var standings = LeagueWeekBatchSimEngine.bootstrap(schedule.seasonIndex, prestige.keys)

        val startWeek = checkpoint.activePosition.weekIndex
        repeat(PhysicalDeviceSmokePath.REGULAR_SEASON_SIM_WEEKS) { iteration ->
            val weekIndex = checkpoint.activePosition.weekIndex
            assertEquals(CalendarPhase.RegularSeason, checkpoint.activePosition.phase)
            val batch = LeagueWeekBatchSimEngine.runRegularSeasonWeek(
                schedule = schedule,
                standings = standings,
                rsWeekIndex = weekIndex,
                userTeamId = userTeamId,
                userResult = null,
                seed = 127L + iteration,
            )
            standings = LeagueWeekBatchWeeklyRankings.attach(batch.standings, prestige, weekIndex)
            checkpoint = checkpoint.advanceWeek().nextState
        }

        assertEquals(
            startWeek + PhysicalDeviceSmokePath.REGULAR_SEASON_SIM_WEEKS,
            checkpoint.activePosition.weekIndex,
        )
        assertEquals(PhysicalDeviceSmokePath.FINAL_PHASE, checkpoint.activePosition.phase)
        assertTrue(checkpoint.provisionalGames.isEmpty())
    }

    private fun buildMiniSchedule(teamCount: Int) =
        LeagueScheduleGenerator.generate(
            teams = List(teamCount) { index ->
                TeamDisplayOverlay(
                    teamId = "team-$index",
                    conferenceId = "conf-device",
                    displayName = "Team $index",
                    shortName = "T$index",
                    homeCity = "City",
                    homeState = "ST",
                    primaryColorHex = "#101010",
                    secondaryColorHex = "#EEEEEE",
                    logoUri = null,
                    postseasonEligible = true,
                    programPrestige = 50,
                )
            },
            conferences = mapOf(
                "conf-device" to ConferenceDisplayOverlay(
                    conferenceId = "conf-device",
                    displayName = "Device",
                    leagueGameCount = 18,
                    tournamentTemplateId = "tpl",
                ),
            ),
            seasonIndex = 1,
            dynastySeed = 127L,
        )
}
