package dev.novux.dynasty.cbb.core.notification

/**
 * Optional dashboard notification inputs loaded outside [NotificationFeedComposer]'s baseline snapshot.
 */
data class NotificationSnapshotSidecar(
    val disciplineHeadline: String? = null,
    val carouselApprovalBodies: List<String> = emptyList(),
    val selectionSundayBody: String? = null,
    val medicalRedshirtBodies: List<String> = emptyList(),
    val fundraisingCompleteBodies: List<String> = emptyList(),
)