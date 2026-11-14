package dev.novux.dynasty.cbb.core.qa

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SimWeekAdvancePreDialogPerfTest {
    @Test
    fun qa008PreDialogBudget() {
        assertEquals("MEGA-QA-008", SimWeekAdvancePreDialogPerf.QA_ID)
        assertEquals(500L, SimWeekAdvancePreDialogPerf.MAX_PRE_DIALOG_MS)
        assertEquals(5, SimWeekAdvancePreDialogPerf.DEVICE_PROFILE_RS_WEEK_INDEX)
    }

    @Test
    fun recordsSampleAndBudget() {
        SimWeekAdvancePreDialogPerf.recordSample(120L)
        assertEquals(120L, SimWeekAdvancePreDialogPerf.lastSample?.elapsedMs)
        assertTrue(SimWeekAdvancePreDialogPerf.withinDeviceBudget(500L))
        assertFalse(SimWeekAdvancePreDialogPerf.withinDeviceBudget(501L))
    }
}
