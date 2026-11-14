package dev.novux.dynasty.cbb.core.preseason

import org.junit.Assert.assertEquals
import org.junit.Test

class PreseasonNewsFeedPolicyTest {
    @Test
    fun top60StoryId() {
        assertEquals("top60-3", PreseasonNewsFeedPolicy.top60StoryId(3))
    }

    @Test
    fun megaFrameId() {
        assertEquals("MEGA-FRAME-077", PreseasonNewsFeedPolicy.MEGA_FRAME_ID)
    }
}
