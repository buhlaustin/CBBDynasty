package dev.novux.dynasty.cbb.core.player.loyalty

import dev.novux.dynasty.cbb.core.recruiting.RecruitOfferPromise
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RecruitCommitPromiseLockEngineTest {
    @Test
    fun lockFromOffer_setsSeasonIndex() {
        val offer = RecruitOfferPromise(
            prospectId = "hs-1-0",
            promiseType = CoachPromiseType.StayPledgeTopN,
            stayTopN = 12,
        )
        val locked = RecruitCommitPromiseLockEngine.lockFromOffer(offer, lockedAtSeasonIndex = 4)
        assertEquals(4, locked.lockedAtSeasonIndex)
        assertEquals(PromiseStatus.Pending, locked.toCoachPromise().status)
    }

    @Test
    fun allSigneesNeedOffers() {
        val offers = mapOf("a" to RecruitOfferPromise("a", CoachPromiseType.MinutesStarter))
        assertTrue(RecruitCommitPromiseLockEngine.allSigneesHaveOffer(listOf("a"), offers))
        assertFalse(RecruitCommitPromiseLockEngine.allSigneesHaveOffer(listOf("a", "b"), offers))
    }
}
