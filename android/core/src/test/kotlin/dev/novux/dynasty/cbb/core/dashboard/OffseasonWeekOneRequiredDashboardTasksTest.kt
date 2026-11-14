package dev.novux.dynasty.cbb.core.dashboard

import dev.novux.dynasty.cbb.core.roster.PlayerClassYear
import dev.novux.dynasty.cbb.core.roster.RosterPlayer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class OffseasonWeekOneRequiredDashboardTasksTest {
    @Test
    fun pendingProDayAndPortal_emitRequiredTasks() {
        val players = listOf(
            stub(id = "a", portal = true),
            stub(id = "b", proDay = false, senior = true),
        )
        val tasks = OffseasonWeekOneRequiredDashboardTasks.build(players)
        assertEquals(2, tasks.size)
        assertTrue(tasks.any { it.id == DashboardTaskId.WeekOneTransferRetention })
        assertTrue(tasks.any { it.id == DashboardTaskId.WeekOneProDay })
        assertFalse(OffseasonWeekOneRequiredDashboardTasks.canAdvanceWeek(players))
    }

    @Test
    fun allResolved_allowsAdvance() {
        val players = listOf(stub(id = "c", portal = false, proDay = true, senior = true))
        assertTrue(OffseasonWeekOneRequiredDashboardTasks.build(players).isEmpty())
        assertTrue(OffseasonWeekOneRequiredDashboardTasks.canAdvanceWeek(players))
    }

    private fun stub(
        id: String,
        portal: Boolean = false,
        proDay: Boolean = true,
        senior: Boolean = false,
    ) = RosterPlayer(
        playerId = id,
        dynastyId = "d",
        displayName = id,
        position = dev.novux.dynasty.cbb.core.roster.PlayerPosition.PointGuard,
        classYear = if (senior) PlayerClassYear.Senior else PlayerClassYear.Junior,
        isStarter = false,
        depthOrder = 0,
        inTransferPortal = portal,
        declaredForDraft = senior,
        proDayCompleted = proDay,
    )
}
