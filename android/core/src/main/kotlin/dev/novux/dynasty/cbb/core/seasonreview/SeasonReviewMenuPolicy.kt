package dev.novux.dynasty.cbb.core.seasonreview

import dev.novux.dynasty.cbb.core.calendar.CalendarPhase
import dev.novux.dynasty.cbb.core.calendar.CalendarPosition

/** Season review shell uses the same hamburger hub matrix as other career phases ([MEGA-SHELL-104]). */
object SeasonReviewMenuPolicy {
    const val MEGA_ID: String = "MEGA-SHELL-104"

    fun usesFullDynastyHubMenu(position: CalendarPosition): Boolean =
        position.phase == CalendarPhase.SeasonReview
}