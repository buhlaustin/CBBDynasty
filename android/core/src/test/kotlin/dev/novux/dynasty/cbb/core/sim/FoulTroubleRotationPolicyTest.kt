package dev.novux.dynasty.cbb.core.sim

import dev.novux.dynasty.cbb.core.roster.PlayerPosition
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class FoulTroubleRotationPolicyTest {
    private val roster = GameSimRosterInput(
        teamId = "t",
        participants = listOf(
            GameSimParticipant("s1", PlayerPosition.PointGuard, PlayerPosition.PointGuard, 90, 70, 32),
            GameSimParticipant("s2", PlayerPosition.ShootingGuard, PlayerPosition.ShootingGuard, 70, 88, 32),
            GameSimParticipant("b1", PlayerPosition.SmallForward, PlayerPosition.SmallForward, 75, 75, 18),
            GameSimParticipant("b2", PlayerPosition.PowerForward, PlayerPosition.PowerForward, 74, 74, 16),
            GameSimParticipant("b3", PlayerPosition.Center, PlayerPosition.Center, 72, 72, 14),
            GameSimParticipant("b4", PlayerPosition.Center, PlayerPosition.Center, 71, 71, 12),
        ),
    )

    @Test
    fun benchesAtThreeFoulsFirstHalf() {
        assertTrue(FoulTroubleRotationPolicy.isFoulTroubleBenched(3, GameSimClock.HALF_SECONDS + 60))
        assertFalse(FoulTroubleRotationPolicy.isFoulTroubleBenched(2, GameSimClock.HALF_SECONDS + 60))
    }

    @Test
    fun benchesAtFourFoulsSecondHalf() {
        assertFalse(FoulTroubleRotationPolicy.isFoulTroubleBenched(3, GameSimClock.HALF_SECONDS - 60))
        assertTrue(FoulTroubleRotationPolicy.isFoulTroubleBenched(4, GameSimClock.HALF_SECONDS - 60))
    }

    @Test
    fun floorSwapsInReserveWhenStarterInTrouble() {
        val ledger = PersonalFoulLedger(mapOf("s1" to 3))
        val floor = FoulTroubleRotationPolicy.floorParticipants(
            roster,
            ledger,
            gameClockSeconds = GameSimClock.REGULATION_SECONDS,
        )
        assertEquals(5, floor.size)
        assertTrue("s1" !in floor.map { it.playerId })
        assertTrue("b1" in floor.map { it.playerId })
    }

    @Test
    fun weightedDefenseReflectsFloorSwap() {
        val ledger = PersonalFoulLedger(mapOf("s2" to 3))
        val static = roster.weightedDefensive()
        val onFloor = roster.weightedDefensiveOnFloor(ledger, SimFloorMinutesLedger(), GameSimClock.REGULATION_SECONDS)
        assertTrue(onFloor < static)
    }
}
