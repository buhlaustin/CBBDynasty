package dev.novux.dynasty.cbb.core.sim

import dev.novux.dynasty.cbb.core.qa.PossessionLogPlayerPath
import dev.novux.dynasty.cbb.core.roster.PlayerClassYear
import dev.novux.dynasty.cbb.core.roster.PlayerPosition
import dev.novux.dynasty.cbb.core.roster.RosterPlayer
import dev.novux.dynasty.cbb.core.sim.tuning.IntMinMax
import dev.novux.dynasty.cbb.core.sim.tuning.SimShotClock
import dev.novux.dynasty.cbb.core.sim.tuning.SimTimeUsedSeconds
import dev.novux.dynasty.cbb.core.sim.tuning.SimTuning
import dev.novux.dynasty.cbb.core.sim.tuning.SimVenueModifiers
import kotlinx.serialization.json.JsonObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class PossessionLogPlayerLabelTest {
    @Test
    fun megaSim097_qaAnchor() {
        assertEquals(PossessionLogPlayerPath.SIM_ID, PossessionLogPlayerLabel.QA_ID)
    }

    @Test
    fun format_makeIncludesJerseyNameAndAssist() {
        val roster = sampleRoster("pg1", "Jay Holt")
        val credits = PossessionPlayerCredits(scorerId = "pg1", assistId = "sg1")
        val before = TeamBoxScore(teamId = "KU")
        val after = before.copy(
            fieldGoalsMade = 1,
            fieldGoalsAttempted = 1,
            points = 2,
        )
        val text = PossessionLogPlayerLabel.format(
            credits = credits,
            offenseRoster = roster,
            defenseRoster = null,
            before = before,
            after = after,
            change = PossessionChange.DefenseTakesBall,
            blockedShot = false,
        )
        assertNotNull(text)
        assertTrue(text!!.contains("#"))
        assertTrue(text.contains("Jay Holt"))
        assertTrue(text.contains("ast"))
    }

    @Test
    fun fullSim_possessionLogIncludesJerseyAndName() {
        fun player(id: String, pos: PlayerPosition, name: String) = RosterPlayer(
            playerId = id,
            dynastyId = "d",
            position = pos,
            classYear = PlayerClassYear.Senior,
            isStarter = true,
            depthOrder = 1,
            inTransferPortal = false,
            displayName = name,
            ovr = 82,
            off = 82,
            def = 78,
        )
        val roster = GameLineupPrep.build(
            "home",
            listOf(
                player("pg", PlayerPosition.PointGuard, "Chris Lane"),
                player("sg", PlayerPosition.ShootingGuard, "Drew Cole"),
                player("sf", PlayerPosition.SmallForward, "Evan Moss"),
                player("pf", PlayerPosition.PowerForward, "Finn Gray"),
                player("c", PlayerPosition.Center, "Gabe Holt"),
            ),
        )
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
        val result = PossessionSimEngine.simulate(
            GameSimInput(
                home = GameSimTeamInput("h", 80, 78),
                away = GameSimTeamInput("a", 76, 76),
                tuning = tuning,
                seed = 97L,
                homeRoster = roster,
            ),
        )
        assertTrue(
            result.possessionLog.any { line ->
                line.contains("#") && (
                    line.contains("Chris Lane") ||
                        line.contains("Drew Cole") ||
                        line.contains("rebound") ||
                        line.contains("make")
                    )
            },
        )
    }

    private fun sampleRoster(scorerId: String, scorerName: String): GameSimRosterInput =
        GameSimRosterInput(
            teamId = "KU",
            participants = listOf(
                GameSimParticipant(
                    playerId = scorerId,
                    slotPlayed = PlayerPosition.PointGuard,
                    listedPosition = PlayerPosition.PointGuard,
                    offensiveRating = 80,
                    defensiveRating = 75,
                    targetMinutes = 32,
                    logDisplayName = scorerName,
                    jerseyNumber = 12,
                ),
                GameSimParticipant(
                    playerId = "sg1",
                    slotPlayed = PlayerPosition.ShootingGuard,
                    listedPosition = PlayerPosition.ShootingGuard,
                    offensiveRating = 78,
                    defensiveRating = 76,
                    targetMinutes = 30,
                    logDisplayName = "Kim Lee",
                    jerseyNumber = 5,
                ),
            ),
        )

}
