package dev.novux.dynasty.cbb.core.lineup

import org.junit.Assert.assertEquals
import org.junit.Test

class RotationMinutesBudgetTest {
    @Test
    fun statusDetectsUnderExactOver() {
        val cap = RotationMinutesAllocator.TEAM_MINUTES_REGULATION
        assertEquals(RotationMinutesBudget.Status.Under, RotationMinutesBudget.status(180, cap))
        assertEquals(RotationMinutesBudget.Status.Exact, RotationMinutesBudget.status(200, cap))
        assertEquals(RotationMinutesBudget.Status.Over, RotationMinutesBudget.status(212, cap))
    }
}
