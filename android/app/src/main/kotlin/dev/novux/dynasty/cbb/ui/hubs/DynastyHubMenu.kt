package dev.novux.dynasty.cbb.ui.hubs

import dev.novux.dynasty.cbb.R
import dev.novux.dynasty.cbb.core.calendar.CalendarPhase
import dev.novux.dynasty.cbb.core.calendar.CalendarPosition
import dev.novux.dynasty.cbb.core.hubs.DynastyHubMenuPolicy
import dev.novux.dynasty.cbb.core.recruiting.RecruitingCyclePolicy
import dev.novux.dynasty.cbb.ui.dynasty.DynastyShellMenuAction

data class DynastyHubMenuCallbacks(
    val onOpenSchool: () -> Unit,
    val onOpenRecruiting: () -> Unit,
    val onOpenStrategy: () -> Unit,
    val onOpenStatistics: () -> Unit,
    val onOpenRankings: () -> Unit,
    val onOpenNewsHub: () -> Unit,
    val onOpenNewsHubPortal: () -> Unit,
    val onOpenHistory: () -> Unit,
    val onOpenAwards: () -> Unit,
    val onOpenMarchMayhem: () -> Unit,
    val onOpenConferenceTournament: () -> Unit,
    val onExitHome: () -> Unit,
)

fun buildDynastyHubMenu(
    position: CalendarPosition,
    callbacks: DynastyHubMenuCallbacks,
    selectionSundayPublished: Boolean = false,
    recruitingCycleClosed: Boolean = false,
    onRecruitingCycleClosed: () -> Unit = {},
    leadingExtras: List<DynastyShellMenuAction> = emptyList(),
    trailingExtras: List<DynastyShellMenuAction> = emptyList(),
): List<DynastyShellMenuAction> {
    val offseasonWeek = position.weekIndex.takeIf { position.phase == CalendarPhase.Offseason }
    val core = buildList {
        add(DynastyShellMenuAction(R.string.school_hub_open, callbacks.onOpenSchool))
        when {
            RecruitingCyclePolicy.hubNavigable(position.phase, offseasonWeek, recruitingCycleClosed) ->
                add(DynastyShellMenuAction(R.string.shell_menu_recruiting_hub, callbacks.onOpenRecruiting))
            recruitingCycleClosed &&
                RecruitingCyclePolicy.hubAccessible(position.phase, offseasonWeek) ->
                add(
                    DynastyShellMenuAction(R.string.shell_menu_recruiting_closed) {
                        onRecruitingCycleClosed()
                    },
                )
        }
        add(DynastyShellMenuAction(R.string.dynasty_shell_open_strategy, callbacks.onOpenStrategy))
        add(DynastyShellMenuAction(R.string.shell_menu_statistics_hub, callbacks.onOpenStatistics))
        add(DynastyShellMenuAction(R.string.shell_menu_rankings_hub, callbacks.onOpenRankings))
        add(DynastyShellMenuAction(R.string.shell_menu_news_hub, callbacks.onOpenNewsHub))
        add(DynastyShellMenuAction(R.string.shell_menu_history_hub, callbacks.onOpenHistory))
        add(DynastyShellMenuAction(R.string.shell_menu_awards_hub, callbacks.onOpenAwards))
        if (DynastyHubMenuPolicy.conferenceTournamentMenuVisible(position)) {
            add(DynastyShellMenuAction(R.string.shell_menu_conf_tourney, callbacks.onOpenConferenceTournament))
        }
        if (DynastyHubMenuPolicy.marchMayhemMenuVisible(position, selectionSundayPublished)) {
            add(DynastyShellMenuAction(R.string.shell_menu_march_mayhem, callbacks.onOpenMarchMayhem))
        }
        add(DynastyShellMenuAction(R.string.shell_menu_exit_home, callbacks.onExitHome))
    }
    return leadingExtras + core + trailingExtras
}
