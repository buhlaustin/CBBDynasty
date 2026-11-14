package dev.novux.dynasty.cbb.core.player.draft

import dev.novux.dynasty.cbb.core.player.DynastyPlayer
import dev.novux.dynasty.cbb.core.player.PlayerBootstrapFactory
import dev.novux.dynasty.cbb.core.player.PlayerRosterRole
import dev.novux.dynasty.cbb.core.roster.PlayerClassYear
import dev.novux.dynasty.cbb.core.roster.PlayerPosition
import dev.novux.dynasty.cbb.core.roster.RosterPlayerSeed
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PlayerDraftProjectionEngineTest {
    @Test
    fun seniorWithStock_mapsToRankAndLotteryTier() {
        val base = samplePlayer(stock = 92)
        val proj = PlayerDraftProjectionEngine.resolve(base)
        assertTrue(proj.isAvailable)
        assertTrue(proj.rank!! in 1..14)
        assertEquals("Lottery", proj.tierLabel)
    }

    @Test
    fun freshmanShowsNotAvailable() {
        val player = samplePlayer(stock = 90, classYear = PlayerClassYear.Freshman)
        assertFalse(PlayerDraftProjectionEngine.resolve(player).isAvailable)
    }

    private fun samplePlayer(
        stock: Int,
        classYear: PlayerClassYear = PlayerClassYear.Senior,
    ): DynastyPlayer {
        val seed = PlayerBootstrapFactory.enrichSeed(
            RosterPlayerSeed(
                playerId = "p1",
                dynastyId = "d1",
                position = PlayerPosition.PointGuard,
                classYear = classYear,
                isStarter = true,
                depthOrder = 1,
            ),
        )
        return PlayerBootstrapFactory.fromSeed(seed).copy(
            role = PlayerRosterRole(
                isStarter = true,
                depthOrder = 1,
                inTransferPortal = false,
                declaredForDraft = false,
                draftStockPoints = stock,
                proDayCompleted = false,
            ),
        )
    }
}
