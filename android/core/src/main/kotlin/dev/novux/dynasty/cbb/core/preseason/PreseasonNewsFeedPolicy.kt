package dev.novux.dynasty.cbb.core.preseason

/** Preseason shell News tab — Top 60 after incoming class seed ([planning/preseason.md], [MEGA-FRAME-077]). */
object PreseasonNewsFeedPolicy {
    const val MEGA_FRAME_ID: String = "MEGA-FRAME-077"
    const val TOP60_HEADLINE_ID_PREFIX: String = "top60-"

    fun top60StoryId(seasonIndex: Int): String = "$TOP60_HEADLINE_ID_PREFIX$seasonIndex"
}
