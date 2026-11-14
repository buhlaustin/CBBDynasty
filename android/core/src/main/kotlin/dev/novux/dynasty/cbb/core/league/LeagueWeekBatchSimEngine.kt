package dev.novux.dynasty.cbb.core.league

import dev.novux.dynasty.cbb.core.calendar.CalendarPhase

object LeagueWeekBatchSimEngine {
    fun bootstrap(seasonIndex: Int, teamIds: Collection<String>): LeagueStandingsState =
        LeagueStandingsState(
            seasonIndex = seasonIndex,
            records = teamIds.associateWith { LeagueTeamRecord(teamId = it) },
        )

    fun runRegularSeasonWeek(
        schedule: LeagueScheduleSeason,
        standings: LeagueStandingsState,
        rsWeekIndex: Int,
        userTeamId: String,
        userResult: LeagueGameResult?,
        seed: Long,
        batch: LeagueBatchSimParams? = null,
    ): LeagueWeekBatchSimResult {
        var state = standings.copy(lastWeekResults = emptyList(), aiInjuryRollsLastWeek = 0)
        val matchups = LeagueMatchupIndex.uniqueRsMatchups(schedule, rsWeekIndex)
        var simmed = 0
        val injuryRolls = mutableListOf<AiLeagueWeekOps.GameInjuryRoll>()
        val injuryParams = batch?.let {
            AiLeagueWeekOps.InjuryRollParams(
                phase = it.phase,
                bank = it.injuryBank,
                occurrence = it.injuryOccurrence,
                medicalStaffGrade = it.medicalStaffGrade,
            )
        }
        matchups.forEach { matchup ->
            if (matchup.homeTeamId == userTeamId || matchup.awayTeamId == userTeamId) {
                return@forEach
            }
            val gameSeed = seed + simmed
            val result = if (batch != null) {
                val simulated = LeagueFastSim.simulateWithBatch(matchup, gameSeed, batch)
                injuryParams?.let { params ->
                    injuryRolls += AiLeagueWeekOps.rollInjuriesForCompletedGame(
                        homeTeamId = matchup.homeTeamId,
                        awayTeamId = matchup.awayTeamId,
                        sim = simulated.sim,
                        gameSeed = gameSeed,
                        homePrestige = batch.prestigeFor(matchup.homeTeamId),
                        awayPrestige = batch.prestigeFor(matchup.awayTeamId),
                        params = params,
                    )
                }
                simulated.league
            } else {
                LeagueFastSim.simGame(matchup, gameSeed)
            }
            state = state.withResult(result)
            simmed++
        }
        userResult?.let { state = state.withResult(it) }
        val injuries = if (batch != null) {
            injuryRolls.sumOf { it.totalEvents }
        } else {
            AiLeagueWeekOps.rollInjuriesForGames(simmed + (if (userResult != null) 1 else 0), seed)
        }
        if (batch != null) {
            AiLeagueWeekOps.lineupAdjustTeamsAffected(injuryRolls)
        } else {
            AiLeagueWeekOps.lineupAdjustTeamsAffected(injuries)
        }
        return LeagueWeekBatchSimResult(
            standings = state.copy(
                lastCommittedRsWeek = rsWeekIndex,
                aiInjuryRollsLastWeek = injuries,
            ),
            gamesSimmed = simmed,
            aiInjuryRolls = injuries,
        )
    }

    fun phaseSupportsBatch(phase: CalendarPhase): Boolean =
        phase == CalendarPhase.RegularSeason || phase == CalendarPhase.Postseason
}
