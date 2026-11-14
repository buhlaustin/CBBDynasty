package dev.novux.dynasty.cbb.core.carousel

/**
 * Carousel flow — background sim then user approvals ([planning/offseason.md], [planning/game-design.md], [MEGA-GDD-119]).
 */
object CoachingCarouselEngine {
    const val MEGA_ID: String = "MEGA-GDD-119"
    fun initialState(membershipSeasonIndex: Int): CoachingCarouselState =
        if (!CoachingCarouselPolicy.requiredForMembershipYear(membershipSeasonIndex)) {
            CoachingCarouselState(CarouselStatus.NotRequired)
        } else {
            CoachingCarouselState(CarouselStatus.AwaitingBackgroundSim)
        }

    fun runBackgroundSim(
        state: CoachingCarouselState,
        context: CarouselSimContext,
    ): CoachingCarouselState {
        require(state.status == CarouselStatus.AwaitingBackgroundSim) {
            "background sim already run"
        }
        val seeded = context.copy(
            backgroundSimSeedSalt = state.backgroundSimGeneration,
        )
        val background = CarouselLeagueBackgroundSimEngine.run(seeded)
        val cards = CarouselUserApprovalPlanner.pendingCards(seeded, background)
        val nextStatus = if (cards.isEmpty()) {
            CarouselStatus.Complete
        } else {
            CarouselStatus.AwaitingUserApprovals
        }
        return state.copy(
            status = nextStatus,
            pendingApprovals = cards,
            backgroundSimGeneration = state.backgroundSimGeneration + 1,
            backgroundSimSummary = background.summary,
        )
    }

    fun resolveApproval(
        state: CoachingCarouselState,
        cardId: String,
        accepted: Boolean,
    ): CoachingCarouselState {
        require(state.status == CarouselStatus.AwaitingUserApprovals) {
            "no approvals pending"
        }
        val remaining = state.pendingApprovals.filterNot { it.id == cardId }
        val nextStatus = if (remaining.isEmpty()) CarouselStatus.Complete else CarouselStatus.AwaitingUserApprovals
        return state.copy(status = nextStatus, pendingApprovals = remaining)
    }

    fun skipToComplete(state: CoachingCarouselState): CoachingCarouselState =
        state.copy(status = CarouselStatus.Complete, pendingApprovals = emptyList())
}

object CoachingCarouselGate {
    fun blocksOffseasonShell(
        membershipSeasonIndex: Int,
        carousel: CoachingCarouselState,
    ): Boolean =
        CoachingCarouselPolicy.requiredForMembershipYear(membershipSeasonIndex) &&
            !carousel.canEnterOffseasonWeekOne()
}
