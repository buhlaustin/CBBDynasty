package dev.novux.dynasty.cbb.core.notification

/** Open discipline incidents surface as high-priority dashboard feed rows ([MEGA-SHELL-127]). */
object NotificationFeedDisciplineComposerPolicy {
    const val MEGA_ID: String = "MEGA-SHELL-127"

    val eventKind: NotificationEventKind = NotificationEventKind.DisciplinePending
}