package dev.novux.dynasty.cbb.ui.navigation

import androidx.navigation.NavHostController
import dev.novux.dynasty.cbb.core.calendar.CalendarPhase
import dev.novux.dynasty.cbb.core.calendar.SeasonReviewGate
import dev.novux.dynasty.cbb.core.carousel.CoachingCarouselGate
import dev.novux.dynasty.cbb.core.carousel.CoachingCarouselState
import dev.novux.dynasty.cbb.core.coach.employment.CoachEmploymentState
import dev.novux.dynasty.cbb.core.coach.employment.UnemployedCoachShellPolicy
import dev.novux.dynasty.cbb.core.navigation.DynastyCareerPhaseResolver
import dev.novux.dynasty.cbb.core.save.LoadedDynasty

/** Routes after a dynasty load or sign ([planning/game-start.md], [planning/offseason.md]). */
object DynastyLaunch {
    fun navigateForLoaded(
        navController: NavHostController,
        loaded: LoadedDynasty,
        carousel: CoachingCarouselState,
        employment: CoachEmploymentState = CoachEmploymentState(),
    ) {
        when {
            UnemployedCoachShellPolicy.usesUnifiedCareerRoute(employment.status) -> {
                navController.navigate(AppRoute.DynastyCareer.route) {
                    launchSingleTop = true
                }
            }
            SeasonReviewGate.isActive(loaded.weekCheckpoint) -> {
                navController.navigate(AppRoute.SeasonReview.route)
            }
            loaded.weekCheckpoint.activePosition.phase == CalendarPhase.Offseason &&
                CoachingCarouselGate.blocksOffseasonShell(loaded.metadata.seasonIndex, carousel) -> {
                navController.navigate(AppRoute.CoachingCarousel.route)
            }
            DynastyCareerPhaseResolver.careerBody(loaded.weekCheckpoint.activePosition.phase) != null -> {
                navController.navigate(AppRoute.DynastyCareer.route) {
                    launchSingleTop = true
                }
            }
        }
    }
}
