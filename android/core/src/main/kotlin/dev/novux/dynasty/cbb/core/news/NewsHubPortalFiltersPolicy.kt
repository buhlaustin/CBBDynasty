package dev.novux.dynasty.cbb.core.news

import dev.novux.dynasty.cbb.core.portal.PortalEntrant

/** Portal news filters — conference, stars, position, pipeline ([MEGA-SHELL-033]). */
object NewsHubPortalFiltersPolicy {
    const val MEGA_ID: String = "MEGA-SHELL-033"

    enum class StarFilter { All, FourPlus, FiveOnly }

    fun matchesStars(entrant: PortalEntrant, filter: StarFilter): Boolean {
        val stars = entrant.stars ?: return filter == StarFilter.All
        return when (filter) {
            StarFilter.All -> true
            StarFilter.FourPlus -> stars >= 4
            StarFilter.FiveOnly -> stars >= 5
        }
    }
}