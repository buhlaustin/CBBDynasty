package dev.novux.dynasty.cbb.core.league

import dev.novux.dynasty.cbb.core.pack.ConferenceDisplayOverlay
import dev.novux.dynasty.cbb.core.pack.TeamDisplayOverlay
import kotlin.system.measureNanoTime
import org.junit.Assert.assertTrue
import org.junit.Test

/** Product-band smoke for Sim Week compute ([MEGA-QA-014]); full league: [LeagueWeekBatchPerformanceTest]. */
class LeagueWeekBatchProductBudgetTest {
    @Test
    fun scaledLeague_regularSeasonWeek_underProductSmokeBudget() {
        val teamCount = LeagueWeekBatchPerformanceBudget.PRODUCT_SMOKE_TEAM_COUNT
        val schedule = buildSchedule(teamCount)
        val userTeamId = schedule.schedules.first().teamId
        val prestige = schedule.schedules.associate { it.teamId to 55 }
        var standings = LeagueWeekBatchSimEngine.bootstrap(schedule.seasonIndex, prestige.keys)
        val timingsMs = mutableListOf<Long>()
        repeat(LeagueWeekBatchPerformanceBudget.BENCHMARK_ITERATIONS) { iteration ->
            val elapsedMs = measureNanoTime {
                val batch = LeagueWeekBatchSimEngine.runRegularSeasonWeek(
                    schedule = schedule,
                    standings = standings,
                    rsWeekIndex = 1,
                    userTeamId = userTeamId,
                    userResult = null,
                    seed = 7L + iteration,
                )
                standings = LeagueWeekBatchWeeklyRankings.attach(batch.standings, prestige, weekIndex = 1)
            } / 1_000_000
            timingsMs += elapsedMs
        }
        val medianMs = timingsMs.sorted()[timingsMs.size / 2]
        assertTrue(
            "scaled RS week median ${medianMs}ms exceeds product smoke " +
                "${LeagueWeekBatchPerformanceBudget.MAX_PRODUCT_SMOKE_REGULAR_SEASON_WEEK_MS}ms",
            medianMs <= LeagueWeekBatchPerformanceBudget.MAX_PRODUCT_SMOKE_REGULAR_SEASON_WEEK_MS,
        )
    }

    private fun buildSchedule(teamCount: Int): LeagueScheduleSeason {
        val conf = ConferenceDisplayOverlay(
            conferenceId = "conf-smoke",
            displayName = "Smoke",
            leagueGameCount = 18,
            tournamentTemplateId = "tpl",
        )
        val teams = List(teamCount) { index ->
            TeamDisplayOverlay(
                teamId = "team-$index",
                conferenceId = conf.conferenceId,
                displayName = "Team $index",
                shortName = "T$index",
                homeCity = "City",
                homeState = "ST",
                primaryColorHex = "#000000",
                secondaryColorHex = "#FFFFFF",
                logoUri = null,
                postseasonEligible = true,
                programPrestige = 50 + (index % 50),
            )
        }
        return LeagueScheduleGenerator.generate(
            teams = teams,
            conferences = mapOf(conf.conferenceId to conf),
            seasonIndex = 1,
            dynastySeed = 11L,
        )
    }
}
