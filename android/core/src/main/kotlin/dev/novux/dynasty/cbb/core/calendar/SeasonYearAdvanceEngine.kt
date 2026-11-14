package dev.novux.dynasty.cbb.core.calendar

import dev.novux.dynasty.cbb.core.calendar.checkpoint.WeekCheckpointState

/**
 * End of Season → next membership year offseason entry ([planning/season-review.md], [planning/offseason.md]).
 */
data class SeasonYearAdvancePlan(
    val transition: SeasonReviewTransition,
    val newSeasonIndex: Int,
    val coachingCarouselRequired: Boolean,
)

object SeasonYearAdvanceEngine {
    fun planEnterOffseason(checkpoint: WeekCheckpointState): SeasonYearAdvancePlan {
        val transition = SeasonReviewGate.enterOffseason(checkpoint, seasonReviewAcknowledged = true)
        val nextSeason = transition.advanceResult.nextState.activePosition.seasonIndex
        return SeasonYearAdvancePlan(
            transition = transition,
            newSeasonIndex = nextSeason,
            coachingCarouselRequired = transition.offseasonEntry == OffseasonEntryKind.CoachingCarouselFirst,
        )
    }
}
