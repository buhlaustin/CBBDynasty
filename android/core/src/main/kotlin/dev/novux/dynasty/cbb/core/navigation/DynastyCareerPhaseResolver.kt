package dev.novux.dynasty.cbb.core.navigation

import dev.novux.dynasty.cbb.core.calendar.CalendarPhase

/** Maps calendar phase → in-career shell body ([MEGA-FRAME-005], [planning/game-frame.md#entry--shell]). */
object DynastyCareerPhaseResolver {
    enum class CareerBody {
        Offseason,
        Preseason,
        InSeason,
    }

  private val IN_SEASON = setOf(
        CalendarPhase.Exhibition,
        CalendarPhase.RegularSeason,
        CalendarPhase.ConferenceTournament,
        CalendarPhase.Postseason,
    )

    fun careerBody(phase: CalendarPhase): CareerBody? = when (phase) {
        CalendarPhase.Offseason -> CareerBody.Offseason
        CalendarPhase.Preseason -> CareerBody.Preseason
        in IN_SEASON -> CareerBody.InSeason
        else -> null
    }
}
