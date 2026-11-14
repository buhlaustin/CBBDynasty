package dev.novux.dynasty.cbb.core.sim

import dev.novux.dynasty.cbb.core.qa.DenyPrimaryScorerPath
import dev.novux.dynasty.cbb.core.roster.PlayerPosition
import dev.novux.dynasty.cbb.core.sim.tuning.SimDenyPrimaryWeights
import dev.novux.dynasty.cbb.core.strategy.GameplanInput
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class DenyPrimaryScorerPolicyTest {
    private val weights = SimDenyPrimaryWeights(
        assignedSlotTurnoverBoost = 0.05,
        denyFoulOnPrimaryTouchRate = 1.0,
        denyFoulOnTurnoverRate = 1.0,
    )

    private val offenseRoster = GameSimRosterInput(
        teamId = "off",
        participants = listOf(
            rosterPlayer("pg1", PlayerPosition.PointGuard, 70, 55, minutes = 32),
            rosterPlayer("sg1", PlayerPosition.ShootingGuard, 68, 58),
            rosterPlayer("sf1", PlayerPosition.SmallForward, 66, 60),
            rosterPlayer("pf1", PlayerPosition.PowerForward, 64, 62),
            rosterPlayer("c1", PlayerPosition.Center, 62, 65),
        ),
    )

    private val defenseRoster = GameSimRosterInput(
        teamId = "def",
        participants = listOf(
            rosterPlayer("dpg", PlayerPosition.PointGuard, 55, 72, minutes = 32),
            rosterPlayer("dsg", PlayerPosition.ShootingGuard, 58, 70),
            rosterPlayer("dsf", PlayerPosition.SmallForward, 60, 68),
            rosterPlayer("dpf", PlayerPosition.PowerForward, 62, 66),
            rosterPlayer("dc", PlayerPosition.Center, 64, 64),
        ),
    )

    @Test
    fun megaSim117_qaAnchor() {
        assertEquals(DenyPrimaryScorerPath.SIM_ID, DenyPrimaryScorerPolicy.QA_ID)
    }

    @Test
    fun context_mapsAssignmentAndPrimaryOnFloor() {
        val floorOff = listOf("pg1", "sg1", "sf1", "pf1", "c1")
        val floorDef = listOf("dpg", "dsg", "dsf", "dpf", "dc")
        val gp = GameplanInput(
            denyPrimaryScorer = true,
            primaryScorerSlot = PlayerPosition.PointGuard,
            defensiveAssignments = mapOf(PlayerPosition.PointGuard to "dpg"),
        )
        val ctx = DenyPrimaryScorerPolicy.context(gp, offenseRoster, floorOff, floorDef, defenseRoster)
        assertTrue(ctx.active)
        assertEquals("pg1", ctx.primaryOffenseId)
        assertEquals("dpg", ctx.assignedDefenderId)
        assertEquals(PlayerPosition.PointGuard, ctx.opponentSlot)
    }

    @Test
    fun extraTurnoverChance_onlyWhenAssignmentAndPrimaryPresent() {
        val active = DenyPrimaryScorerPolicy.Context(
            active = true,
            primaryOffenseId = "pg1",
            assignedDefenderId = "dpg",
            opponentSlot = PlayerPosition.PointGuard,
        )
        assertEquals(weights.assignedSlotTurnoverBoost, DenyPrimaryScorerPolicy.extraTurnoverChance(active, weights), 0.0001)
        assertEquals(0.0, DenyPrimaryScorerPolicy.extraTurnoverChance(active.copy(assignedDefenderId = null), weights), 0.0)
    }

    @Test
    fun rollDenyFoul_targetsPrimaryTouchNotRandomTurnoverHandler() {
        val ctx = DenyPrimaryScorerPolicy.Context(
            active = true,
            primaryOffenseId = "pg1",
            assignedDefenderId = "dpg",
            opponentSlot = PlayerPosition.PointGuard,
        )
        val primaryCredits = PossessionPlayerCredits(scorerId = "pg1")
        assertTrue(
            DenyPrimaryScorerPolicy.rollDenyFoulOnPrimaryTouch(ctx, primaryCredits, turnoversAdded = 0, kotlin.random.Random(1), weights),
        )
        val otherTurnover = PossessionPlayerCredits(turnoverHandlerId = "sg1")
        assertFalse(
            DenyPrimaryScorerPolicy.rollDenyFoulOnPrimaryTouch(ctx, otherTurnover, turnoversAdded = 1, kotlin.random.Random(1), weights),
        )
        val primaryTurnover = PossessionPlayerCredits(turnoverHandlerId = "pg1")
        assertTrue(
            DenyPrimaryScorerPolicy.rollDenyFoulOnPrimaryTouch(ctx, primaryTurnover, turnoversAdded = 1, kotlin.random.Random(1), weights),
        )
    }

    @Test
    fun logTag_includesSlotAndDefender() {
        val ctx = DenyPrimaryScorerPolicy.Context(
            active = true,
            primaryOffenseId = "pg1",
            assignedDefenderId = "dpg",
            opponentSlot = PlayerPosition.PointGuard,
        )
        assertNotNull(DenyPrimaryScorerPolicy.logTag(ctx))
    }

    private fun rosterPlayer(
        id: String,
        pos: PlayerPosition,
        off: Int,
        def: Int,
        minutes: Int = 28,
    ) = GameSimParticipant(
        playerId = id,
        slotPlayed = pos,
        listedPosition = pos,
        offensiveRating = off,
        defensiveRating = def,
        targetMinutes = minutes,
        logDisplayName = id,
    )
}
