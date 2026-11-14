package dev.novux.dynasty.cbb.core.notification

import dev.novux.dynasty.cbb.core.dashboard.DashboardDeepLink

enum class NotificationPriority {
    High,
    Medium,
    Low,
}

enum class NotificationEventKind {
    UserInjury,
    GameThisWeek,
    CoachSundayPoints,
    UserCommit,
    UserDecommit,
    SigningDayPending,
    SigningDayComplete,
    CareerFired,
    PostseasonEliminated,
    MajorNationalRecord,
    RankingsMove,
    FundraisingComplete,
    DisciplinePending,
    CarouselApproval,
    SelectionSunday,
    MedicalRedshirtEligible,
}

/**
 * In-app dashboard alert row ([planning/notifications.md]) — not OS push, not News.
 */
data class NotificationFeedItem(
    val id: String,
    val eventKey: String,
    val kind: NotificationEventKind,
    val priority: NotificationPriority,
    val deepLink: DashboardDeepLink,
    val title: String,
    val body: String,
    val createdAtMs: Long,
    val requiresBlockingModal: Boolean = false,
    val modalAcknowledged: Boolean = false,
)
