package dev.novux.dynasty.cbb.core.offseason.weekone

import dev.novux.dynasty.cbb.core.roster.PlayerClassYear
import dev.novux.dynasty.cbb.core.roster.PlayerPosition
import dev.novux.dynasty.cbb.core.roster.RosterPlayer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class WeekOneNilRetentionPolicyTest {
    @Test
    fun ledgerTargetPrefixesPlayerId() {
        assertEquals("retention:p1", WeekOneNilRetentionPolicy.ledgerTargetId("p1"))
    }

    @Test
    fun suggestedOfferBumpsExistingDeal() {
        val player = samplePlayer(nilUsd = 100_000)
        assertTrue(WeekOneNilRetentionPolicy.suggestedOfferUsd(player) >= 100_000)
    }

    private fun samplePlayer(nilUsd: Long) = RosterPlayer(
        playerId = "p1",
        dynastyId = "d1",
        position = PlayerPosition.SmallForward,
        classYear = PlayerClassYear.Junior,
        isStarter = true,
        depthOrder = 1,
        inTransferPortal = false,
        declaredForDraft = false,
        draftStockPoints = 0,
        proDayCompleted = false,
        displayName = "Test",
        hometownCity = "X",
        hometownState = "ST",
        heightInches = 78,
        weightLbs = 200,
        nilUsdPerYear = nilUsd,
        ovr = 80,
    )
}