package dev.novux.dynasty.cbb.core.recruiting

import org.junit.Assert.assertTrue
import org.junit.Test

class IncomingClassSeedIoPolicyTest {
    @Test
    fun documentsOffMainThreadSeedPolicy() {
        assertTrue(IncomingClassSeedIoPolicy.CPU_BOUND_OPERATIONS.contains("IncomingClassSeeder.seed"))
        assertTrue(IncomingClassSeedIoPolicy.CPU_BOUND_OPERATIONS.contains("decodePool"))
    }
}
