package dev.novux.dynasty.cbb.core.sim

import dev.novux.dynasty.cbb.core.roster.PlayerPosition
import dev.novux.dynasty.cbb.core.sim.tuning.SimPositionComfortModifiers
import org.junit.Assert.assertEquals
import org.junit.Test

class PositionComfortNudgeTest {
    @Test
    fun eligibleInterchangeSmallPenalty() {
        val p = GameSimParticipant(
            playerId = "p",
            slotPlayed = PlayerPosition.ShootingGuard,
            listedPosition = PlayerPosition.PointGuard,
            offensiveRating = 80,
            defensiveRating = 78,
            targetMinutes = 32,
        )
        assertEquals(79, PositionComfortNudge.offensiveRating(p))
    }

    @Test
    fun ineligibleInterchangeLargerPenalty() {
        val p = GameSimParticipant(
            playerId = "p",
            slotPlayed = PlayerPosition.Center,
            listedPosition = PlayerPosition.PointGuard,
            offensiveRating = 80,
            defensiveRating = 78,
            targetMinutes = 32,
        )
        assertEquals(77, PositionComfortNudge.offensiveRating(p))
    }

    @Test
    fun tuningPenaltiesDriveComfortDelta() {
        val comfort = SimPositionComfortModifiers(
            eligibleInterchangePenalty = 2,
            ineligibleSlotPenalty = 5,
        )
        val p = GameSimParticipant(
            playerId = "p",
            slotPlayed = PlayerPosition.Center,
            listedPosition = PlayerPosition.PointGuard,
            offensiveRating = 80,
            defensiveRating = 78,
            targetMinutes = 32,
        )
        assertEquals(75, PositionComfortNudge.offensiveRating(p, comfort))
    }
}
