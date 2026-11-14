package dev.novux.dynasty.cbb.core.save

import java.util.concurrent.ConcurrentHashMap
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Serializes `advanceDynastyWeek` (and related writes) per save slot ([MEGA-QA-045]).
 */
class SlotAdvanceSerialGuard {
    private val mutexBySlot = ConcurrentHashMap<Int, Mutex>()

    suspend fun <T> withSlotAdvanceLock(slotIndex: Int, block: suspend () -> T): T {
        val mutex = mutexBySlot.getOrPut(slotIndex) { Mutex() }
        return mutex.withLock { block() }
    }
}
