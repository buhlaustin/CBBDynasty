package dev.novux.dynasty.cbb.core.qa

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ReleaseR8MinifyPathTest {
    @Test
    fun qaId_isMegaQa151() {
        assertEquals("MEGA-QA-151", ReleaseR8MinifyPath.QA_ID)
    }

    @Test
    fun requiresMinifyAndShrinkResourcesFlags() {
        assertEquals(2, ReleaseR8MinifyPath.REQUIRED_GRADLE_FLAGS.size)
        assertTrue(ReleaseR8MinifyPath.REQUIRED_GRADLE_FLAGS.all { it.contains("true") })
    }
}
