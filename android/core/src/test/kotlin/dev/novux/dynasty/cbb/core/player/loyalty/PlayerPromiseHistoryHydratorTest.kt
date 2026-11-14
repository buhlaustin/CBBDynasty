package dev.novux.dynasty.cbb.core.player.loyalty

import dev.novux.dynasty.cbb.core.player.DynastyPlayer
import dev.novux.dynasty.cbb.core.player.PlayerBootstrapFactory
import dev.novux.dynasty.cbb.core.player.PlayerRosterRole
import dev.novux.dynasty.cbb.core.roster.PlayerClassYear
import dev.novux.dynasty.cbb.core.roster.PlayerPosition
import dev.novux.dynasty.cbb.core.roster.RosterPlayerSeed
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PlayerPromiseHistoryHydratorTest {
    @Test
    fun hydrator_backfillsSeasonsAfterLock() {
        val player = sampleWithPromise(lockedAt = 1, starter = true)
        val rows = PlayerPromiseHistoryHydrator.historyForDisplay(player, currentSeasonIndex = 3)
        assertEquals(2, rows.size)
        assertTrue(rows.any { it.seasonIndex == 1 && it.status == PromiseStatus.Kept })
        assertTrue(rows.any { it.seasonIndex == 2 })
    }

    private fun sampleWithPromise(lockedAt: Int, starter: Boolean): DynastyPlayer {
        val seed = PlayerBootstrapFactory.enrichSeed(
            RosterPlayerSeed(
                playerId = "p-hist",
                dynastyId = "d1",
                position = PlayerPosition.PointGuard,
                classYear = PlayerClassYear.Sophomore,
                isStarter = starter,
                depthOrder = 1,
            ),
        )
        val base = PlayerBootstrapFactory.fromSeed(seed)
        return base.copy(
            role = PlayerRosterRole(
                isStarter = starter,
                depthOrder = 1,
                inTransferPortal = false,
                declaredForDraft = false,
                draftStockPoints = 0,
                proDayCompleted = false,
            ),
            loyalty = base.loyalty.copy(
                promise = CoachPromise(
                    type = CoachPromiseType.MinutesStarter,
                    lockedAtSeasonIndex = lockedAt,
                    status = PromiseStatus.Kept,
                ),
            ),
        )
    }
}
