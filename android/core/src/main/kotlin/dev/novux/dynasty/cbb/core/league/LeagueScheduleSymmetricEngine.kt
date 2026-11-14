package dev.novux.dynasty.cbb.core.league

import dev.novux.dynasty.cbb.core.pack.ConferenceDisplayOverlay
import dev.novux.dynasty.cbb.core.pack.TeamDisplayOverlay
import dev.novux.dynasty.cbb.core.preseason.tournament.RegularSeasonSlotBudget
import dev.novux.dynasty.cbb.core.schedule.ScheduleGameKind

/** Mutual-opponent conference + non-conf pairing with week collision avoidance ([MEGA-NOTES-047]). */
object LeagueScheduleSymmetricEngine {
    const val MEGA_ID: String = "MEGA-NOTES-047"

    private data class DirectedMatchup(
        val homeTeamId: String,
        val awayTeamId: String,
        val kind: ScheduleGameKind,
    )

    fun generate(
        teams: List<TeamDisplayOverlay>,
        conferences: Map<String, ConferenceDisplayOverlay>,
        seasonIndex: Int,
        dynastySeed: Long,
    ): LeagueScheduleSeason {
        val byConference = teams.groupBy { it.conferenceId }
        val matchups = mutableListOf<DirectedMatchup>()
        byConference.forEach { (confId, confTeams) ->
            val conference = conferences[confId] ?: return@forEach
            val gamesPerTeam = conference.leagueGameCount.coerceAtMost(RegularSeasonSlotBudget.RS_CAP)
            matchups += conferenceMatchups(confTeams, gamesPerTeam, dynastySeed xor confId.hashCode().toLong())
        }

        val teamIds = teams.map { it.teamId }.toSet()
        val gamesPerTeam = matchups.groupingBy { it.homeTeamId }.eachCount().toMutableMap()
        matchups.forEach { gamesPerTeam[it.awayTeamId] = gamesPerTeam.getOrDefault(it.awayTeamId, 0) + 1 }

        val nonConfNeed = teams.associate { team ->
            val confGames = gamesPerTeam.getOrDefault(team.teamId, 0)
            team.teamId to (RegularSeasonSlotBudget.RS_CAP - confGames).coerceAtLeast(0)
        }
        matchups += nonConferenceFill(teams, nonConfNeed, dynastySeed)

        val capped = capMatchupsPerTeam(matchups, RegularSeasonSlotBudget.RS_CAP)
        val withWeeks = assignWeeks(capped)
        val schedules = teamIds.associateWith { mutableListOf<LeagueGameStub>() }.toMutableMap()
        withWeeks.forEach { (matchup, week) ->
            schedules.getValue(matchup.homeTeamId) +=
                LeagueGameStub(matchup.kind, matchup.awayTeamId, homeGame = true, rsWeekIndex = week)
            schedules.getValue(matchup.awayTeamId) +=
                LeagueGameStub(matchup.kind, matchup.homeTeamId, homeGame = false, rsWeekIndex = week)
        }

        val leagueSchedules = teams.map { team ->
            LeagueTeamSchedule(
                teamId = team.teamId,
                conferenceId = team.conferenceId,
                games = schedules[team.teamId].orEmpty().sortedBy { it.rsWeekIndex },
            )
        }
        return LeagueScheduleSeason(
            seasonIndex = seasonIndex,
            teamsScheduled = leagueSchedules.size,
            schedules = leagueSchedules,
        )
    }

    private fun conferenceMatchups(
        teams: List<TeamDisplayOverlay>,
        gamesPerTeam: Int,
        seed: Long,
    ): List<DirectedMatchup> {
        val ids = teams.map { it.teamId }
        if (ids.size < 2 || gamesPerTeam <= 0) return emptyList()
        val counts = ids.associateWith { 0 }.toMutableMap()
        val matchups = mutableListOf<DirectedMatchup>()
        var round = 0
        val maxRounds = gamesPerTeam * 2
        while (counts.values.any { it < gamesPerTeam } && round < maxRounds) {
            val ring = rotate(ids, round)
            for (i in 0 until ring.size / 2) {
                val a = ring[i]
                val b = ring[ring.lastIndex - i]
                if (a == b) continue
                if (counts.getValue(a) >= gamesPerTeam || counts.getValue(b) >= gamesPerTeam) continue
                val homeFirst = ((round + i + seed.toInt()) % 2) == 0
                val home = if (homeFirst) a else b
                val away = if (home == a) b else a
                matchups += DirectedMatchup(home, away, ScheduleGameKind.Conference)
                counts[a] = counts.getValue(a) + 1
                counts[b] = counts.getValue(b) + 1
            }
            round++
        }
        return matchups
    }

