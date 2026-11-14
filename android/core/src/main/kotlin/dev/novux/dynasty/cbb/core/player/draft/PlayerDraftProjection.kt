package dev.novux.dynasty.cbb.core.player.draft

import dev.novux.dynasty.cbb.core.player.DynastyPlayer
import dev.novux.dynasty.cbb.core.roster.PlayerClassYear

/**
 * Projected draft slot on player Overview ([MEGA-COACH-065], [planning/player-archetypes.md]).
 */
/** National Top 60 draft slot when ranked on the player card ([planning/player-archetypes.md]). */
typealias DraftProjection = PlayerDraftProjection

data class PlayerDraftProjection(
    val rank: Int?,
    val tierLabel: String?,
) {
    val isAvailable: Boolean get() = rank != null

    companion object {
        val NotAvailable = PlayerDraftProjection(rank = null, tierLabel = null)
    }
}

object PlayerDraftProjectionEngine {
    const val QA_ID: String = "MEGA-COACH-065"

    const val MIN_STOCK_FOR_TOP_60_POOL: Int = 40

    /**
     * Resolves [DraftProjection] from national Top 60 ranks when [nationalTop60RankByPlayerId] is
     * supplied (player card path). Falls back to stock-derived mock slot only when the map is empty
     * (legacy previews/tests).
     */
    fun resolve(
        player: DynastyPlayer,
        nationalTop60RankByPlayerId: Map<String, Int> = emptyMap(),
    ): PlayerDraftProjection {
        if (!isDraftEligible(player)) return PlayerDraftProjection.NotAvailable
        val usesNationalPool =
            player.nationalTop60DraftRank != null || nationalTop60RankByPlayerId.isNotEmpty()
        if (usesNationalPool) {
            val poolRank = player.nationalTop60DraftRank
                ?: nationalTop60RankByPlayerId[player.playerId]
                ?: return PlayerDraftProjection.NotAvailable
            return PlayerDraftProjection(rank = poolRank, tierLabel = tierForRank(poolRank))
        }
        val stock = player.role.draftStockPoints
        if (stock < MIN_STOCK_FOR_TOP_60_POOL) return PlayerDraftProjection.NotAvailable
        val rank = stockToRank(stock)
        return PlayerDraftProjection(rank = rank, tierLabel = tierForRank(rank))
    }

    fun isDraftEligible(player: DynastyPlayer): Boolean {
        if (player.role.inTransferPortal) return false
        return player.role.declaredForDraft ||
            player.classYear == PlayerClassYear.Senior ||
            (
                player.classYear == PlayerClassYear.Junior &&
                    player.role.draftStockPoints >= MIN_STOCK_FOR_TOP_60_POOL
                )
    }

    fun stockToRank(stockPoints: Int): Int {
        val clamped = stockPoints.coerceIn(MIN_STOCK_FOR_TOP_60_POOL, 100)
        val span = 100 - MIN_STOCK_FOR_TOP_60_POOL
        val rank = 60 - ((clamped - MIN_STOCK_FOR_TOP_60_POOL) * 59 / span)
        return rank.coerceIn(1, 60)
    }

    fun tierForRank(rank: Int): String = when (rank) {
        in 1..14 -> "Lottery"
        in 15..29 -> "First round"
        in 30..44 -> "Early second"
        in 45..60 -> "Late second"
        else -> ""
    }
}
