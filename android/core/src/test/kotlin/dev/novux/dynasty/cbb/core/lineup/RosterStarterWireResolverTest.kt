package dev.novux.dynasty.cbb.core.lineup

import dev.novux.dynasty.cbb.core.roster.PlayerClassYear
import dev.novux.dynasty.cbb.core.roster.PlayerPosition
import dev.novux.dynasty.cbb.core.roster.RosterPlayer
import org.junit.Assert.assertEquals
import org.junit.Test

class RosterStarterWireResolverTest {
    @Test
    fun persistsStarterSlotWireOnApply() {
        val players = listOf(
            sample("pg1", PlayerPosition.PointGuard, isStarter = true, wire = "pg"),
            sample("sg1", PlayerPosition.ShootingGuard),
            sample("sf1", PlayerPosition.SmallForward),
            sample("pf1", PlayerPosition.PowerForward),
            sample("c1", PlayerPosition.Center),
        )
        val starters = mapOf(
            PlayerPosition.PointGuard to "pg1",
            PlayerPosition.ShootingGuard to "sg1",
            PlayerPosition.SmallForward to "sf1",
            PlayerPosition.PowerForward to "pf1",
            PlayerPosition.Center to "c1",
        )
        val next = RosterStarterWireResolver.applyToRoster(players, starters)
        assertEquals("pg", next.first { it.playerId == "pg1" }.starterSlotWire)
        assertEquals(true, next.first { it.playerId == "sg1" }.isStarter)
        assertEquals("sg", next.first { it.playerId == "sg1" }.starterSlotWire)
    }

    private fun sample(
        id: String,
        pos: PlayerPosition,
        isStarter: Boolean = false,
        wire: String? = null,
    ) = RosterPlayer(
        playerId = id,
        dynastyId = "d",
        displayName = id,
        position = pos,
        classYear = PlayerClassYear.Sophomore,
        depthOrder = 1,
        inTransferPortal = false,
        isStarter = isStarter,
        starterSlotWire = wire,
    )
}
