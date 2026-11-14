package dev.novux.dynasty.cbb.core.player.history

import dev.novux.dynasty.cbb.core.player.stats.PlayerCountingStats
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PlayerPriorSeasonSnapshotTest {
    @Test
    fun perGameRates_divideByGamesPlayed() {
        val snap = PlayerPriorSeasonSnapshot(
            seasonIndex = 1,
            gamesPlayed = 10,
            totals = PlayerCountingStats(points = 150, assists = 30, rebounds = 50),
        )
        assertEquals(15.0, snap.pointsPerGame(), 0.001)
        assertEquals(3.0, snap.assistsPerGame(), 0.001)
        assertEquals(5.0, snap.reboundsPerGame(), 0.001)
    }

    @Test
    fun transferChipWhenPriorSchoolDiffers() {
        val snap = PlayerPriorSeasonSnapshot(
            seasonIndex = 1,
            gamesPlayed = 5,
            totals = PlayerCountingStats(),
            schoolDisplayName = "State U",
        )
        assertTrue(snap.showsTransferFrom("Dynasty U"))
        assertTrue(!snap.showsTransferFrom("State U"))
    }
}
