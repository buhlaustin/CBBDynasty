package dev.novux.dynasty.cbb.core.preseason

import org.junit.Assert.assertEquals
import org.junit.Test

class PreseasonTaskCompletenessPolicyTest {
    @Test
    fun countsEightRequiredTasks() {
        assertEquals(8, PreseasonTaskCompletenessPolicy.requiredCount)
    }

    @Test
    fun incompleteListExcludesDone() {
        val progress = PreseasonTaskProgress(
            completed = setOf(PreseasonTaskId.IncomingClassSeed, PreseasonTaskId.PreseasonWorkouts),
        )
        assertEquals(2, PreseasonTaskCompletenessPolicy.completedCount(progress))
        assertEquals(6, PreseasonTaskCompletenessPolicy.incompleteTasks(progress).size)
    }
}