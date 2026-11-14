package dev.novux.dynasty.cbb.core.dashboard

import dev.novux.dynasty.cbb.core.discipline.DisciplineIncidentKind
import dev.novux.dynasty.cbb.core.discipline.PendingDisciplineIncident
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DisciplineRequiredDashboardTasksTest {
    @Test
    fun pendingIncident_emitsRequiredDisciplineTask() {
        val incident = PendingDisciplineIncident(
            playerId = "p1",
            playerDisplayName = "Alex Smith",
            kind = DisciplineIncidentKind.Conduct,
            headline = "PG arrested last night",
            flavorBody = "Details",
            recordedAtRsWeek = 4,
        )
        val tasks = DisciplineRequiredDashboardTasks.build(incident)
        assertEquals(1, tasks.size)
        assertEquals(DashboardTaskId.TeamDisciplinePending, tasks.single().id)
        assertEquals(DashboardDeepLink.DisciplineSheet, tasks.single().deepLink)
        assertEquals("PG arrested last night", tasks.single().stringFormatArgs.single())
    }

    @Test
    fun noPending_returnsEmpty() {
        assertTrue(DisciplineRequiredDashboardTasks.build(null).isEmpty())
    }
}
