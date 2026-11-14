package dev.novux.dynasty.cbb.core.preseason

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PreseasonTaskOrderPolicyTest {
    @Test
    fun flexibleBlockCoversTasksOneThroughFive() {
        assertEquals("MEGA-FRAME-131", PreseasonTaskOrderPolicy.FRAME_ID)
        assertEquals(5, PreseasonTaskOrderPolicy.flexibleOrderTaskIds.size)
        assertTrue(PreseasonTaskId.IncomingClassSeed in PreseasonTaskOrderPolicy.flexibleOrderTaskIds)
        assertTrue(PreseasonTaskId.PreseasonReport in PreseasonTaskOrderPolicy.flexibleOrderTaskIds)
    }
}