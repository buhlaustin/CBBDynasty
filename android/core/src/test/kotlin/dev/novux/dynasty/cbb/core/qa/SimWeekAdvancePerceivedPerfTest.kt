package dev.novux.dynasty.cbb.core.qa

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SimWeekAdvancePerceivedPerfTest {
    @Test
    fun deviceBudget_matchesQa014_twoPointFiveSeconds() {
        assertEquals(2_500L, SimWeekAdvancePerceivedPerf.maxPerceivedMs)
    }

    @Test
    fun withinBudget_boundary() {
        assertTrue(SimWeekAdvancePerceivedPerf.withinDeviceBudget(2_500L))
        assertFalse(SimWeekAdvancePerceivedPerf.withinDeviceBudget(2_501L))
    }

    @Test
    fun recordSample_updatesLast() {
        SimWeekAdvancePerceivedPerf.recordSample(900L)
        assertEquals(900L, SimWeekAdvancePerceivedPerf.lastSample?.elapsedMs)
    }
}
