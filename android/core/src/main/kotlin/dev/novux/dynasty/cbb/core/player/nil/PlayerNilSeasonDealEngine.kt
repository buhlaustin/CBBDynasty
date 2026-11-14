package dev.novux.dynasty.cbb.core.player.nil

import dev.novux.dynasty.cbb.core.player.PlayerIdentity
import dev.novux.dynasty.cbb.core.roster.RosterPlayer

/** Retention NIL bumps — `↑` chip the season the deal changes ([MEGA-COACH-148], [planning/player-cards.md]). */
object PlayerNilSeasonDealEngine {
    const val MEGA_ID: String = "MEGA-COACH-148"

    fun showRaisedChip(identity: PlayerIdentity): Boolean =
        identity.nilDealRaisedThisSeason && identity.nilUsdPerYear > 0

    fun applyRetentionBump(player: RosterPlayer, newUsdPerYear: Long): RosterPlayer {
        val previous = player.nilUsdPerYear
        val nextUsd = newUsdPerYear.coerceAtLeast(0)
        val raisedThisSeason = nextUsd > previous && previous > 0
        return player.copy(
            nilUsdPerYear = nextUsd,
            nilDealRaisedThisSeason = raisedThisSeason,
        )
    }

    fun clearSeasonDealFlags(players: List<RosterPlayer>): List<RosterPlayer> =
        players.map { player ->
            if (player.nilDealRaisedThisSeason) {
                player.copy(nilDealRaisedThisSeason = false)
            } else {
                player
            }
        }
}
