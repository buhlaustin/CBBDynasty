package dev.novux.dynasty.cbb.core.player.nil

import dev.novux.dynasty.cbb.core.player.PlayerIdentity
import dev.novux.dynasty.cbb.core.roster.PlayerClassYear
import dev.novux.dynasty.cbb.core.roster.PlayerPosition
import dev.novux.dynasty.cbb.core.roster.RosterPlayer
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Assert.assertEquals
import org.junit.Test

class PlayerNilSeasonDealEngineTest {
    private val base = RosterPlayer(
        playerId = "p1",
        dynastyId = "d1",
        position = PlayerPosition.PointGuard,
        classYear = PlayerClassYear.Sophomore,
        isStarter = true,
        depthOrder = 0,
        inTransferPortal = false,
        nilUsdPerYear = 100_000,
    )

    @Test
    fun raisedChipWhenFlagSetAndDealPositive() {
        val identity = PlayerIdentity(
            displayName = "Test",
            hometownCity = "",
            hometownState = "TX",
            heightInches = 76,
            weightLbs = 200,
            nilUsdPerYear = 120_000,
            nilDealRaisedThisSeason = true,
        )
        assertTrue(PlayerNilSeasonDealEngine.showRaisedChip(identity))
    }

    @Test
    fun retentionBumpSetsFlagWhenPriorDealPositive() {
        val bumped = PlayerNilSeasonDealEngine.applyRetentionBump(base, 150_000)
        assertEquals(150_000, bumped.nilUsdPerYear)
        assertTrue(bumped.nilDealRaisedThisSeason)
    }

    @Test
    fun firstSigningDoesNotSetRaisedFlag() {
        val signed = PlayerNilSeasonDealEngine.applyRetentionBump(base.copy(nilUsdPerYear = 0), 80_000)
        assertEquals(80_000, signed.nilUsdPerYear)
        assertFalse(signed.nilDealRaisedThisSeason)
    }

    @Test
    fun clearSeasonFlags() {
        val flagged = base.copy(nilDealRaisedThisSeason = true)
        val cleared = PlayerNilSeasonDealEngine.clearSeasonDealFlags(listOf(flagged)).single()
        assertFalse(cleared.nilDealRaisedThisSeason)
    }
}
