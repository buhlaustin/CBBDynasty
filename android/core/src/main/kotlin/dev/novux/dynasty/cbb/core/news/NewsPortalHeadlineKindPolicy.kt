package dev.novux.dynasty.cbb.core.news

/** Portal headline taxonomy in the shared Stories archive ([MEGA-SHELL-136]). */
object NewsPortalHeadlineKindPolicy {
    const val MEGA_ID: String = "MEGA-SHELL-136"

    val portalKinds: Set<NewsHeadlineKind> = setOf(
        NewsHeadlineKind.PortalEntry,
        NewsHeadlineKind.PortalCommit,
        NewsHeadlineKind.PortalDecommit,
        NewsHeadlineKind.PortalFlip,
    )

    fun isPortalKind(kind: NewsHeadlineKind): Boolean = kind in portalKinds
}