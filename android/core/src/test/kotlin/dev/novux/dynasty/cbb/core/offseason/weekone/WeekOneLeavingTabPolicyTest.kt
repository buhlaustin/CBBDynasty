package dev.novux.dynasty.cbb.core.offseason.weekone

import dev.novux.dynasty.cbb.core.roster.PlayerClassYear
import dev.novux.dynasty.cbb.core.roster.PlayerPosition
import dev.novux.dynasty.cbb.core.roster.RosterPlayer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class WeekOneLeavingTabPolicyTest {
    @Test
    fun tabsPartitionPortalState() {
        val base = RosterPlayer(
            playerId = "p1",
            dynastyId = "d",
            position = PlayerPosition.ShootingGuard,
            classYear = PlayerClassYear.Sophomore,
            isStarter = true,
            depthOrder = 1,
            inTransferPortal = false,
        )
        val portal = WeekOneLeavingActions.encourageTransfer(base)
        val all = listOf(base.copy(playerId = "p0"), portal)
        val roster = WeekOneLeavingTabPolicy.rosterPlayers(all)
        val transfer = WeekOneLeavingTabPolicy.transferPlayers(all)
        assertEquals(1, roster.size)
        assertEquals(1, transfer.size)
        assertTrue(roster.none { it.inTransferPortal })
        assertTrue(transfer.all { it.inTransferPortal })
    }
}
