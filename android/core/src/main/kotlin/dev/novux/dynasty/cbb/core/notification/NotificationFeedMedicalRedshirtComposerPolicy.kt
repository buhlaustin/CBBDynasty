package dev.novux.dynasty.cbb.core.notification

/** Medical redshirt eligibility bodies in notification composer ([MEGA-SHELL-126]). */
object NotificationFeedMedicalRedshirtComposerPolicy {
    const val MEGA_ID: String = "MEGA-SHELL-126"

    val eventKind: NotificationEventKind = NotificationEventKind.MedicalRedshirtEligible
}