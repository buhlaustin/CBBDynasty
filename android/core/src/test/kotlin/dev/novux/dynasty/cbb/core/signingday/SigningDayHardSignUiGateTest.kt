package dev.novux.dynasty.cbb.core.signingday

import dev.novux.dynasty.cbb.core.qa.SigningDayHardSignGatingPath
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SigningDayHardSignUiGateTest {
    @Test
    fun megaRecr103_hardSignBlockedUntilRankingsAndPositions() {
        assertEquals(SigningDayHardSignGatingPath.RECR_ID, "MEGA-RECR-103")
        val fresh = SigningDayProgress(cycleSeasonIndex = 1)
        assertFalse(SigningDayHardSignUiGate.canConfirmPositions(fresh))
        assertFalse(SigningDayHardSignUiGate.canHardSign(fresh, allSigneesHavePromises = true))
        val rankingsOnly = fresh.copy(rankingsViewed = true)
        assertTrue(SigningDayHardSignUiGate.canConfirmPositions(rankingsOnly))
        assertFalse(SigningDayHardSignUiGate.canHardSign(rankingsOnly, allSigneesHavePromises = true))
        val ready = rankingsOnly.copy(positionsConfirmed = true)
        assertFalse(SigningDayHardSignUiGate.canConfirmPositions(ready))
        assertTrue(SigningDayHardSignUiGate.canHardSign(ready, allSigneesHavePromises = true))
        assertFalse(SigningDayHardSignUiGate.canHardSign(ready, allSigneesHavePromises = false))
    }

    @Test
    fun confirmPositionsBlockedWhenProgressNull() {
        assertFalse(SigningDayHardSignUiGate.canConfirmPositions(null))
        assertFalse(SigningDayHardSignUiGate.canHardSign(null, allSigneesHavePromises = true))
    }
}
