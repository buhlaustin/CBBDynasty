package dev.novux.dynasty.cbb.core.dashboard

import dev.novux.dynasty.cbb.core.seasonreview.SeasonReviewProgress
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SeasonReviewRequiredDashboardTasksTest {
    @Test
    fun unacknowledged_emitsSingleRequiredTask() {
        val tasks = SeasonReviewRequiredDashboardTasks.build(SeasonReviewProgress(acknowledged = false))
        assertEquals(1, tasks.size)
        assertEquals(DashboardTaskId.SeasonReviewAcknowledge, tasks.single().id)
        assertEquals(DashboardDeepLink.SeasonReviewRecap, tasks.single().deepLink)
    }

    @Test
    fun acknowledged_clearsTasksAndAllowsOffseason() {
        val progress = SeasonReviewProgress(acknowledged = true)
        assertTrue(SeasonReviewRequiredDashboardTasks.build(progress).isEmpty())
        assertTrue(SeasonReviewRequiredDashboardTasks.canEnterOffseason(progress))
    }

    @Test
    fun unacknowledged_blocksOffseason() {
        assertFalse(SeasonReviewRequiredDashboardTasks.canEnterOffseason(SeasonReviewProgress()))
    }
}
