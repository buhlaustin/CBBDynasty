package dev.novux.dynasty.cbb.core.dashboard

import dev.novux.dynasty.cbb.core.preseason.PreseasonTaskId
import dev.novux.dynasty.cbb.core.preseason.PreseasonTaskProgress
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PreseasonRequiredDashboardTasksTest {
    @Test
    fun build_listsOnlyIncomplete_forAdvanceGate() {
        val progress = PreseasonTaskProgress(completed = setOf(PreseasonTaskId.IncomingClassSeed))
        val tasks = PreseasonRequiredDashboardTasks.build(progress)
        assertEquals(PreseasonTaskId.entries.size - 1, tasks.size)
        assertTrue(tasks.none { it.stringFormatArgs.firstOrNull() == PreseasonTaskId.IncomingClassSeed.name })
    }

    @Test
    fun checklistSurface_keepsAllEightTaskIds() {
        assertEquals(8, PreseasonTaskId.entries.size)
    }

    @Test
    fun eachIncompleteTask_hasDistinctDeepLinkAndTaskId() {
        val tasks = PreseasonRequiredDashboardTasks.build(PreseasonTaskProgress())
        assertEquals(PreseasonTaskId.entries.size, tasks.size)
        val deepLinks = tasks.map { it.deepLink }.toSet()
        assertEquals(PreseasonTaskId.entries.size, deepLinks.size)
        tasks.forEach { task ->
            assertEquals(task.preseasonTaskId, PreseasonTaskDeepLinks.taskId(task.deepLink))
        }
    }
}
