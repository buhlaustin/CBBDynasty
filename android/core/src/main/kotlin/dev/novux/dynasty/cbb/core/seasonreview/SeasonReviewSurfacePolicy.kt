package dev.novux.dynasty.cbb.core.seasonreview

/** Season review body before offseason entry ([MEGA-GDD-061]). */
object SeasonReviewSurfacePolicy {
    const val MEGA_ID: String = "MEGA-GDD-061"

    fun hasRecap(snapshot: SeasonReviewSnapshot?): Boolean = snapshot != null
}