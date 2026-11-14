package dev.novux.dynasty.cbb.core.sim

import dev.novux.dynasty.cbb.core.roster.PlayerClassYear
import dev.novux.dynasty.cbb.core.roster.PlayerPosition
import dev.novux.dynasty.cbb.core.roster.RosterPlayer
import dev.novux.dynasty.cbb.core.sim.tuning.SimTuning
import dev.novux.dynasty.cbb.core.sim.tuning.SimShotClock
import dev.novux.dynasty.cbb.core.sim.tuning.SimTimeUsedSeconds
import dev.novux.dynasty.cbb.core.sim.tuning.SimVenueModifiers
import dev.novux.dynasty.cbb.core.sim.tuning.IntMinMax
import dev.novux.dynasty.cbb.core.strategy.GameplanInput
import dev.novux.dynasty.cbb.core.strategy.OffensiveSchemePreset
import kotlinx.serialization.json.JsonObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PossessionSimEngineTest {
    private val tuning = SimTuning(
        simTuningId = "test",
        schemaVersion = 1,
        possessionsPerGameTarget = IntMinMax(60, 70),
        shotClock = SimShotClock(full = 30, offensiveRebound = 20),
        timeUsedSeconds = SimTimeUsedSeconds(
            quickTurnover = listOf(3, 6),
            quickShot = listOf(8, 14),
            foulStoppage = listOf(12, 18),
        ),
        eventWeights = JsonObject(emptyMap()),
        homeNeutralModifiers = SimVenueModifiers(1.0, 1.0, 1.0),
    )

    @Test
    fun tripSecondsWithinThreeToThirty() {
        val random = kotlin.random.Random(42)
        repeat(100) {
            val sec = PossessionSimEngine.sampleTripSeconds(tuning, random)
            assertTrue(sec in GameSimClock.MIN_TRIP_SECONDS..GameSimClock.MAX_TRIP_SECONDS)
        }
    }

    @Test
    fun boxScoreMatchesScoreboard() {
        val result = PossessionSimEngine.simulate(
            GameSimInput(
                home = GameSimTeamInput("h", 78, 74),
                away = GameSimTeamInput("a", 76, 75),
                tuning = tuning,
                seed = 99L,
            ),
        )
        assertEquals(result.homeScore, result.homeBox.points)
        assertEquals(result.awayScore, result.awayBox.points)
        assertTrue(result.homeScore in 20..120)
        assertTrue(result.awayScore in 20..120)
        val possessionsPerTeam = (result.homeBox.possessions + result.awayBox.possessions) / 2
        assertTrue(possessionsPerTeam in 35..100)
    }

    @Test
    fun rosterPrepBoostsTeamWithBetterLineup() {
        fun roster(vararg pairs: Pair<PlayerPosition, Int>) = pairs.map { (pos, off) ->
            RosterPlayer(
                playerId = pos.wire,
                dynastyId = "d",
                position = pos,
                classYear = PlayerClassYear.Senior,
                isStarter = true,
                depthOrder = 1,
                inTransferPortal = false,
                ovr = off,
                off = off,
                def = off - 5,
            )
        }
        val strong = GameLineupPrep.build(
            "h",
            roster(
                PlayerPosition.PointGuard to 88,
                PlayerPosition.ShootingGuard to 86,
                PlayerPosition.SmallForward to 85,
                PlayerPosition.PowerForward to 84,
                PlayerPosition.Center to 83,
                PlayerPosition.PointGuard to 70,
            ),
        )
        val weak = GameLineupPrep.build(
            "a",
            roster(
                PlayerPosition.PointGuard to 62,
                PlayerPosition.ShootingGuard to 61,
                PlayerPosition.SmallForward to 60,
                PlayerPosition.PowerForward to 59,
                PlayerPosition.Center to 58,
            ),
        )
        val result = PossessionSimEngine.simulate(
            GameSimInput(
                home = GameSimTeamInput("h", 60, 60),
                away = GameSimTeamInput("a", 60, 60),
                tuning = tuning,
                seed = 7L,
                homeRoster = strong,
                awayRoster = weak,
            ),
        )
        assertTrue(result.homeScore + result.awayScore > 0)
        assertTrue(strong.weightedOffensive() > weak.weightedOffensive())
    }

    @Test
    fun denyPrimaryProducesMoreTurnoversThanOpenDefense() {
        val team = GameSimTeamInput("d", 72, 72)
        var denyTurnovers = 0
        var openTurnovers = 0
        repeat(50) { i ->
            val deny = PossessionSimEngine.simulate(
                GameSimInput(
                    home = team,
                    away = team.copy(teamId = "o"),
                    tuning = tuning,
                    seed = 4000L + i,
                    homeGameplan = GameplanInput(denyPrimaryScorer = true),
                ),
            )
            val open = PossessionSimEngine.simulate(
                GameSimInput(
                    home = team,
                    away = team.copy(teamId = "o2"),
                    tuning = tuning,
                    seed = 4000L + i,
                ),
            )
            denyTurnovers += deny.awayBox.turnovers
            openTurnovers += open.awayBox.turnovers
        }
        assertTrue(denyTurnovers >= openTurnovers)
    }

    @Test
    fun perimeterSchemeAttemptsMoreThreesThanPost() {
        val team = GameSimTeamInput("t", 75, 75)
        var perimeterThrees = 0
        var postThrees = 0
        repeat(40) { i ->
            val perimeter = PossessionSimEngine.simulate(
                GameSimInput(
                    home = team.copy(
                        teamId = "perim",
                        offensiveScheme = OffensiveSchemePreset.Perimeter,
                    ),
                    away = team.copy(teamId = "opp"),
                    tuning = tuning,
                    seed = 1000L + i,
                ),
            )
            val post = PossessionSimEngine.simulate(
                GameSimInput(
                    home = team.copy(
                        teamId = "post",
                        offensiveScheme = OffensiveSchemePreset.Post,
                    ),
                    away = team.copy(teamId = "opp2"),
                    tuning = tuning,
                    seed = 1000L + i,
                ),
            )
            perimeterThrees += perimeter.homeBox.threePointsAttempted
            postThrees += post.homeBox.threePointsAttempted
        }
        assertTrue(perimeterThrees > postThrees)
    }

    @Test
    fun rosterSim_tracksPersonalFoulsUpToDisqualification() {
        fun roster(vararg pairs: Pair<PlayerPosition, Int>) = pairs.map { (pos, off) ->
            RosterPlayer(
                playerId = pos.wire,
                dynastyId = "d",
                position = pos,
                classYear = PlayerClassYear.Senior,
                isStarter = true,
                depthOrder = 1,
                inTransferPortal = false,
                ovr = off,
                off = off,
                def = off - 5,
            )
        }
        val home = GameLineupPrep.build("h", roster(
            PlayerPosition.PointGuard to 80,
            PlayerPosition.ShootingGuard to 79,
            PlayerPosition.SmallForward to 78,
            PlayerPosition.PowerForward to 77,
            PlayerPosition.Center to 76,
        ))
        val result = PossessionSimEngine.simulate(
            GameSimInput(
                home = GameSimTeamInput("h", 75, 75),
                away = GameSimTeamInput("a", 75, 75),
                tuning = tuning,
                seed = 2024L,
                homeRoster = home,
                awayRoster = home.copy(teamId = "a"),
            ),
        )
        val maxFouls = result.homePersonalFoulsByPlayer.values.maxOrNull() ?: 0
        assertTrue(result.homeBox.personalFouls > 0 || result.awayBox.personalFouls > 0 || maxFouls > 0)
        assertTrue(result.homePersonalFoulsByPlayer.values.all { it <= PersonalFoulRules.DISQUALIFICATION_FOUL_COUNT })
    }

    @Test
    fun strongerOffenseScoresMoreOften() {
        var homeTotal = 0
        var awayTotal = 0
        repeat(30) { i ->
            val result = PossessionSimEngine.simulate(
                GameSimInput(
                    home = GameSimTeamInput("h", 92, 70),
                    away = GameSimTeamInput("a", 62, 70),
                    tuning = tuning,
                    seed = i.toLong(),
                ),
            )
            homeTotal += result.homeScore
            awayTotal += result.awayScore
        }
        assertTrue(homeTotal > awayTotal)
    }
}
