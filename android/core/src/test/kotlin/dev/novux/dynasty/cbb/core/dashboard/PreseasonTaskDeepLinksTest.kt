package dev.novux.dynasty.cbb.core.dashboard

import dev.novux.dynasty.cbb.core.preseason.PreseasonTaskId
import org.junit.Assert.assertEquals
import org.junit.Test

class PreseasonTaskDeepLinksTest {
    @Test
    fun deepLinkRoundTrip() {
        PreseasonTaskId.entries.forEach { id ->
            val link = PreseasonTaskDeepLinks.deepLink(id)
            assertEquals(id, PreseasonTaskDeepLinks.taskId(link))
        }
    }
}
