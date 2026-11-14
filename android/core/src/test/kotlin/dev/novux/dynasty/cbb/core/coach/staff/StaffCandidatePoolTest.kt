package dev.novux.dynasty.cbb.core.coach.staff

import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class StaffCandidatePoolTest {
    @Test
    fun poachRatingsMeetOrExceedHireBaseline() {
        val hire = StaffCandidatePool.generateAssistantHire(
            "dyn",
            "School",
            CoachTrackRole.Assistant,
            seed = 1L,
        )
        val poach = StaffCandidatePool.generateAssistantPoach(
            "dyn",
            "School",
            CoachTrackRole.Assistant,
            seed = 1L,
        )
        assertNotEquals(hire.displayName, poach.displayName)
        assertTrue(poach.ratings.recruiting >= hire.ratings.recruiting)
    }
}
