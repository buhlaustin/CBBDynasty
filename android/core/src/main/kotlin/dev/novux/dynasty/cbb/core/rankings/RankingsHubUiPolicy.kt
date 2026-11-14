package dev.novux.dynasty.cbb.core.rankings

/** Hub entry defaults — program polls, not recruit national ranks ([MEGA-QA-111]). */
object RankingsHubUiPolicy {
    const val DEFAULT_TAB_INDEX: Int = 0
    const val DEFAULT_TAB_IS_TEAM_NET: Boolean = true
    /** Preseason review slice before user publishes via Continue ([MEGA-FRAME-055]). */
    const val UNPUBLISHED_PREVIEW_CAP: Int = 25
}
