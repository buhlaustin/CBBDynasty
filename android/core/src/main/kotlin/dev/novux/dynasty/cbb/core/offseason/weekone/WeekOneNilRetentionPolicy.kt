package dev.novux.dynasty.cbb.core.offseason.weekone

import dev.novux.dynasty.cbb.core.roster.RosterPlayer

/** Week 1 retention NIL bumps share the annual ledger ([MEGA-GDD-031]). */
object WeekOneNilRetentionPolicy {
    const val MEGA_ID: String = "MEGA-GDD-031"

    fun ledgerTargetId(playerId: String): String = "retention:$playerId"

    /** NIL ask basis for 3× cap — current deal or program floor when unsigned. */
    fun retentionAskUsd(player: RosterPlayer): Int {
        val current = player.nilUsdPerYear.coerceAtLeast(0L).toInt()
        if (current > 0) return current
        return (player.ovr.coerceAtLeast(60) * 1_500).coerceIn(25_000, 250_000)
    }

    fun suggestedOfferUsd(player: RosterPlayer): Long {
        val current = player.nilUsdPerYear.coerceAtLeast(0L)
        if (current <= 0L) return retentionAskUsd(player).toLong()
        return (current * 115L / 100L).coerceAtLeast(current)
    }
}