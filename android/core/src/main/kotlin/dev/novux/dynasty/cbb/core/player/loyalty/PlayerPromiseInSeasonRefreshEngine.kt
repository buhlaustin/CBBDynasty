package dev.novux.dynasty.cbb.core.player.loyalty

import dev.novux.dynasty.cbb.core.balance.BalanceLoyaltyTuning
import dev.novux.dynasty.cbb.core.player.DynastyPlayer

/**
 * In-season promise checks — surfaces [PromiseStatus.AtRisk] before a hard [PromiseStatus.Broken]
 * ([MEGA-COACH-170], [planning/player-loyalty-promises.md#ui--data]).
 */
object PlayerPromiseInSeasonRefreshEngine {
    const val MEGA_ID: String = "MEGA-COACH-170"

    fun refresh(
        player: DynastyPlayer,
        seasonMinutesPerGame: Double?,
        tuning: BalanceLoyaltyTuning = BalanceLoyaltyTuning(),
    ): DynastyPlayer {
        if (player.loyalty.promise == null) return player
        var status = PromiseEvaluator.evaluateSeasonWindow(player, seasonMinutesPerGame, tuning)
        if (status == PromiseStatus.Broken) {
            val current = player.loyalty.promise!!.status
            if (current == PromiseStatus.Pending || current == PromiseStatus.AtRisk) {
                status = PromiseStatus.AtRisk
            }
        }
        val profile = PromiseEvaluator.applyStatusDelta(player.loyalty, status, tuning)
        return if (profile == player.loyalty) player else player.copy(loyalty = profile)
    }
}
