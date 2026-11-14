package dev.novux.dynasty.cbb.core.dashboard

import dev.novux.dynasty.cbb.core.carousel.CarouselStatus
import dev.novux.dynasty.cbb.core.carousel.CoachingCarouselState

/** Required dashboard tasks during coaching carousel ([MEGA-FRAME-030]). */
object CarouselDashboardTasks {
    fun build(state: CoachingCarouselState): List<DashboardTask> =
        when (state.status) {
            CarouselStatus.AwaitingBackgroundSim -> listOf(
                DashboardTask(
                    id = DashboardTaskId.CarouselLeagueSim,
                    kind = DashboardTaskKind.Required,
                    deepLink = DashboardDeepLink.PreseasonChecklist,
                ),
            )
            CarouselStatus.AwaitingUserApprovals -> state.pendingApprovals.map { card ->
                DashboardTask(
                    id = DashboardTaskId.CarouselUserApproval,
                    kind = DashboardTaskKind.Required,
                    deepLink = DashboardDeepLink.CoachCard,
                    stringFormatArgs = listOf(card.id, card.headline),
                )
            }
            else -> emptyList()
        }
}
