package dev.novux.dynasty.cbb.core.calendar

import kotlinx.serialization.Serializable

/**
 * Dynasty calendar phase — see [planning/time-system.md] and [planning/offseason.md].
 */
@Serializable
enum class CalendarPhase {
    Offseason,
    Preseason,
    Exhibition,
    RegularSeason,
    ConferenceTournament,
    Postseason,
    SeasonReview,
}
