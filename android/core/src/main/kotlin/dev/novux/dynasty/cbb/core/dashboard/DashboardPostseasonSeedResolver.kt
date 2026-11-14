package dev.novux.dynasty.cbb.core.dashboard

import dev.novux.dynasty.cbb.core.selection.SelectionSundaySnapshot

/**
 * Dashboard postseason row ([planning/game-frame.md#postseason-row-dashboard], [MEGA-FRAME-036]).
 */
object DashboardPostseasonSeedResolver {
    fun label(userTeamId: String, selection: SelectionSundaySnapshot?): String? {
        if (selection == null) return null
        selection.ncbaField.firstOrNull { it.teamId == userTeamId }?.let {
            return "NCBA #${it.ncbaSeed}"
        }
        selection.nciField.firstOrNull { it.teamId == userTeamId }?.let {
            return "NCI #${it.seed}"
        }
        selection.heritageField.firstOrNull { it.teamId == userTeamId }?.let {
            return "HC #${it.seed}"
        }
        return null
    }
}
