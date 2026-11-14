package dev.novux.dynasty.cbb.core.player.loyalty

data class PlayerLoyaltyProfile(
    val loyalty: LoyaltyRating,
    val personality: LoyaltyPersonalityTag,
    val promise: CoachPromise?,
    val promiseHistory: List<PlayerPromiseHistoryEntry> = emptyList(),
) {
    fun overviewLoyaltyLine(): String = "Loyalty: ${loyalty.value}"

    fun overviewPromiseLine(): String? {
        val promise = promise ?: return null
        return "Promise: ${promise.displayLabel()} — ${promise.statusChipLabel()}"
    }

    companion object {
        val Empty = PlayerLoyaltyProfile(
            LoyaltyRating.Default,
            LoyaltyPersonalityTag.ProgramGuy,
            promise = null,
            promiseHistory = emptyList(),
        )
    }
}
