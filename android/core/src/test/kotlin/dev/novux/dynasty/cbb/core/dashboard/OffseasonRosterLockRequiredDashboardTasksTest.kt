package dev.novux.dynasty.cbb.core.dashboard

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class OffseasonRosterLockRequiredDashboardTasksTest {
    @Test
    fun overCap_emitsRequiredTask() {
        val tasks = OffseasonRosterLockRequiredDashboardTasks.build(16)
        assertEquals(1, tasks.size)
        assertEquals(DashboardTaskId.OffseasonRosterLock, tasks.single().id)
        assertFalse(OffseasonRosterLockRequiredDashboardTasks.canAdvanceWeek(16))
    }

    @Test
    fun atCap_clearsTasks() {
        assertTrue(OffseasonRosterLockRequiredDashboardTasks.build(15).isEmpty())
        assertTrue(OffseasonRosterLockRequiredDashboardTasks.canAdvanceWeek(15))
    }
}
