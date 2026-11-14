package dev.novux.dynasty.cbb.core.signingday

import dev.novux.dynasty.cbb.core.roster.PlayerPosition

/** Signing Day position crunch summary for the board ([MEGA-GDD-028]). */
object SigningDayClassBoardSummaryPolicy {
    const val MEGA_ID: String = "MEGA-GDD-028"

    fun positionCounts(signees: List<IncomingSignee>): Map<PlayerPosition, Int> =
        signees.groupingBy { it.position }.eachCount()

    fun orderedCounts(signees: List<IncomingSignee>): List<Pair<PlayerPosition, Int>> {
        val counts = positionCounts(signees)
        return PlayerPosition.entries.mapNotNull { pos ->
            val n = counts[pos] ?: 0
            if (n == 0) null else pos to n
        }
    }
}