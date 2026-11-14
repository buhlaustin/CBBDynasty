package dev.novux.dynasty.cbb.core.dashboard

import dev.novux.dynasty.cbb.core.schedule.ScheduleGameKind
import dev.novux.dynasty.cbb.core.schedule.ScheduledGameStub
import org.junit.Assert.assertEquals
import org.junit.Test

/** [MEGA-FRAME-106] */
class InSeasonOptionalDashboardTasksTest {
    @Test
    fun build_emitsTaskPerScheduledGame() {
        val games = listOf(
            ScheduledGameStub(ScheduleGameKind.NonConference, "a", "Alpha", true, 2),
            ScheduledGameStub(ScheduleGameKind.NonConference, "b", "Beta", false, 2),
        )
        val tasks = InSeasonOptionalDashboardTasks.build(games, weekSimStaged = false)
        assertEquals(2, tasks.size)
        assertEquals(setOf("a", "b"), tasks.map { it.dismissScopeKey }.toSet())
    }

    @Test
    fun build_weekStaged_emitsReplayTasks() {
        val games = listOf(
            ScheduledGameStub(ScheduleGameKind.NonConference, "a", "Alpha", true, 2),
        )
        val tasks = InSeasonOptionalDashboardTasks.build(games, weekSimStaged = true)
        assertEquals(1, tasks.size)
        assertEquals(DashboardTaskId.ReplayStagedWeekGame, tasks.single().id)
        assertEquals(DashboardDeepLink.ReplayProvisionalWeekGame, tasks.single().deepLink)
    }
}