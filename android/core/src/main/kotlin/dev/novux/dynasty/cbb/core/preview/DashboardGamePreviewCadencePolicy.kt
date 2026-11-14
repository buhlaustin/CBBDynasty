package dev.novux.dynasty.cbb.core.preview

import dev.novux.dynasty.cbb.core.preseason.FirstPlayableGame

/** Dashboard next-game card should surface game preview entry ([MEGA-GDD-054]). */
object DashboardGamePreviewCadencePolicy {
    const val MEGA_ID: String = "MEGA-GDD-054"

    fun showPreviewHint(game: FirstPlayableGame?): Boolean = game != null
}