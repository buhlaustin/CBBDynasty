package dev.novux.dynasty.cbb.core.notification

import dev.novux.dynasty.cbb.core.dashboard.DashboardDeepLink
import java.util.UUID

data class NotificationSnapshotInput(
    val asOfMs: Long,
    val newInjurySummaries: List<String> = emptyList(),
    val gameThisWeekLabel: String? = null,
    val coachSkillPointsBank: Int = 0,
    val signingDayUnsignedCount: Int = 0,
    val signingDayJustCompleted: Boolean = false,
    val careerFired: Boolean = false,
    val postseasonEliminated: Boolean = false,
    val userCommitsThisWeek: List<String> = emptyList(),
    val userDecommitsThisWeek: List<String> = emptyList(),
    val majorRecordLines: List<String> = emptyList(),
    val rankingsMoveLine: String? = null,
    val disciplineHeadline: String? = null,
    val carouselApprovalBodies: List<String> = emptyList(),
    val selectionSundayBody: String? = null,
    val medicalRedshirtBodies: List<String> = emptyList(),
    val fundraisingCompleteBodies: List<String> = emptyList(),
)

object NotificationFeedComposer {
    fun compose(input: NotificationSnapshotInput): List<NotificationFeedItem> {
        val id = { UUID.randomUUID().toString() }
        return buildList {
            input.disciplineHeadline?.let { headline ->
                add(
                    NotificationFeedItem(
                        id = id(),
                        eventKey = "discipline:${headline.hashCode()}",
                        kind = NotificationEventKind.DisciplinePending,
                        priority = NotificationPriority.High,
                        deepLink = NotificationFeedDisciplineRowPolicy.deepLink,
                        title = NotificationFeedDisciplineRowPolicy.TITLE,
                        body = headline,
                        createdAtMs = input.asOfMs,
                    ),
                )
            }
            input.carouselApprovalBodies.forEach { body ->
                add(
                    NotificationFeedItem(
                        id = id(),
                        eventKey = "carousel:${body.hashCode()}",
                        kind = NotificationEventKind.CarouselApproval,
                        priority = NotificationPriority.High,
                        deepLink = NotificationFeedCarouselRowPolicy.deepLink,
                        title = NotificationFeedCarouselRowPolicy.TITLE,
                        body = body,
                        createdAtMs = input.asOfMs,
                    ),
                )
            }
            input.selectionSundayBody?.let { body ->
                add(
                    NotificationFeedItem(
                        id = id(),
                        eventKey = "selection_sunday",
                        kind = NotificationEventKind.SelectionSunday,
                        priority = NotificationPriority.High,
                        deepLink = NotificationFeedSelectionSundayRowPolicy.deepLink,
                        title = NotificationFeedSelectionSundayRowPolicy.TITLE,
                        body = body,
                        createdAtMs = input.asOfMs,
                    ),
                )
            }
            input.newInjurySummaries.forEach { summary ->
                add(
                    NotificationFeedItem(
                        id = id(),
                        eventKey = "injury:${summary.hashCode()}",
                        kind = NotificationEventKind.UserInjury,
                        priority = NotificationPriority.High,
                        deepLink = DashboardDeepLink.Strategy,
                        title = "Injury report",
                        body = summary,
                        createdAtMs = input.asOfMs,
                    ),
                )
            }
            input.userCommitsThisWeek.forEach { name ->
                add(
                    NotificationFeedItem(
                        id = id(),
                        eventKey = "commit:$name",
                        kind = NotificationEventKind.UserCommit,
                        priority = NotificationPriority.High,
                        deepLink = DashboardDeepLink.RecruitingRecommended,
                        title = "New commit",
                        body = "$name signed with your program.",
                        createdAtMs = input.asOfMs,
                    ),
                )
            }
            input.userDecommitsThisWeek.forEach { name ->
                add(
                    NotificationFeedItem(
                        id = id(),
                        eventKey = "decommit:$name",
                        kind = NotificationEventKind.UserDecommit,
                        priority = NotificationPriority.High,
                        deepLink = DashboardDeepLink.RecruitingRecommended,
                        title = "Decommit",
                        body = "$name reopened recruitment.",
                        createdAtMs = input.asOfMs,
                    ),
                )
            }
            if (input.careerFired) {
                add(
                    NotificationFeedItem(
                        id = id(),
                        eventKey = "career:fired",
                        kind = NotificationEventKind.CareerFired,
                        priority = NotificationPriority.High,
                        deepLink = DashboardDeepLink.CoachCard,
                        title = "Contract ended",
                        body = "The board has released you. Visit the job center to continue your career.",
                        createdAtMs = input.asOfMs,
                        requiresBlockingModal = true,
                    ),
                )
            }
            if (input.postseasonEliminated) {
                add(
                    NotificationFeedItem(
                        id = id(),
                        eventKey = "career:postseason_out",
                        kind = NotificationEventKind.PostseasonEliminated,
                        priority = NotificationPriority.High,
                        deepLink = DashboardDeepLink.CoachCard,
                        title = "Season over",
                        body = "Your team has been eliminated from the postseason.",
                        createdAtMs = input.asOfMs,
                        requiresBlockingModal = true,
                    ),
                )
            }
            input.medicalRedshirtBodies.forEach { body ->
                add(
                    NotificationFeedItem(
                        id = id(),
                        eventKey = "medical_rs:${body.hashCode()}",
                        kind = NotificationEventKind.MedicalRedshirtEligible,
                        priority = NotificationPriority.Medium,
                        deepLink = NotificationFeedMedicalRedshirtRowPolicy.deepLink,
                        title = NotificationFeedMedicalRedshirtRowPolicy.TITLE,
                        body = body,
                        createdAtMs = input.asOfMs,
                    ),
                )
            }
            input.gameThisWeekLabel?.let { label ->
                add(
                    NotificationFeedItem(
                        id = id(),
                        eventKey = "game:${label.hashCode()}",
                        kind = NotificationEventKind.GameThisWeek,
                        priority = NotificationPriority.Medium,
                        deepLink = NotificationFeedGameWeekRowPolicy.deepLink,
                        title = "Game this week",
                        body = label,
                        createdAtMs = input.asOfMs,
                    ),
                )
            }
            if (input.coachSkillPointsBank > 0) {
                add(
                    NotificationFeedItem(
                        id = id(),
                        eventKey = "coach_points:${input.coachSkillPointsBank}",
                        kind = NotificationEventKind.CoachSundayPoints,
                        priority = NotificationPriority.Medium,
                        deepLink = DashboardDeepLink.CoachCard,
                        title = "Coach skill points",
                        body = "You have ${input.coachSkillPointsBank} skill points to assign.",
                        createdAtMs = input.asOfMs,
                    ),
                )
            }
            if (input.signingDayUnsignedCount > 0) {
                add(
                    NotificationFeedItem(
                        id = id(),
                        eventKey = "signing:unsigned",
                        kind = NotificationEventKind.SigningDayPending,
                        priority = NotificationPriority.Medium,
                        deepLink = DashboardDeepLink.SigningDayPositions,
                        title = "Signing Day",
                        body = "${input.signingDayUnsignedCount} prospects still need to sign.",
                        createdAtMs = input.asOfMs,
                    ),
                )
            }
            if (input.signingDayJustCompleted) {
                add(
                    NotificationFeedItem(
                        id = id(),
                        eventKey = "signing:complete",
                        kind = NotificationEventKind.SigningDayComplete,
                        priority = NotificationPriority.Medium,
                        deepLink = DashboardDeepLink.RecruitingRecommended,
                        title = "Class signed",
                        body = "Your incoming class is locked for Signing Day.",
                        createdAtMs = input.asOfMs,
                    ),
                )
            }
            input.majorRecordLines.forEach { line ->
                add(
                    NotificationFeedItem(
                        id = id(),
                        eventKey = "record:${line.hashCode()}",
                        kind = NotificationEventKind.MajorNationalRecord,
                        priority = NotificationPriority.Low,
                        deepLink = DashboardDeepLink.Strategy,
                        title = "National record",
                        body = line,
                        createdAtMs = input.asOfMs,
                    ),
                )
            }
            input.rankingsMoveLine?.let { line ->
                add(
                    NotificationFeedItem(
                        id = id(),
                        eventKey = "rankings:${line.hashCode()}",
                        kind = NotificationEventKind.RankingsMove,
                        priority = NotificationPriority.Low,
                        deepLink = NotificationFeedRankingsMoveRowPolicy.deepLink,
                        title = "Rankings update",
                        body = line,
                        createdAtMs = input.asOfMs,
                    ),
                )
            }
            input.fundraisingCompleteBodies.forEach { body ->
                add(
                    NotificationFeedItem(
                        id = id(),
                        eventKey = "fundraising:${body.hashCode()}",
                        kind = NotificationEventKind.FundraisingComplete,
                        priority = NotificationPriority.Low,
                        deepLink = NotificationFeedFundraisingRowPolicy.deepLink,
                        title = NotificationFeedFundraisingRowPolicy.TITLE,
                        body = body,
                        createdAtMs = input.asOfMs,
                    ),
                )
            }
        }
    }
}
