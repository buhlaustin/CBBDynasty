package dev.novux.dynasty.cbb.core.signingday

import dev.novux.dynasty.cbb.core.player.loyalty.CoachPromiseType
import dev.novux.dynasty.cbb.core.recruiting.RecruitOfferPromise
import dev.novux.dynasty.cbb.core.roster.PlayerPosition
import org.junit.Assert.assertTrue
import org.junit.Test

class SigningDayHardSignEngineTest {
    @Test
    fun hardSign_requiresRankingsAndPositions() {
        val incomplete = SigningDayProgress(cycleSeasonIndex = 1, rankingsViewed = true)
        try {
            SigningDayHardSignEngine.finalizeClass(incomplete, emptyList(), emptyMap())
            error("expected failure")
        } catch (_: IllegalArgumentException) {
        }
        val signee = IncomingSignee(
            prospectId = "p1",
            displayName = "Test Player",
            stars = 4,
            position = PlayerPosition.SmallForward,
            homeState = "OH",
            nilAskUsd = 100_000,
            nilSignedUsd = 80_000,
            nationalHsRank = 12,
            scoutLetterOverall = "B+",
        )
        val offers = mapOf(
            "p1" to RecruitOfferPromise("p1", CoachPromiseType.MinutesRotation),
        )
        val done = SigningDayHardSignEngine.finalizeClass(
            incomplete.copy(positionsConfirmed = true),
            listOf(signee),
            offers,
        )
        assertTrue(done.progress.hardSignComplete)
    }
}
