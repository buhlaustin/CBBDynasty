package dev.novux.dynasty.cbb.core.sim

import dev.novux.dynasty.cbb.core.roster.PlayerPosition
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RotationBandFloorPolicyTest {
    private val roster = GameSimRosterInput(
        teamId = "t",
        participants = listOf(
            GameSimParticipant("starter", PlayerPosition.PointGuard, PlayerPosition.PointGuard, 80, 80, 32),
            GameSimParticipant("reserve", PlayerPosition.ShootingGuard, PlayerPosition.ShootingGuard, 75, 75, 20),
        ),
    )

    @Test
    fun rotatesOutPlayerAheadOfMinutePace() {
        val minutes = SimFloorMinutesLedger(mapOf("starter" to 20 * 60))
        val clock = GameSimClock.REGULATION_SECONDS / 2
        assertTrue(
            RotationBandFloorPolicy.shouldRotateOut(
                roster.participants.first(),
                minutes,
                clock,
            ),
        )
    }

    @Test
    fun subInReserveWhenStarterOverplayed() {
        val foul = PersonalFoulLedger()
        val minutes = SimFloorMinutesLedger(mapOf("starter" to 19 * 60, "reserve" to 2 * 60))
        val foulFloor = FoulTroubleRotationPolicy.floorParticipants(roster, foul, GameSimClock.HALF_SECONDS)
        val floor = RotationBandFloorPolicy.applyMinuteBands(
            roster,
            foulFloor,
            foul,
            minutes,
            GameSimClock.HALF_SECONDS,
        )
        assertTrue(floor.any { it.playerId == "reserve" })
        assertFalse(floor.any { it.playerId == "starter" })
    }
}
