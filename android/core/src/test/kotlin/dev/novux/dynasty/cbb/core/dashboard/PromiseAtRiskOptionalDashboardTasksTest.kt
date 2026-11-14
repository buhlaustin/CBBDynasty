package dev.novux.dynasty.cbb.core.dashboard

import dev.novux.dynasty.cbb.core.player.loyalty.CoachPromiseType
import dev.novux.dynasty.cbb.core.player.loyalty.PromiseStatus
import dev.novux.dynasty.cbb.core.roster.PlayerClassYear
import dev.novux.dynasty.cbb.core.roster.PlayerPosition
import dev.novux.dynasty.cbb.core.roster.RosterPlayer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PromiseAtRiskOptionalDashboardTasksTest {
    @Test
    fun atRiskRosterPlayer_emitsDashboardTask() {
        val player = RosterPlayer(
            playerId = "p1",
            dynastyId = "d1",
            position = PlayerPosition.PointGuard,
            classYear = PlayerClassYear.Junior,
            isStarter = false,
            depthOrder = 8,
            inTransferPortal = false,
            displayName = "Jay Sloan",
            promiseTypeWire = CoachPromiseType.MinutesStarter.wire,
            promiseStatusWire = PromiseStatus.AtRisk.wire,
        )
        val tasks = PromiseAtRiskOptionalDashboardTasks.build(listOf(player))
        assertEquals(1, tasks.size)
        assertTrue(tasks.single().id == DashboardTaskId.PromiseAtRisk)
        assertEquals("Jay Sloan", tasks.single().stringFormatArgs.first())
    }
}
