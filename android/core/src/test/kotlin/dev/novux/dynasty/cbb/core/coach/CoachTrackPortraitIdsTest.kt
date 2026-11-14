package dev.novux.dynasty.cbb.core.coach

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CoachTrackPortraitIdsTest {
    @Test
    fun forStaffId_isStableAndUsesCoachPrefix() {
        val id = CoachTrackPortraitIds.forStaffId("asst-dyn-1")
        assertEquals(id, CoachTrackPortraitIds.forStaffId("asst-dyn-1"))
        assertTrue(id.startsWith("coach-"))
        assertTrue(CoachAvatarPresets.isKnownSkin(id.split("-")[1]))
    }
}
