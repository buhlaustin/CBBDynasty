package dev.novux.dynasty.cbb.core.sim

import dev.novux.dynasty.cbb.core.qa.ScoreDifferentialSituationPath
import dev.novux.dynasty.cbb.core.roster.PlayerClassYear
import dev.novux.dynasty.cbb.core.roster.PlayerPosition
import dev.novux.dynasty.cbb.core.roster.RosterPlayer
import dev.novux.dynasty.cbb.core.sim.tuning.IntMinMax
import dev.novux.dynasty.cbb.core.sim.tuning.SimShotClock
import dev.novux.dynasty.cbb.core.sim.tuning.SimSituationWeights
import dev.novux.dynasty.cbb.core.sim.tuning.SimTimeUsedSeconds
import dev.novux.dynasty.cbb.core.sim.tuning.SimTuning
import dev.novux.dynasty.cbb.core.sim.tuning.SimVenueModifiers
import dev.novux.dynasty.cbb.core.strategy.ResolvedSchemeEffects
import kotlinx.serialization.json.JsonObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ScoreDifferentialSituationPolicyTest {
    private val weights = SimSituationWeights()

    @Test
    fun megaSim111_qaAnchor() {
        assertEquals(ScoreDifferentialSituationPath.SIM_ID, ScoreDifferentialSituationPolicy.QA_ID)
    }

    @Test
    fun classify_trailingAndLeadingInLastFourMinutes() {
        assertEquals(
            ScoreDifferentialSituationPolicy.Situation.TrailingLate,
            ScoreDifferentialSituationPolicy.classify(200, 62, 70, weights),
        )
        assertEquals(
            ScoreDifferentialSituationPolicy.Situation.LeadingLate,
            ScoreDifferentialSituationPolicy.classify(200, 74, 68, weights),
        )
        assertEquals(
            ScoreDifferentialSituationPolicy.Situation.Neutral,
            ScoreDifferentialSituationPolicy.classify(301, 62, 70, weights),
        )
    }

    @Test
    fun trailingLate_increasesThreePointAttemptRateVsLeading() {
        val base = ResolvedSchemeEffects(
            tripPaceScale = 1.0,
            threePointAttemptRate = 0.36,
            turnoverChance = 0.20,
            makeRateAdjust = 0.0,
            offensiveRatingDelta = 0,
            defensiveRatingDelta = 0,
        )
        val trailing = ScoreDifferentialSituationPolicy.applyToEffects(
            base,
            ScoreDifferentialSituationPolicy.Situation.TrailingLate,
            weights,
        )
        val leading = ScoreDifferentialSituationPolicy.applyToEffects(
            base,
            ScoreDifferentialSituationPolicy.Situation.LeadingLate,
            weights,
        )
        assertTrue(trailing.threePointAttemptRate > leading.threePointAttemptRate)
        assertTrue(trailing.turnoverChance >= leading.turnoverChance)
    }

    @Test
    fun resumedLateGame_trailingTeamAttemptsMoreThrees() {
        val tuning = SimTuning(
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
        fun roster(team: String) = GameLineupPrep.build(
            team,
            (1..6).map { i ->
                RosterPlayer(
                    playerId = "$team-p$i",
                    dynastyId = "d",
                    position = PlayerPosition.entries[i % 5],
                    classYear = PlayerClassYear.Junior,
                    isStarter = i <= 5,
                    depthOrder = i,
                    inTransferPortal = false,
                    off = 72,
                    def = 70,
                )
            },
        )
        val trailingResume = MidGameState(
            random = kotlin.random.Random(1),
            gameClockSeconds = 180,
            offenseHome = true,
            homeBox = TeamBoxScore(teamId = "trail", points = 60),
            awayBox = TeamBoxScore(teamId = "lead", points = 68),
            possessionLog = emptyList(),
            totalPossessions = 80,
        )
        val leadingResume = trailingResume.copy(
            homeBox = TeamBoxScore(teamId = "lead", points = 72),
            awayBox = TeamBoxScore(teamId = "trail", points = 65),
        )
        fun threes(resume: MidGameState, seed: Long): Int {
            val input = GameSimInput(
                home = GameSimTeamInput("lead", 75, 75),
                away = GameSimTeamInput("trail", 75, 75),
                tuning = tuning,
                seed = seed,
                homeRoster = roster("lead"),
                awayRoster = roster("trail"),
            )
            return PossessionSimEngine.simulate(
                input,
                SimulateOptions(resume = resume.copy(random = kotlin.random.Random(seed))),
            ).homeBox.threePointsAttempted
        }
        var trailingThrees = 0
        var leadingThrees = 0
        repeat(25) { i ->
            val seed = 9000L + i
            trailingThrees += threes(trailingResume, seed)
            leadingThrees += threes(leadingResume, seed)
        }
        assertTrue(trailingThrees > leadingThrees)
    }
}
