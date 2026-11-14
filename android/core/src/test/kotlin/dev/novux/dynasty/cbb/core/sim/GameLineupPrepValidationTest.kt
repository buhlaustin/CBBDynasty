package dev.novux.dynasty.cbb.core.sim

import dev.novux.dynasty.cbb.core.roster.PlayerClassYear
import dev.novux.dynasty.cbb.core.roster.PlayerPosition
import dev.novux.dynasty.cbb.core.roster.RosterPlayer
import org.junit.Assert.assertFalse
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class GameLineupPrepValidationTest {
    private fun player(id: String, pos: PlayerPosition, ovr: Int = 70) = RosterPlayer(
        playerId = id,
        dynastyId = "d",
        position = pos,
        classYear = PlayerClassYear.Junior,
        isStarter = true,
        depthOrder = 1,
        inTransferPortal = false,
        ovr = ovr,
        off = ovr,
        def = ovr - 2,
    )

    @Test
    fun completeAutoStartersCanStartSim() {
        val roster = listOf(
            player("pg", PlayerPosition.PointGuard),
            player("sg", PlayerPosition.ShootingGuard),
            player("sf", PlayerPosition.SmallForward),
            player("pf", PlayerPosition.PowerForward),
            player("c", PlayerPosition.Center),
        )
        val validation = GameLineupPrepValidator.validate(roster, null)
        assertTrue(validation.canStartSim)
        GameLineupPrep.build("t", roster)
    }

    @Test
    fun missingStarterSlotBlocksSim() {
        val roster = listOf(
            player("pg", PlayerPosition.PointGuard),
            player("sg", PlayerPosition.ShootingGuard),
        )
        val validation = GameLineupPrepValidator.validate(roster, null)
        assertFalse(validation.canStartSim)
        assertTrue(validation.missingSlots.isNotEmpty())
        assertThrows(IllegalArgumentException::class.java) {
            GameLineupPrep.build("t", roster)
        }
    }

    @Test
    fun illegalInterchangeBlocksSim() {
        val pg = player("pg", PlayerPosition.PointGuard, 90)
        val roster = listOf(pg)
        val validation = GameLineupPrepValidator.validate(
            roster,
            mapOf(
                PlayerPosition.PointGuard to "pg",
                PlayerPosition.Center to "pg",
            ),
        )
        assertFalse(validation.canStartSim)
        assertTrue(validation.illegalAssignments.isNotEmpty())
    }

    @Test
    fun legalGuardInterchangeAllowsSim() {
        val pg = player("pg", PlayerPosition.PointGuard, 90)
        val roster = listOf(
            pg,
            player("sg", PlayerPosition.ShootingGuard),
            player("sf", PlayerPosition.SmallForward),
            player("pf", PlayerPosition.PowerForward),
            player("c", PlayerPosition.Center),
        )
        val starters = mapOf(
            PlayerPosition.PointGuard to "pg",
            PlayerPosition.ShootingGuard to "pg",
            PlayerPosition.SmallForward to "sf",
            PlayerPosition.PowerForward to "pf",
            PlayerPosition.Center to "c",
        )
        val validation = GameLineupPrepValidator.validate(roster, starters)
        assertTrue(validation.canStartSim)
    }
}
