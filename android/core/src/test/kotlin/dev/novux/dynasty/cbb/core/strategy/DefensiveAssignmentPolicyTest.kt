package dev.novux.dynasty.cbb.core.strategy

import dev.novux.dynasty.cbb.core.roster.PlayerClassYear
import dev.novux.dynasty.cbb.core.roster.PlayerPosition
import dev.novux.dynasty.cbb.core.roster.RosterPlayer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DefensiveAssignmentPolicyTest {
    private fun player(id: String, pos: PlayerPosition) = RosterPlayer(
        playerId = id,
        dynastyId = "d1",
        position = pos,
        classYear = PlayerClassYear.Sophomore,
        isStarter = true,
        depthOrder = 1,
        inTransferPortal = false,
        displayName = id,
        ovr = 70,
        off = 70,
        def = 75,
    )

    private val pg = player("pg1", PlayerPosition.PointGuard)
    private val sg = player("sg1", PlayerPosition.ShootingGuard)
    private val sf = player("sf1", PlayerPosition.SmallForward)
    private val pf = player("pf1", PlayerPosition.PowerForward)
    private val c = player("c1", PlayerPosition.Center)
    private val bench = player("bench1", PlayerPosition.ShootingGuard)

    @Test
    fun defaultAssignments_mirror_starters() {
        val starters = mapOf(
            PlayerPosition.PointGuard to pg.playerId,
            PlayerPosition.ShootingGuard to sg.playerId,
            PlayerPosition.SmallForward to sf.playerId,
            PlayerPosition.PowerForward to pf.playerId,
            PlayerPosition.Center to c.playerId,
        )
        val defaults = DefensiveAssignmentPolicy.defaultAssignments(starters)
        assertEquals(starters, defaults)
    }

    @Test
    fun cycleAssignment_rotates_eligible_defenders() {
        val roster = listOf(sg, bench)
        val first = DefensiveAssignmentPolicy.cycleAssignment(roster, PlayerPosition.PointGuard, null)
        assertEquals(sg.playerId, first)
        val second = DefensiveAssignmentPolicy.cycleAssignment(roster, PlayerPosition.PointGuard, first)
        assertEquals(bench.playerId, second)
    }

    @Test
    fun deny_with_assignment_adds_extra_turnover_tweak() {
        val without = GameplanEffectsResolver.defenseTweaks(
            GameplanInput(denyPrimaryScorer = true),
            gameClockSeconds = 600,
            defenseScore = 40,
            offenseScore = 42,
        )
        val withAssignment = GameplanEffectsResolver.defenseTweaks(
            GameplanInput(
                denyPrimaryScorer = true,
                primaryScorerSlot = PlayerPosition.PointGuard,
                defensiveAssignments = mapOf(PlayerPosition.PointGuard to pg.playerId),
            ),
            gameClockSeconds = 600,
            defenseScore = 40,
            offenseScore = 42,
        )
        assertTrue(withAssignment.turnoverAdjust > without.turnoverAdjust)
    }
}
