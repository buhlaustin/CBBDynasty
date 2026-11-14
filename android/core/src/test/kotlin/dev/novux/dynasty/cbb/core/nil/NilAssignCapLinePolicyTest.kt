package dev.novux.dynasty.cbb.core.nil

import dev.novux.dynasty.cbb.core.balance.BalanceNilRules
import org.junit.Assert.assertEquals
import org.junit.Test

class NilAssignCapLinePolicyTest {
    private val rules = BalanceNilRules(maxOfferMultiplierOfAsk = 3, interestNilRatioExponent = 1.0)

    @Test
    fun capIsThreeTimesAsk() {
        assertEquals(300_000L, NilAssignCapLinePolicy.maxOfferUsd(100_000, rules))
    }
}