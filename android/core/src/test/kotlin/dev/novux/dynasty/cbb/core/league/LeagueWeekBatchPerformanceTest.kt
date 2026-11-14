package dev.novux.dynasty.cbb.core.league

import dev.novux.dynasty.cbb.core.pack.ConferenceDisplayOverlay
import dev.novux.dynasty.cbb.core.pack.TeamDisplayOverlay
import kotlin.system.measureNanoTime
import org.junit.Assert.assertTrue
import org.junit.Test

/** Full-league JVM perf gate [MEGA-QA-015]. */
class LeagueWeekBatchPerformanceTest {
    @Test
    fun fullLeague_regularSeasonWeek_underCoreBudget() {
        val schedule = buildFullLeagueSchedule()
        val userTeamId = "team-0"
        val prestige = schedule.schedules.associate { it.teamId to 55 }
        var standings = LeagueWeekBatchSimEngine.bootstrap(schedule.seasonIndex, prestige.keys)
        val warmSeed = 42L
        for (week in 1..4) {
            val warm = LeagueWeekBatchSimEngine.runRegularSeasonWeek(
                schedule = schedule,
                standings = standings,
                rsWeekIndex = week,
                userTeamId = userTeamId,
                userResult = null,
                seed = warmSeed + week,
            )
            standings = LeagueWeekBatchWeeklyRankings.attach(warm.standings, prestige, week)
        }
        val timingsMs = mutableListOf<Long>()
        repeat(LeagueWeekBatchPerformanceBudget.BENCHMARK_ITERATIONS) { iteration ->
            var scratch = standings
            val elapsedMs = measureNanoTime {
                val batch = LeagueWeekBatchSimEngine.runRegularSeasonWeek(
                    schedule = schedule,
                    standings = scratch,
                    rsWeekIndex = 5,
                    userTeamId = userTeamId,
                    userResult = null,
                    seed = warmSeed + 100 + iteration,
                )
                scratch = LeagueWeekBatchWeeklyRankings.attach(batch.standings, prestige, weekIndex = 5)
            } / 1_000_000
            timingsMs += elapsedMs
            standings = scratch
        }
        val medianMs = timingsMs.sorted()[timingsMs.size / 2]
        assertTrue(
            "365-team week batch median ${medianMs}ms (all $timingsMs ms) exceeds " +
                "${LeagueWeekBatchPerformanceBudget.MAX_CORE_REGULAR_SEASON_WEEK_MS}ms",
            medianMs <= LeagueWeekBatchPerformanceBudget.MAX_CORE_REGULAR_SEASON_WEEK_MS,
        )
    }

    private fun buildFullLeagueSchedule(): LeagueScheduleSeason {
        val conf = ConferenceDisplayOverlay(
            conferenceId = "conf-big",
            displayName = "Big",
            leagueGameCount = 18,
            tournamentTemplateId = "tpl",
        )
        val teams = List(LeagueWeekBatchPerformanceBudget.FULL_LEAGUE_TEAM_COUNT) { index ->
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
            dynastySeed = 99L,
        )
    }
}
