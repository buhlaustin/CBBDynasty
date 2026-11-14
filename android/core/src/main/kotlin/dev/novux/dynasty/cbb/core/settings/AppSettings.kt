package dev.novux.dynasty.cbb.core.settings

import dev.novux.dynasty.cbb.core.notification.NotificationFeedCategory

/**
 * App-wide preferences (not dynasty rules) — [planning/game-start.md#home-settings--credits].
 */
data class AppSettings(
    val skipStudioIntro: Boolean = false,
    val sfxEnabled: Boolean = true,
    val musicEnabled: Boolean = true,
    val masterVolumePercent: Int = DEFAULT_MASTER_VOLUME,
    /** In-app dashboard notification feed ([planning/notifications.md#defaults--settings]). */
    val showDashboardNotificationFeed: Boolean = true,
    /** Muted notification feed buckets ([MEGA-SHELL-121]). */
    val mutedNotificationFeedCategories: Set<NotificationFeedCategory> = emptySet(),
    /** Default for new dynasties — sim-week recruiting auto-maintain. */
    val defaultRecruitingAutoMaintain: Boolean = true,
    /** Default for new dynasties — head coach skill auto-spend. */
    val defaultCoachAutoSpend: Boolean = false,
) {
    init {
        require(masterVolumePercent in 0..100) {
            "masterVolumePercent must be in 0..100"
        }
    }

    companion object {
        const val DEFAULT_MASTER_VOLUME = 100
    }
}
