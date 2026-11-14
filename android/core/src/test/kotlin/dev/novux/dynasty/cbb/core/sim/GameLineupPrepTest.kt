package dev.novux.dynasty.cbb.core.sim

import dev.novux.dynasty.cbb.core.roster.PlayerClassYear
import dev.novux.dynasty.cbb.core.roster.PlayerPosition
import dev.novux.dynasty.cbb.core.roster.RosterPlayer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GameLineupPrepTest {
    @Test
    fun prepUsesFiveStartersAndRotationMinutes() {
        val roster = (1..10).map { i ->
            val pos = PlayerPosition.entries[i % 5]
            RosterPlayer(
                playerId = "p$i",
                dynastyId = "d",
                position = pos,
                classYear = PlayerClassYear.Junior,
                isStarter = i <= 5,
                depthOrder = i,
                inTransferPortal = false,
                ovr = 60 + i,
                off = 60 + i,
                def = 58,
            )
        }
        val prep = GameLineupPrep.build("team", roster)
        assertEquals(200, prep.participants.sumOf { it.targetMinutes })
        assertEquals(5, prep.participants.count { it.targetMinutes >= 30 })
    }

    @Test
    fun userInterchangeHonorsSlotPolicy() {
        fun rosterPlayer(id: String, pos: PlayerPosition, ovr: Int) = RosterPlayer(
            playerId = id,
            dynastyId = "d",
            position = pos,
            classYear = PlayerClassYear.Senior,
            isStarter = true,
            depthOrder = 1,
            inTransferPortal = false,
            ovr = ovr,
            off = ovr,
            def = ovr - 2,
        )
        val pg = rosterPlayer("pg1", PlayerPosition.PointGuard, 90)
        val sg = rosterPlayer("sg1", PlayerPosition.ShootingGuard, 72)
        val sf = rosterPlayer("sf1", PlayerPosition.SmallForward, 70)
        val pf = rosterPlayer("pf1", PlayerPosition.PowerForward, 68)
        val c = rosterPlayer("c1", PlayerPosition.Center, 71)
        val prep = GameLineupPrep.build(
            "team",
            listOf(pg, sg, sf, pf, c),
            userStarters = mapOf(
                PlayerPosition.PointGuard to "pg1",
                PlayerPosition.ShootingGuard to "pg1",
                PlayerPosition.SmallForward to "sf1",
                PlayerPosition.PowerForward to "pf1",
                PlayerPosition.Center to "c1",
            ),
        )
        assertTrue(prep.participants.any { it.playerId == "pg1" && it.slotPlayed == PlayerPosition.ShootingGuard })
    }
}
