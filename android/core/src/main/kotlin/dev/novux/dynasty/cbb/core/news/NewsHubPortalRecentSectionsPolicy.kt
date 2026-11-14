package dev.novux.dynasty.cbb.core.news

import dev.novux.dynasty.cbb.core.portal.PortalEntrant
import dev.novux.dynasty.cbb.core.portal.PortalTop40

/**
 * Portal tab shows national Top 40 plus recent unranked entrants ([MEGA-SHELL-032]).
 */
object NewsHubPortalRecentSectionsPolicy {
    const val MEGA_ID: String = "MEGA-SHELL-032"

    fun top40(entrants: List<PortalEntrant>): List<PortalEntrant> =
        PortalTop40.rankedList(entrants)

    fun recentEntrants(entrants: List<PortalEntrant>, cap: Int = 8): List<PortalEntrant> =
        entrants
            .filter { it.nationalTop40Rank == null }
            .take(cap)
}