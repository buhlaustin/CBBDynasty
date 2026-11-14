package dev.novux.dynasty.cbb.core.preseason

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class IncomingClassSeedRunProgressTest {
    @Test
    fun fractions_increaseMonotonically() {
        val phases = IncomingClassSeedRunPhase.entries
        val fractions = phases.map { IncomingClassSeedRunProgress.fraction(it) }
        assertTrue(fractions.zipWithNext().all { (a, b) -> a <= b })
        assertEquals(1f, fractions.last(), 0.001f)
    }

    @Test
    fun megaFrameId() {
        assertEquals("MEGA-FRAME-071", IncomingClassSeedRunProgress.MEGA_FRAME_ID)
    }
}
