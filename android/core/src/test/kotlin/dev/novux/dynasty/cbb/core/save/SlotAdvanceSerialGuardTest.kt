package dev.novux.dynasty.cbb.core.save

import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

class SlotAdvanceSerialGuardTest {
    @Test
    fun sameSlot_concurrentWaiters_neverOverlap() = runBlocking {
        val guard = SlotAdvanceSerialGuard()
        var inFlight = 0
        var maxInFlight = 0
        val monitor = Any()
        val jobs = (1..4).map {
            async {
                guard.withSlotAdvanceLock(0) {
                    synchronized(monitor) {
                        inFlight++
                        maxInFlight = maxOf(maxInFlight, inFlight)
                    }
                    delay(30)
                    synchronized(monitor) {
                        inFlight--
                    }
                }
            }
        }
        jobs.forEach { it.await() }
        assertEquals(1, maxInFlight)
    }

    @Test
    fun differentSlots_concurrentWaiters_mayOverlap() = runBlocking {
        val guard = SlotAdvanceSerialGuard()
        var inFlight = 0
        var maxInFlight = 0
        val monitor = Any()
        val jobs = listOf(0, 1).map { slot ->
            async {
                guard.withSlotAdvanceLock(slot) {
                    synchronized(monitor) {
                        inFlight++
                        maxInFlight = maxOf(maxInFlight, inFlight)
                    }
                    delay(50)
                    synchronized(monitor) {
                        inFlight--
                    }
                }
            }
        }
        jobs.forEach { it.await() }
        assertEquals(2, maxInFlight)
    }
}
