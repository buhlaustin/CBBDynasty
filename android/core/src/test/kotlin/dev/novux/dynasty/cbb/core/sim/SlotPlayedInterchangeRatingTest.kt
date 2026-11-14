package dev.novux.dynasty.cbb.core.sim

import dev.novux.dynasty.cbb.core.roster.PlayerClassYear
import dev.novux.dynasty.cbb.core.roster.PlayerPosition
import dev.novux.dynasty.cbb.core.roster.RosterPlayer
import org.junit.Assert.assertTrue
import org.junit.Test

class SlotPlayedInterchangeRatingTest {
    @Test
    fun elitePgAtSgSlotBeatsNativeSgOnFloor() {
        val pgStar = rosterPlayer("pg1", PlayerPosition.PointGuard, off = 84)
        val weakSg = rosterPlayer("sg1", PlayerPosition.ShootingGuard, off = 72)
        val filler = listOf(
            rosterPlayer("sf1", PlayerPosition.SmallForward, 70),
            rosterPlayer("pf1", PlayerPosition.PowerForward, 70),
            rosterPlayer("c1", PlayerPosition.Center, 70),
        )
        val interchangeStarters = mapOf(
            PlayerPosition.PointGuard to "pg1",
            PlayerPosition.ShootingGuard to "pg1",
            PlayerPosition.SmallForward to "sf1",
            PlayerPosition.PowerForward to "pf1",
            PlayerPosition.Center to "c1",
        )
        val nativeStarters = mapOf(
            PlayerPosition.PointGuard to "pg1",
            PlayerPosition.ShootingGuard to "sg1",
            PlayerPosition.SmallForward to "sf1",
            PlayerPosition.PowerForward to "pf1",
            PlayerPosition.Center to "c1",
        )
        val interchangeRoster = GameLineupPrep.build(
            "t",
            listOf(pgStar, weakSg) + filler,
            userStarters = interchangeStarters,
        )
        val nativeRoster = GameLineupPrep.build(
            "t",
            listOf(pgStar, weakSg) + filler,
            userStarters = nativeStarters,
        )
        val interchangeFloor = interchangeRoster.participants.filter { it.targetMinutes >= 20 }
        val nativeFloor = nativeRoster.participants.filter { it.targetMinutes >= 20 }
        val interchangeOff = SlotPlayedFloorRating.offensiveFromFloor(interchangeFloor, 60)
        val nativeOff = SlotPlayedFloorRating.offensiveFromFloor(nativeFloor, 60)
        assertTrue(interchangeOff > nativeOff)
    }

    private fun rosterPlayer(id: String, pos: PlayerPosition, off: Int) = RosterPlayer(
        playerId = id,
        dynastyId = "d",
        position = pos,
        classYear = PlayerClassYear.Senior,
        isStarter = true,
        depthOrder = 1,
        inTransferPortal = false,
        ovr = off,
        off = off,
        def = off - 4,
    )
}
