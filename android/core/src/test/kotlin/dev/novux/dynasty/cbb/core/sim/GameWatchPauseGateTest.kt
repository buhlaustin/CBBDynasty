package dev.novux.dynasty.cbb.core.sim

import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.delay
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class GameWatchPauseGateTest {
    @Test
    fun pauseBlocksUntilResume() = runBlocking {
        val gate = GameWatchPauseGate()
        gate.pause()
        var released = false
        val job = async {
            gate.awaitUnpaused()
            released = true
        }
        delay(50)
        assertFalse(released)
        gate.resume()
        job.await()
        assertTrue(released)
        assertFalse(gate.paused)
    }
}
