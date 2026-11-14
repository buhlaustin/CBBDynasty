package dev.novux.dynasty.cbb.core.notification

/** Discipline pending rows in feed use DisciplineSheet deep link ([MEGA-SHELL-110]). */
object NotificationFeedDisciplineFeedPolicy {
    const val MEGA_ID: String = "MEGA-SHELL-110"

    val eventKind: NotificationEventKind = NotificationEventKind.DisciplinePending

    val deepLink = NotificationFeedDisciplineRowPolicy.deepLink
}