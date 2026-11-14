package dev.novux.dynasty.cbb.core.carousel

/**
 * Coaching Carousel — Season 2+ only ([planning/offseason.md#coaching-carousel]).
 */
object CoachingCarouselPolicy {
    /** Offseason carousel gate before week 1 ([MEGA-GDD-026]). */
    const val MEGA_ID: String = "MEGA-GDD-026"

    fun requiredForMembershipYear(seasonIndex: Int): Boolean = seasonIndex >= 2
}
