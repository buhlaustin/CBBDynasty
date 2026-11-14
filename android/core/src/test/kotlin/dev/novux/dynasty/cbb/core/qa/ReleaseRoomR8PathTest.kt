package dev.novux.dynasty.cbb.core.qa

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ReleaseRoomR8PathTest {
    @Test
    fun targetsThreeWeekAdvances() {
        assertEquals(3, ReleaseRoomR8Path.TARGET_ADVANCE_WEEKS)
    }

    @Test
    fun listsCriticalRoomEntities() {
        assertTrue(ReleaseRoomR8Path.CRITICAL_ENTITIES.any { it.contains("SaveSlotEntity") })
        assertEquals("MEGA-QA-153", ReleaseRoomR8Path.QA_ID)
    }
}
