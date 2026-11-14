package dev.novux.dynasty.cbb.core.injuries

import dev.novux.dynasty.cbb.core.calendar.CalendarPhase
import dev.novux.dynasty.cbb.core.roster.PlayerClassYear
import dev.novux.dynasty.cbb.core.roster.PlayerPosition
import dev.novux.dynasty.cbb.core.roster.RosterPlayer
import org.junit.Assert.assertEquals
import org.junit.Test

class InjuryMultiReturnOrderTest {
    @Test
    fun multipleStarterReturnsProcessPgBeforeCenter() {
        val players = listOf(
            injuredStarter("pg1", PlayerPosition.PointGuard, "pg"),
            injuredStarter("c1", PlayerPosition.Center, "c"),
            bench("fill1", PlayerPosition.ShootingGuard),
            bench("fill2", PlayerPosition.PowerForward),
            bench("fill3", PlayerPosition.SmallForward),
        )
        val ticked = InjuryRosterOps.tickWeekAdvance(
            CalendarPhase.RegularSeason,
            players.map {
                if (it.injuryActive) it.copy(injuryWeeksRemaining = 1) else it
            },
        )
        val pg = ticked.players.first { it.playerId == "pg1" }
        val center = ticked.players.first { it.playerId == "c1" }
        assertEquals(false, pg.injuryActive)
        assertEquals(false, center.injuryActive)
        assertEquals("pg", pg.starterSlotWire)
        assertEquals("c", center.starterSlotWire)
    }

    private fun injuredStarter(id: String, pos: PlayerPosition, slotWire: String) = RosterPlayer(
        playerId = id,
        dynastyId = "d",
        position = pos,
        classYear = PlayerClassYear.Junior,
        isStarter = false,
        depthOrder = 30,
        inTransferPortal = false,
        injuryActive = true,
        injuryWeeksRemaining = 1,
        injuryWasStarterSlotWire = slotWire,
        starterSlotWire = null,
    )

    private fun bench(id: String, pos: PlayerPosition) = RosterPlayer(
        playerId = id,
        dynastyId = "d",
        position = pos,
        classYear = PlayerClassYear.Sophomore,
        isStarter = true,
        depthOrder = 1,
        inTransferPortal = false,
        starterSlotWire = pos.wire,
    )
}
