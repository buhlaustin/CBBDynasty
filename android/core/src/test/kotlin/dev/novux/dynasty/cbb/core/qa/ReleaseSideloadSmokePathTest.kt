package dev.novux.dynasty.cbb.core.qa

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ReleaseSideloadSmokePathTest {
    @Test
    fun applicationId_matchesPlayPreLaunchProduct() {
        assertEquals("com.dynasty.cbb", ReleaseSideloadSmokePath.APPLICATION_ID)
    }

    @Test
    fun parityChecklist_coversDebugSmokeMilestones() {
        val ids = ReleaseSideloadSmokePath.PARITY_WITH_DEBUG.map { it.id }
        assertTrue(ids.contains("cold_launch"))
        assertTrue(ids.contains("new_game_sign"))
        assertTrue(ids.contains("reach_rs"))
        assertEquals(ReleaseSideloadSmokePath.QA_ID, "MEGA-QA-134")
    }
}
