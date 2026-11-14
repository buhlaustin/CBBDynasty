package dev.novux.dynasty.cbb.core.news

/** Portal-related story detail shows View in Portal footer CTA ([MEGA-SHELL-113]). */
object NewsPortalStoryFooterPolicy {
    const val MEGA_ID: String = "MEGA-SHELL-113"

    fun showViewInPortalFooter(headline: NewsHeadline): Boolean = when (headline.kind) {
        NewsHeadlineKind.RecruitingSpotlight -> true
        else -> headline.title.contains("portal", ignoreCase = true) ||
            headline.title.contains("transfer", ignoreCase = true)
    }
}