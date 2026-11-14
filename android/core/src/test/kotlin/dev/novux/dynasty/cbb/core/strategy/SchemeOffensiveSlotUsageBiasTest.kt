package dev.novux.dynasty.cbb.core.strategy

import dev.novux.dynasty.cbb.core.roster.PlayerClassYear
import dev.novux.dynasty.cbb.core.roster.PlayerPosition
import dev.novux.dynasty.cbb.core.roster.RosterPlayer
import dev.novux.dynasty.cbb.core.sim.GameLineupPrep
import org.junit.Assert.assertTrue
import org.junit.Test

class SchemeOffensiveSlotUsageBiasTest {
    @Test
    fun perimeterPresetBoostsGuardUsageShare() {
        val roster = GameLineupPrep.build("t", sampleRoster())
        val floor = roster.participants.map { it.playerId }
        val base = floor.associateWith { 20 }
        val biased = SchemeOffensiveSlotUsageBias.apply(
            scheme = OffensiveSchemePreset.Perimeter,
            roster = roster,
            offenseFloor = floor,
            baseWeights = base,
        )
        val pg = roster.participants.first { it.slotPlayed == PlayerPosition.PointGuard }.playerId
        val c = roster.participants.first { it.slotPlayed == PlayerPosition.Center }.playerId
        assertTrue(biased.getValue(pg) > biased.getValue(c))
    }

  @Test
    fun postPresetBoostsPostSlots() {
        val roster = GameLineupPrep.build("t", sampleRoster())
        val floor = roster.participants.map { it.playerId }
        val base = floor.associateWith { 20 }
        val biased = SchemeOffensiveSlotUsageBias.apply(
            scheme = OffensiveSchemePreset.Post,
            roster = roster,
            offenseFloor = floor,
            baseWeights = base,
        )
        val pf = roster.participants.first { it.slotPlayed == PlayerPosition.PowerForward }.playerId
        val sg = roster.participants.first { it.slotPlayed == PlayerPosition.ShootingGuard }.playerId
        assertTrue(biased.getValue(pf) > biased.getValue(sg))
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
            classYear = PlayerClassYear.Sophomore,
            isStarter = depth <= 5,
            depthOrder = depth,
            inTransferPortal = false,
            displayName = id,
            off = 70,
            def = 68,
        )
}
