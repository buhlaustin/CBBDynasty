package dev.novux.dynasty.cbb.core.player.stats

import dev.novux.dynasty.cbb.core.roster.PlayerClassYear
import dev.novux.dynasty.cbb.core.roster.PlayerPosition
import dev.novux.dynasty.cbb.core.roster.RosterPlayer
import org.junit.Assert.assertEquals
import org.junit.Test

class PlayerSeasonStatFinalizeGateTest {
    @Test
    fun noGamePlayedWeek_returnsEmpty() {
        val roster = listOf(sample("a", isStarter = true))
        assertEquals(0, PlayerSeasonStatFinalizeGate.playersReceivingGamePlayed(roster, teamPlayedGame = false).size)
    }

    @Test
    fun startersAndRotationReceiveGp() {
        val roster = listOf(
            sample("a", isStarter = true, depth = 1),
            sample("b", isStarter = false, depth = 8),
            sample("c", isStarter = false, depth = 14),
        )
        val got = PlayerSeasonStatFinalizeGate.playersReceivingGamePlayed(roster, teamPlayedGame = true)
        assertEquals(setOf("a", "b"), got.map { it.playerId }.toSet())
    }

    private fun sample(
        id: String,
        isStarter: Boolean,
        depth: Int = 5,
    ) = RosterPlayer(
        playerId = id,
        dynastyId = "d1",
        position = PlayerPosition.PointGuard,
        classYear = PlayerClassYear.Sophomore,
        isStarter = isStarter,
        depthOrder = depth,
        inTransferPortal = false,
    )
}
