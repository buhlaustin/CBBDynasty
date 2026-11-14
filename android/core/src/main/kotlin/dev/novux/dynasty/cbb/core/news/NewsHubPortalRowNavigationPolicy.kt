package dev.novux.dynasty.cbb.core.news

import dev.novux.dynasty.cbb.core.portal.PortalEntrant

/** Portal row opens prior program context ([MEGA-SHELL-034]). */
object NewsHubPortalRowNavigationPolicy {
    const val MEGA_ID: String = "MEGA-SHELL-034"

    fun priorProgramIdForNavigation(entrant: PortalEntrant): String = entrant.priorProgramId

    fun focusProspectIdForRecruiting(entrant: PortalEntrant): String = entrant.entrantId
}