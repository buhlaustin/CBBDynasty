package dev.novux.dynasty.cbb.core.recruiting

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RecruitInterestMeterDisplayTest {
    @Test
    fun progressFraction_scalesZeroToOne() {
        assertEquals(0f, RecruitInterestMeterDisplay.progressFraction(0), 0.001f)
        assertEquals(0.5f, RecruitInterestMeterDisplay.progressFraction(50), 0.001f)
        assertEquals(1f, RecruitInterestMeterDisplay.progressFraction(100), 0.001f)
    }

    @Test
    fun clampsOutOfRangeInterest() {
        assertEquals(100, RecruitInterestMeterDisplay.clampedLiveInterest(150))
        assertEquals(0f, RecruitInterestMeterDisplay.progressFraction(-10))
    }

    @Test
    fun spendGain_neverNegative() {
        assertEquals(0, RecruitInterestMeterDisplay.spendGainPoints(60, 40))
        assertTrue(RecruitInterestMeterDisplay.spendGainPoints(40, 55) == 15)
    }
}
