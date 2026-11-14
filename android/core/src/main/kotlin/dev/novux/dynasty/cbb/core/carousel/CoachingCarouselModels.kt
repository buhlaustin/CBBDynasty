package dev.novux.dynasty.cbb.core.carousel

enum class CarouselStatus {
    /** Season 1 — carousel skipped. */
    NotRequired,
    /** Background AI churn not simulated yet. */
    AwaitingBackgroundSim,
    /** User must resolve offer / extension / assistant cards. */
    AwaitingUserApprovals,
    /** Carousel finished; Offseason Week 1 unlocks. */
    Complete,
}

enum class CarouselApprovalKind {
    ContractExtension,
    HeadCoachOffer,
    AssistantHire,
}

data class CarouselApprovalCard(
    val id: String,
    val kind: CarouselApprovalKind,
    val headline: String,
    val detail: String,
)

data class CoachingCarouselState(
    val status: CarouselStatus,
    val pendingApprovals: List<CarouselApprovalCard> = emptyList(),
    val backgroundSimGeneration: Int = 0,
    val backgroundSimSummary: CarouselLeagueBackgroundSimEngine.CarouselBackgroundSimSummary? = null,
) {
    fun canEnterOffseasonWeekOne(): Boolean =
        status == CarouselStatus.Complete || status == CarouselStatus.NotRequired
}
