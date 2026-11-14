package dev.novux.dynasty.cbb.core.preseason

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class PreseasonFeatureTaskCompletionPolicyTest {
    @Test
    fun allPreseasonTasks_useFeatureWiring_notGenericComplete() {
        assertEquals(PreseasonTaskId.entries.toSet(), PreseasonFeatureTaskCompletionPolicy.noGenericCompleteButton)
        PreseasonTaskId.entries.forEach { id ->
            assertFalse(PreseasonFeatureTaskCompletionPolicy.allowsGenericCompleteButton(id))
        }
    }
}
