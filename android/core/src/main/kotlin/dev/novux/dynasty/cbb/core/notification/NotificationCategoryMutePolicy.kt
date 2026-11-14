package dev.novux.dynasty.cbb.core.notification

/** Per-category mute filters visible dashboard rows without disabling the whole feed ([MEGA-SHELL-121]). */
object NotificationCategoryMutePolicy {
    const val MEGA_ID: String = "MEGA-SHELL-121"

    fun categoryFor(row: NotificationFeedItem): NotificationFeedCategory =
        when (row.kind) {
            NotificationEventKind.UserCommit,
            NotificationEventKind.UserDecommit,
            NotificationEventKind.SigningDayPending,
            NotificationEventKind.SigningDayComplete,
            -> NotificationFeedCategory.Recruiting

            NotificationEventKind.UserInjury,
            NotificationEventKind.MedicalRedshirtEligible,
            -> NotificationFeedCategory.RosterHealth

            NotificationEventKind.CoachSundayPoints,
            NotificationEventKind.CareerFired,
            NotificationEventKind.PostseasonEliminated,
            NotificationEventKind.CarouselApproval,
            -> NotificationFeedCategory.CoachCareer

            NotificationEventKind.GameThisWeek,
            NotificationEventKind.SelectionSunday,
            NotificationEventKind.DisciplinePending,
            -> NotificationFeedCategory.GameWeek

            NotificationEventKind.MajorNationalRecord,
            NotificationEventKind.RankingsMove,
            NotificationEventKind.FundraisingComplete,
            -> NotificationFeedCategory.LeagueUpdates
        }

    fun filterVisible(
        feed: List<NotificationFeedItem>,
        mutedCategories: Set<NotificationFeedCategory>,
    ): List<NotificationFeedItem> =
        feed.filter { categoryFor(it) !in mutedCategories }
}