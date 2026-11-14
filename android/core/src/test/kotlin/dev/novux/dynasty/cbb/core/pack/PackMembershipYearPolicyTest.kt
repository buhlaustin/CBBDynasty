package dev.novux.dynasty.cbb.core.pack

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PackMembershipYearPolicyTest {
    @Test
    fun mismatchWhenYearsDiffer() {
        assertTrue(PackMembershipYearPolicy.isMismatch("2025-26", "2026-27"))
    }

    @Test
    fun noMismatchWhenSameOrUnknown() {
        assertFalse(PackMembershipYearPolicy.isMismatch("2026-27", "2026-27"))
        assertFalse(PackMembershipYearPolicy.isMismatch(null, "2026-27"))
    }
}