    private fun nonConferenceFill(
        teams: List<TeamDisplayOverlay>,
        slotsRemaining: Map<String, Int>,
        seed: Long,
    ): List<DirectedMatchup> {
        val matchups = mutableListOf<DirectedMatchup>()
        val remaining = slotsRemaining.toMutableMap()
        val ordered = teams.sortedBy { it.teamId }
        var cursor = 0
        repeat(ordered.size * RegularSeasonSlotBudget.RS_CAP) {
            val needing = ordered.filter { remaining.getOrDefault(it.teamId, 0) > 0 }
            if (needing.size < 2) return@repeat
            val home = needing[cursor % needing.size]
            val away = needing[(cursor + 1 + seed.toInt()) % needing.size]
            cursor++
            if (home.teamId == away.teamId) return@repeat
            if (home.conferenceId == away.conferenceId) return@repeat
            if (remaining.getOrDefault(home.teamId, 0) <= 0 || remaining.getOrDefault(away.teamId, 0) <= 0) {
                return@repeat
            }
            val homeCourt = ((home.teamId.hashCode() + away.teamId.hashCode()) % 2) == 0
            val matchup = if (homeCourt) {
                DirectedMatchup(home.teamId, away.teamId, ScheduleGameKind.NonConference)
            } else {
                DirectedMatchup(away.teamId, home.teamId, ScheduleGameKind.NonConference)
            }
            matchups += matchup
            remaining[matchup.homeTeamId] = remaining.getOrDefault(matchup.homeTeamId, 0) - 1
            remaining[matchup.awayTeamId] = remaining.getOrDefault(matchup.awayTeamId, 0) - 1
        }
        return matchups
    }

    private fun capMatchupsPerTeam(
        matchups: List<DirectedMatchup>,
        cap: Int,
    ): List<DirectedMatchup> {
        val counts = mutableMapOf<String, Int>()
        return matchups.filter { matchup ->
            val homeCount = counts.getOrDefault(matchup.homeTeamId, 0)
            val awayCount = counts.getOrDefault(matchup.awayTeamId, 0)
            if (homeCount >= cap || awayCount >= cap) {
                false
            } else {
                counts[matchup.homeTeamId] = homeCount + 1
                counts[matchup.awayTeamId] = awayCount + 1
                true
            }
        }
    }

    private fun assignWeeks(matchups: List<DirectedMatchup>): List<Pair<DirectedMatchup, Int>> {
        val teamWeeks = mutableMapOf<String, MutableSet<Int>>()
        val assigned = mutableListOf<Pair<DirectedMatchup, Int>>()
        matchups.forEach { matchup ->
            val start = ((matchup.homeTeamId.hashCode() xor matchup.awayTeamId.hashCode()) and Int.MAX_VALUE) % 32
            val week = (0 until RegularSeasonSlotBudget.RS_CAP).firstNotNullOfOrNull { offset ->
                val candidate = ((start + offset) % RegularSeasonSlotBudget.RS_CAP) + 1
                candidate.takeIf {
                    it !in teamWeeks.getOrDefault(matchup.homeTeamId, emptySet()) &&
                        it !in teamWeeks.getOrDefault(matchup.awayTeamId, emptySet())
                }
            } ?: return@forEach
            teamWeeks.getOrPut(matchup.homeTeamId) { mutableSetOf() }.add(week)
            teamWeeks.getOrPut(matchup.awayTeamId) { mutableSetOf() }.add(week)
            assigned += matchup to week
        }
        return assigned
    }

    private fun rotate(ids: List<String>, round: Int): List<String> {
        if (ids.size <= 1) return ids
        val fixed = ids.first()
        val rest = ids.drop(1).toMutableList()
        repeat(round % rest.size.coerceAtLeast(1)) {
            val last = rest.removeAt(rest.lastIndex)
            rest.add(0, last)
        }
        return listOf(fixed) + rest
    }
}
