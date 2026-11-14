package dev.novux.dynasty.cbb.core.league

import dev.novux.dynasty.cbb.core.sim.GameSimInput
import dev.novux.dynasty.cbb.core.sim.GameSimResult
import dev.novux.dynasty.cbb.core.sim.GameSimTeamInput
import dev.novux.dynasty.cbb.core.sim.PossessionSimEngine
import dev.novux.dynasty.cbb.core.sim.SimHomeVenueType
import dev.novux.dynasty.cbb.core.sim.tuning.SimTuning

/** Fast batch sim using possession engine when tuning is supplied ([planning/simulation-engine.md]). */
object LeagueFastSim {
    data class SimulatedMatchup(
        val league: LeagueGameResult,
        val sim: GameSimResult,
    )

    fun simGame(matchup: LeagueMatchup, seed: Long, tuning: SimTuning? = null): LeagueGameResult =
        if (tuning != null) {
            simulatePossession(matchup, seed, tuning, prestige = null).league
        } else {
            randomScoreStub(matchup, seed)
        }

    fun simulateWithBatch(
        matchup: LeagueMatchup,
        seed: Long,
        batch: LeagueBatchSimParams,
    ): SimulatedMatchup = simulatePossession(
        matchup = matchup,
        seed = seed,
        tuning = batch.tuning,
        prestige = batch,
    )

    private fun simulatePossession(
        matchup: LeagueMatchup,
        seed: Long,
        tuning: SimTuning,
        prestige: LeagueBatchSimParams?,
    ): SimulatedMatchup {
        val mixSeed = seed xor matchup.homeTeamId.hashCode().toLong() xor matchup.awayTeamId.hashCode().toLong()
        val (homeOff, homeDef) = ratingsFor(matchup.homeTeamId, prestige, defaultOff = DEFAULT_RATING, defaultDef = DEFAULT_RATING)
        val (awayOff, awayDef) = ratingsFor(matchup.awayTeamId, prestige, defaultOff = DEFAULT_RATING, defaultDef = DEFAULT_RATING)
        val sim = PossessionSimEngine.simulate(
            GameSimInput(
                home = GameSimTeamInput(matchup.homeTeamId, homeOff, homeDef),
                away = GameSimTeamInput(matchup.awayTeamId, awayOff, awayDef),
                tuning = tuning,
                seed = mixSeed,
                homeVenue = SimHomeVenueType.Home,
            ),
        )
        var home = sim.homeScore
        var away = sim.awayScore
        if (home == away && !sim.isComplete) {
            home += 1
        }
        return SimulatedMatchup(
            league = LeagueGameResult(
                homeTeamId = matchup.homeTeamId,
                awayTeamId = matchup.awayTeamId,
                homeScore = home,
                awayScore = away,
                rsWeekIndex = matchup.rsWeekIndex,
            ),
            sim = sim,
        )
    }

    private fun ratingsFor(
        teamId: String,
        batch: LeagueBatchSimParams?,
        defaultOff: Int,
        defaultDef: Int,
    ): Pair<Int, Int> = if (batch != null) {
        TeamSimRatings.fromProgramPrestige(batch.prestigeFor(teamId))
    } else {
        defaultOff to defaultDef
    }

    private fun randomScoreStub(matchup: LeagueMatchup, seed: Long): LeagueGameResult {
        val random = kotlin.random.Random(
            seed xor matchup.homeTeamId.hashCode().toLong() xor matchup.awayTeamId.hashCode().toLong(),
        )
        var home = 58 + random.nextInt(40)
        var away = 58 + random.nextInt(40)
        if (home == away) {
            home += if (random.nextBoolean()) 1 else -1
        }
        return LeagueGameResult(
            homeTeamId = matchup.homeTeamId,
            awayTeamId = matchup.awayTeamId,
            homeScore = home.coerceAtLeast(0),
            awayScore = away.coerceAtLeast(0),
            rsWeekIndex = matchup.rsWeekIndex,
        )
    }

    private const val DEFAULT_RATING = 72
}
