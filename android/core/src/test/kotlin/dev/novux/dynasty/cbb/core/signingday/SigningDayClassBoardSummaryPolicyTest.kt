package dev.novux.dynasty.cbb.core.signingday

import dev.novux.dynasty.cbb.core.roster.PlayerPosition
import org.junit.Assert.assertEquals
import org.junit.Test

class SigningDayClassBoardSummaryPolicyTest {
    @Test
    fun orderedCountsSkipsEmptyPositions() {
        val signees = listOf(
            IncomingSignee(
                prospectId = "a",
                displayName = "A",
                stars = 4,
                position = PlayerPosition.PointGuard,
                homeState = "TX",
                nilAskUsd = 0,
                nationalHsRank = 10,
                scoutLetterOverall = "B+",
            ),
            IncomingSignee(
                prospectId = "b",
                displayName = "B",
                stars = 3,
                position = PlayerPosition.PointGuard,
                homeState = "CA",
                nilAskUsd = 0,
                nationalHsRank = 40,
                scoutLetterOverall = "B",
            ),
            IncomingSignee(
                prospectId = "c",
                displayName = "C",
                stars = 5,
                position = PlayerPosition.Center,
                homeState = "FL",
                nilAskUsd = 0,
                nationalHsRank = 3,
                scoutLetterOverall = "A",
            ),
        )
        assertEquals(
            listOf(PlayerPosition.PointGuard to 2, PlayerPosition.Center to 1),
            SigningDayClassBoardSummaryPolicy.orderedCounts(signees),
        )
    }
}