package dev.novux.dynasty.cbb.core.dashboard

import dev.novux.dynasty.cbb.core.carousel.CarouselApprovalKind
import dev.novux.dynasty.cbb.core.carousel.CarouselApprovalCard
import dev.novux.dynasty.cbb.core.carousel.CarouselStatus
import dev.novux.dynasty.cbb.core.carousel.CoachingCarouselState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CarouselDashboardTasksTest {
    @Test
    fun awaitingSim_emitsLeagueSimRequiredTask() {
        val tasks = CarouselDashboardTasks.build(
            CoachingCarouselState(CarouselStatus.AwaitingBackgroundSim),
        )
        assertEquals(1, tasks.size)
        assertEquals(DashboardTaskId.CarouselLeagueSim, tasks.single().id)
        assertEquals(DashboardTaskKind.Required, tasks.single().kind)
    }

    @Test
    fun pendingApprovals_emitRequiredTasksPerCard() {
        val state = CoachingCarouselState(
            status = CarouselStatus.AwaitingUserApprovals,
            pendingApprovals = listOf(
                CarouselApprovalCard("c1", CarouselApprovalKind.ContractExtension, "Extend HC", "Details"),
            ),
        )
        val tasks = CarouselDashboardTasks.build(state)
        assertEquals(1, tasks.size)
        assertTrue(tasks.all { it.id == DashboardTaskId.CarouselUserApproval })
    }
}
