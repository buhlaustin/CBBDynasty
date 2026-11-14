package dev.novux.dynasty.cbb.core.notification

/** Carousel approval / extension cards are composed into the high-priority feed ([MEGA-SHELL-125]). */
object NotificationFeedCarouselComposerPolicy {
    const val MEGA_ID: String = "MEGA-SHELL-125"

    val eventKind: NotificationEventKind = NotificationEventKind.CarouselApproval

    val deepLink = NotificationFeedCarouselRowPolicy.deepLink
}