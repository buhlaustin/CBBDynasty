package dev.novux.dynasty.cbb.core.news

/**
 * Portal hub tab is listed only during the portal recruiting window ([MEGA-SHELL-028]).
 */
object NewsHubPortalTabVisibilityPolicy {
    const val MEGA_ID: String = "MEGA-SHELL-028"

    fun showPortalTab(portalWindowOpen: Boolean): Boolean = portalWindowOpen
}