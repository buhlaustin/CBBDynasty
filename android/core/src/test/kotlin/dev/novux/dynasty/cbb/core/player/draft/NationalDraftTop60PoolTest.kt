package dev.novux.dynasty.cbb.core.player.draft

import dev.novux.dynasty.cbb.core.player.DynastyPlayer
import dev.novux.dynasty.cbb.core.player.PlayerBootstrapFactory
import dev.novux.dynasty.cbb.core.player.PlayerRosterRole
import dev.novux.dynasty.cbb.core.roster.PlayerClassYear
import dev.novux.dynasty.cbb.core.roster.PlayerPosition
import dev.novux.dynasty.cbb.core.roster.RosterPlayerSeed
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class NationalDraftTop60PoolTest {
    @Test
    fun eliteSeniorInRoster_receivesNationalRank() {
        val player = samplePlayer(stock = 97, classYear = PlayerClassYear.Senior)
        val ranks = NationalDraftTop60Pool.rankIndexForRoster(
            roster = listOf(player),
            dynastyId = "dyn-pool-test",
            seasonIndex = 3,
        )
        val rank = ranks[player.playerId]
        assertNotNull(rank)
        assertTrue(rank!! in 1..NationalDraftTop60Pool.POOL_SIZE)
    }

    @Test
    fun resolve_usesRankOnDynastyPlayerWhenSet() {
        val player = samplePlayer(stock = 97, classYear = PlayerClassYear.Senior)
            .copy(nationalTop60DraftRank = 12)
        val projection = PlayerDraftProjectionEngine.resolve(player)
        assertTrue(projection.isAvailable)
        org.junit.Assert.assertEquals(12, projection.rank)
    }

    @Test
    fun resolve_usesPoolRankNotStockWhenMapProvided() {
        val player = samplePlayer(stock = 97, classYear = PlayerClassYear.Senior)
        val projection = PlayerDraftProjectionEngine.resolve(
            player,
            nationalTop60RankByPlayerId = mapOf(player.playerId to 9),
        )
        assertTrue(projection.isAvailable)
        org.junit.Assert.assertEquals(9, projection.rank)
        org.junit.Assert.assertEquals("Lottery", projection.tierLabel)
    }

    private fun samplePlayer(stock: Int, classYear: PlayerClassYear): DynastyPlayer {
        val seed = PlayerBootstrapFactory.enrichSeed(
            RosterPlayerSeed(
                playerId = "p-pool",
                dynastyId = "d1",
                position = PlayerPosition.ShootingGuard,
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
                declaredForDraft = true,
                draftStockPoints = stock,
                proDayCompleted = true,
            ),
        )
    }
}
