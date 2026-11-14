package dev.novux.dynasty.cbb.core.sim

import dev.novux.dynasty.cbb.core.balance.BalanceFatigue
import dev.novux.dynasty.cbb.core.roster.PlayerClassYear
import dev.novux.dynasty.cbb.core.roster.PlayerPosition
import dev.novux.dynasty.cbb.core.roster.RosterPlayer
import dev.novux.dynasty.cbb.core.strategy.DefensiveSchemePreset
import dev.novux.dynasty.cbb.core.strategy.SchemeFatigueCost
import org.junit.Assert.assertTrue
import org.junit.Test

class SchemeDefensiveFatigueSimTest {
    @Test
    fun pressExtraDrainMatchesCombinedFloorTime() {
        val roster = GameLineupPrep.build("home", sampleRoster())
        val trip = 120
        val extra = SchemeFatigueCost.extraDefensiveDrainSeconds(DefensiveSchemePreset.FullCourtPress, trip)
        assertTrue(extra > 0)
        val tuning = BalanceFatigue.DEFAULT.copy(baseDrainPerMinute = 12.0)
        val starterId = roster.participants.first { it.slotPlayed == PlayerPosition.PointGuard }.playerId
        val pressLedger = SimStaminaLedger(roster)
        val manLedger = SimStaminaLedger(roster)
        pressLedger.drainFloorTime(listOf(starterId), trip + extra, tuning)
        manLedger.drainFloorTime(listOf(starterId), trip, tuning)
        assertTrue(pressLedger.stamina(starterId) < manLedger.stamina(starterId))
    }

    private fun sampleRoster(): List<RosterPlayer> = listOf(
        player("pg", PlayerPosition.PointGuard),
        player("sg", PlayerPosition.ShootingGuard),
        player("sf", PlayerPosition.SmallForward),
        player("pf", PlayerPosition.PowerForward),
        player("c", PlayerPosition.Center),
        player("b1", PlayerPosition.ShootingGuard, depth = 6),
    )

    private fun player(id: String, pos: PlayerPosition, depth: Int = 1): RosterPlayer =
        RosterPlayer(
            playerId = id,
            dynastyId = "d",
            position = pos,
            classYear = PlayerClassYear.Junior,
            isStarter = depth <= 5,
            depthOrder = depth,
            inTransferPortal = false,
        )
}
