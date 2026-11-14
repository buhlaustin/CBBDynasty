package dev.novux.dynasty.cbb.core.dashboard

import dev.novux.dynasty.cbb.core.calendar.CalendarPhase
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class InSeasonRecruitingOptionalDashboardTasksTest {
    @Test
    fun spendTask_whenPointsAvailableAndCycleOpen() {
        val tasks = InSeasonRecruitingOptionalDashboardTasks.build(
            InSeasonRecruitingDashboardContext(
                phase = CalendarPhase.RegularSeason,
                pointsAvailable = 420,
                cycleClosed = false,
            ),
        )
        assertTrue(tasks.any { it.id == DashboardTaskId.SpendRecruitingPoints })
        assertEquals(420, tasks.first { it.id == DashboardTaskId.SpendRecruitingPoints }.formatArgs.single())
    }

    @Test
    fun noTasks_whenCycleClosed() {
        val tasks = InSeasonRecruitingOptionalDashboardTasks.build(
            InSeasonRecruitingDashboardContext(
                phase = CalendarPhase.RegularSeason,
                pointsAvailable = 200,
                cycleClosed = true,
            ),
        )
        assertTrue(tasks.isEmpty())
    }

    @Test
    fun watchlistOfferNudge_mapsToTaskId() {
        val tasks = InSeasonRecruitingOptionalDashboardTasks.build(
            InSeasonRecruitingDashboardContext(
                phase = CalendarPhase.RegularSeason,
                pointsAvailable = 0,
                cycleClosed = false,
                recruitingNudges = listOf(
                    RecruitingDashboardNudge(
                        RecruitingDashboardNudgeKind.WatchlistOffer,
                        prospectName = "Alex Rivera",
                    ),
                ),
            ),
        )
        assertTrue(tasks.any { it.id == DashboardTaskId.RecruitingWatchlistOffer })
    }
}
