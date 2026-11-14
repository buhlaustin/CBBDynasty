package dev.novux.dynasty.cbb.core.news

/** Shell and hub story headlines open mini-article detail on tap ([MEGA-SHELL-112]). */
object NewsStoryDrillInPolicy {
    const val MEGA_ID: String = "MEGA-SHELL-112"

    fun opensMiniArticleDetail(): Boolean = true

    fun articleFor(headline: NewsHeadline) = NewsHeadlineArticleBuilder.miniArticle(headline)
}