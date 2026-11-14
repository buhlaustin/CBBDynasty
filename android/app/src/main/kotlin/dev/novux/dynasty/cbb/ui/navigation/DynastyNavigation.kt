package dev.novux.dynasty.cbb.ui.navigation

import androidx.navigation.NavHostController
import dev.novux.dynasty.cbb.core.navigation.DynastyDashboardBackTargets
import dev.novux.dynasty.cbb.core.navigation.HubRouteShellRetentionPolicy
import dev.novux.dynasty.cbb.data.save.ActiveDynastySession

/** Pops to the active in-career dashboard — never the main menu ([MEGA-QA-046], [HubRouteShellRetentionPolicy]). */
fun NavHostController.popBackToInShellDashboard(): Boolean {
    check(HubRouteShellRetentionPolicy.restoresShellDashboardTabOnHubBack())
    for (route in DynastyDashboardBackTargets.ORDERED) {
        if (popBackStack(route, inclusive = false)) {
            ActiveDynastySession.requestShellDashboardTabOnHubReturn()
            return true
        }
    }
    val popped = popBackStack()
    if (popped) {
        ActiveDynastySession.requestShellDashboardTabOnHubReturn()
    }
    return popped
}

fun NavHostController.navigateToRecruitingHub() {
    navigate(AppRoute.Recruiting.route) {
        launchSingleTop = true
    }
}
