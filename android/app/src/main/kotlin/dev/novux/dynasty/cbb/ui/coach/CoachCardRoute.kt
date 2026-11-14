package dev.novux.dynasty.cbb.ui.coach

import androidx.compose.runtime.saveable.mapSaver

/**
 * Navigation route for full-screen coach cards with tab selection that survives back navigation
 * and process death ([MEGA-COACH-108]).
 */
sealed class CoachCardRoute {
    abstract val dynastyId: String
    abstract val selectedTab: CoachCardTab

    abstract fun withTab(tab: CoachCardTab): CoachCardRoute

    data class HeadCoach(
        override val dynastyId: String,
        val selectedTabWire: String = CoachCardTab.Overview.wire,
    ) : CoachCardRoute() {
        override val selectedTab: CoachCardTab
            get() = CoachCardTab.fromWire(selectedTabWire) ?: CoachCardTab.Overview

        override fun withTab(tab: CoachCardTab): HeadCoach = copy(selectedTabWire = tab.wire)
    }

    data class Assistant(
        override val dynastyId: String,
        val staffId: String,
        val selectedTabWire: String = CoachCardTab.Overview.wire,
    ) : CoachCardRoute() {
        override val selectedTab: CoachCardTab
            get() = CoachCardTab.fromWire(selectedTabWire) ?: CoachCardTab.Overview

        override fun withTab(tab: CoachCardTab): Assistant = copy(selectedTabWire = tab.wire)
    }

    companion object {
        val HeadCoachSaver = mapSaver(
            save = { route ->
                mapOf(
                    "dynastyId" to route.dynastyId,
                    "tab" to route.selectedTabWire,
                )
            },
            restore = { saved ->
                HeadCoach(
                    dynastyId = saved["dynastyId"] as String,
                    selectedTabWire = saved["tab"] as String,
                )
            },
        )
    }
}
