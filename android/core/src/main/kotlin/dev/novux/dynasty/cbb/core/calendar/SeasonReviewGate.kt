package dev.novux.dynasty.cbb.core.calendar

import dev.novux.dynasty.cbb.core.calendar.checkpoint.WeekCheckpointState

/**
 * End of Season week — non-game gate before offseason ([planning/season-review.md]).
 */
object SeasonReviewGate {
    fun isActive(state: WeekCheckpointState): Boolean =
        state.activePosition.phase == CalendarPhase.SeasonReview

    fun canEnterOffseason(state: WeekCheckpointState, seasonReviewAcknowledged: Boolean): Boolean =
        isActive(state) && seasonReviewAcknowledged

    /**
     * Commits the review week and rolls calendar to **Offseason Week 1** of the next membership year.
     */
    fun enterOffseason(
        state: WeekCheckpointState,
        seasonReviewAcknowledged: Boolean,
    ): SeasonReviewTransition {
        require(canEnterOffseason(state, seasonReviewAcknowledged)) {
            "not in End of Season week or season recap not acknowledged"
        }
        val completedSeasonIndex = state.activePosition.seasonIndex
        val advance = state.advanceWeek()
        val next = advance.nextState.activePosition
        require(next.phase == CalendarPhase.Offseason && next.weekIndex == 1) {
            "expected offseason week 1 after season review"
        }
        val entry = OffseasonEntryRouter.entryAfterSeasonReview(next.seasonIndex)
        return SeasonReviewTransition(
            advanceResult = advance,
            completedSeasonIndex = completedSeasonIndex,
            offseasonEntry = entry,
        )
    }
}

enum class OffseasonEntryKind {
    /** Brand-new dynasty signing — no carousel ([planning/offseason.md]). */
    WeekOnePlayersLeaving,
    /** Season 2+ entry — coaching carousel before week 1 ([planning/offseason.md#coaching-carousel]). */
    CoachingCarouselFirst,
}

object OffseasonEntryRouter {
    fun entryAfterSeasonReview(newSeasonIndex: Int): OffseasonEntryKind =
        if (newSeasonIndex <= 1) {
            OffseasonEntryKind.WeekOnePlayersLeaving
        } else {
            OffseasonEntryKind.CoachingCarouselFirst
        }
}

data class SeasonReviewTransition(
    val advanceResult: WeekCheckpointState.AdvanceResult,
    val completedSeasonIndex: Int,
    val offseasonEntry: OffseasonEntryKind,
)
