package dev.novux.dynasty.cbb.core.notification

/** Medical redshirt eligible bodies ship in notification composer ([MEGA-SHELL-111]). */
object NotificationFeedMedicalRedshirtFeedPolicy {
    const val MEGA_ID: String = "MEGA-SHELL-111"

    val eventKind: NotificationEventKind = NotificationEventKind.MedicalRedshirtEligible

    val deepLink = NotificationFeedMedicalRedshirtRowPolicy.deepLink
}