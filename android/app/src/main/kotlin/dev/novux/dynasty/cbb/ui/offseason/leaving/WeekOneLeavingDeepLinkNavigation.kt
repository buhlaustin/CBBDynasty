package dev.novux.dynasty.cbb.ui.offseason.leaving

import dev.novux.dynasty.cbb.core.dashboard.DashboardDeepLink

fun weekOneNavigationFor(deepLink: DashboardDeepLink): WeekOneLeavingNavigation? =
    when (deepLink) {
        DashboardDeepLink.PlayersLeavingRoster -> WeekOneLeavingNavigation(tabIndex = 0)
        DashboardDeepLink.PlayersLeavingTransfer -> WeekOneLeavingNavigation(tabIndex = 1)
        DashboardDeepLink.PlayersLeavingProDays -> WeekOneLeavingNavigation(tabIndex = 0, emphasizeProDays = true)
        else -> null
    }
