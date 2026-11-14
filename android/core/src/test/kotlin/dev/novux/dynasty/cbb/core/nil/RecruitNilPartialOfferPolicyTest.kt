package dev.novux.dynasty.cbb.core.nil

import dev.novux.dynasty.cbb.core.balance.BalanceNilRules
import org.junit.Assert.assertEquals
import org.junit.Test

class RecruitNilPartialOfferPolicyTest {
  private val rules = BalanceNilRules(maxOfferMultiplierOfAsk = 3, interestNilRatioExponent = 1.0)

    @Test
    fun partialOffer_scalesToCap() {
        val cap = NilAssignCapLinePolicy.maxOfferUsd(100_000, rules)
        assertEquals(cap / 2, RecruitNilPartialOfferPolicy.partialOfferUsd(100_000, rules, 0.5f))
        assertEquals(0L, RecruitNilPartialOfferPolicy.partialOfferUsd(100_000, rules, 0f))
        assertEquals(cap, RecruitNilPartialOfferPolicy.partialOfferUsd(100_000, rules, 1f))
    }
}