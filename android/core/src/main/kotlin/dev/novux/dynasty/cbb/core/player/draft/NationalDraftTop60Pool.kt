package dev.novux.dynasty.cbb.core.player.draft

import dev.novux.dynasty.cbb.core.player.DynastyPlayer
import kotlin.random.Random

/**
 * National Top 60 draft board for college players — same ordering rules as news/draft UI
 * ([MEGA-COACH-077], [planning/player-archetypes.md]).
 */
object NationalDraftTop60Pool {
    const val QA_ID: String = "MEGA-COACH-077"

    const val POOL_SIZE: Int = 60

    private const val NATIONAL_FIELD_SIZE = 220

    private data class DraftPoolEntry(
        val key: String,
        val stockPoints: Int,
        val ovr: Int,
        val isRosterPlayer: Boolean,
    )

    fun rankIndexForRoster(
        roster: List<DynastyPlayer>,
        dynastyId: String,
        seasonIndex: Int,
    ): Map<String, Int> {
        val rosterEntries = roster
            .filter { PlayerDraftProjectionEngine.isDraftEligible(it) }
            .filter { it.role.draftStockPoints >= PlayerDraftProjectionEngine.MIN_STOCK_FOR_TOP_60_POOL }
            .map {
                DraftPoolEntry(
                    key = it.playerId,
                    stockPoints = it.role.draftStockPoints,
                    ovr = it.ratings.ovr,
                    isRosterPlayer = true,
                )
            }
        val nationalField = nationalFieldEntries(dynastyId, seasonIndex)
        val ordered = (rosterEntries + nationalField)
            .sortedWith(
                compareByDescending<DraftPoolEntry> { it.stockPoints }
                    .thenByDescending { it.ovr }
                    .thenBy { it.key },
            )
            .take(POOL_SIZE)
        return ordered.mapIndexedNotNull { index, entry ->
            if (entry.isRosterPlayer) entry.key to (index + 1) else null
        }.toMap()
    }

    private fun nationalFieldEntries(dynastyId: String, seasonIndex: Int): List<DraftPoolEntry> {
        val random = Random(dynastyId.hashCode().toLong() xor (seasonIndex.toLong() * 1_000_003))
        return (1..NATIONAL_FIELD_SIZE).map { slot ->
            DraftPoolEntry(
                key = "national-draft-field-$slot",
                stockPoints = random.nextInt(
                    PlayerDraftProjectionEngine.MIN_STOCK_FOR_TOP_60_POOL,
                    100,
                ),
                ovr = random.nextInt(62, 96),
                isRosterPlayer = false,
            )
        }
    }
}
