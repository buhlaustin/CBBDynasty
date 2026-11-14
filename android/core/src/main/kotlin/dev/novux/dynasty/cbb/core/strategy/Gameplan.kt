package dev.novux.dynasty.cbb.core.strategy

import dev.novux.dynasty.cbb.core.roster.PlayerPosition

/** Pre-game / halftime prep — [planning/strategy-schemes.md#gameplan-fields-locked-for-planning]. */
enum class LateGameFoulStrategy(val wireId: String) {
    Standard("standard"),
    FoulEarly("foul_early"),
    ProtectLead("protect_lead"),
    ;

    companion object {
        fun fromWire(id: String): LateGameFoulStrategy? = entries.firstOrNull { it.wireId == id }
    }
}

data class GameplanInput(
    val usageWeights: Map<String, Int> = emptyMap(),
    val denyPrimaryScorer: Boolean = false,
    /** Opponent slot treated as primary scorer when deny is on. */
    val primaryScorerSlot: PlayerPosition = PlayerPosition.PointGuard,
    /** Opponent slot (PG–C) → your defender player id. */
    val defensiveAssignments: Map<PlayerPosition, String> = emptyMap(),
    val lateGameFoulStrategy: LateGameFoulStrategy = LateGameFoulStrategy.Standard,
) {
    fun forSecondHalf(patch: GameplanInput): GameplanInput = patch
}

object GameplanUsageNormalizer {
    fun normalize(weights: Map<String, Int>): Map<String, Double> {
        if (weights.isEmpty()) return emptyMap()
        val total = weights.values.sum().coerceAtLeast(1)
        return weights.mapValues { (_, v) -> v.toDouble() / total }
    }

    fun defaultForStarters(starterIds: Collection<String>): Map<String, Int> {
        if (starterIds.isEmpty()) return emptyMap()
        val share = 100 / starterIds.size
        return starterIds.associateWith { share }
    }
}
