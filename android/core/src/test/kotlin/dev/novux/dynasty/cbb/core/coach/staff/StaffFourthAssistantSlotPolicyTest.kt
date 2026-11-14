package dev.novux.dynasty.cbb.core.coach.staff

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class StaffFourthAssistantSlotPolicyTest {
    @Test
    fun fourthSlotAtPrestige85() {
        assertEquals(3, StaffFourthAssistantSlotPolicy.maxAssistantSlots(84))
        assertEquals(4, StaffFourthAssistantSlotPolicy.maxAssistantSlots(85))
        assertTrue(StaffFourthAssistantSlotPolicy.showFourthSlotPlaceholder(90, 3))
        assertFalse(StaffFourthAssistantSlotPolicy.showFourthSlotPlaceholder(70, 3))
    }
}