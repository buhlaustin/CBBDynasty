package dev.novux.dynasty.cbb.core.news

/** Hub Stories tab mirrors dashboard metadata: kind chip + optional week line ([MEGA-SHELL-135]). */
object NewsHubStoriesRowPolicy {
    const val MEGA_ID: String = "MEGA-SHELL-135"

    fun showKindChip(): Boolean = true

    fun showWeekIndex(headline: NewsHeadline): Boolean = headline.rsWeekIndex != null
}