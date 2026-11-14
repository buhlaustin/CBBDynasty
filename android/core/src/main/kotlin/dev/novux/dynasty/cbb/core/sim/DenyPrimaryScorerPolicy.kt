package dev.novux.dynasty.cbb.core.sim

import dev.novux.dynasty.cbb.core.lineup.LineupSlotPolicy
import dev.novux.dynasty.cbb.core.qa.DenyPrimaryScorerPath
import dev.novux.dynasty.cbb.core.roster.PlayerPosition
import dev.novux.dynasty.cbb.core.sim.tuning.SimDenyPrimaryWeights
import dev.novux.dynasty.cbb.core.strategy.GameplanInput
import kotlin.random.Random

/**
 * Deny-primary gameplan: assigned slot defender adds pressure and foul risk (MEGA-SIM-117).
 */
object DenyPrimaryScorerPolicy {
    val QA_ID: String = DenyPrimaryScorerPath.SIM_ID

    data class Context(
        val active: Boolean,
        val primaryOffenseId: String? = null,
        val assignedDefenderId: String? = null,
        val opponentSlot: PlayerPosition = PlayerPosition.PointGuard,
    ) {
        companion object {
            val Inactive = Context(active = false)
        }
    }

    fun context(
        defGameplan: GameplanInput?,
        offenseRoster: GameSimRosterInput?,
        offenseFloor: List<String>,
        defenseFloor: List<String>,
        defenseRoster: GameSimRosterInput?,
    ): Context {
        if (defGameplan?.denyPrimaryScorer != true) return Context.Inactive
        val slot = defGameplan.primaryScorerSlot
        val primary = primaryScorerOnFloor(offenseRoster, offenseFloor, slot)
        val assigned = defGameplan.defensiveAssignments[slot]
            ?.takeIf { it in defenseFloor }
            ?: bestDefenderForSlot(defenseRoster, defenseFloor, slot)
        if (assigned == null) {
            return Context(active = true, primaryOffenseId = primary, opponentSlot = slot)
        }
        return Context(
            active = true,
            primaryOffenseId = primary,
            assignedDefenderId = assigned,
            opponentSlot = slot,
        )
    }

    fun primaryScorerOnFloor(
        offenseRoster: GameSimRosterInput?,
        offenseFloor: List<String>,
        slot: PlayerPosition,
    ): String? {
        if (offenseRoster == null || offenseFloor.isEmpty()) return null
        val onFloor = offenseRoster.participants.filter { it.playerId in offenseFloor }
        return onFloor.firstOrNull { it.slotPlayed == slot }?.playerId
            ?: onFloor.firstOrNull { it.listedPosition == slot }?.playerId
            ?: onFloor.firstOrNull { LineupSlotPolicy.slotEligible(it.listedPosition, slot) }?.playerId
    }

    fun bestDefenderForSlot(
        defenseRoster: GameSimRosterInput?,
        defenseFloor: List<String>,
        slot: PlayerPosition,
    ): String? {
        if (defenseRoster == null) return null
        return defenseRoster.participants
            .asSequence()
            .filter { it.playerId in defenseFloor }
            .filter { LineupSlotPolicy.slotEligible(it.listedPosition, slot) }
            .maxByOrNull { it.defensiveRating }
            ?.playerId
    }

    fun extraTurnoverChance(ctx: Context, weights: SimDenyPrimaryWeights): Double =
        if (ctx.active && ctx.primaryOffenseId != null && ctx.assignedDefenderId != null) {
            weights.assignedSlotTurnoverBoost
        } else {
            0.0
        }

    fun rollDenyFoulOnPrimaryTouch(
        ctx: Context,
        credits: PossessionPlayerCredits,
        turnoversAdded: Int,
        random: Random,
        weights: SimDenyPrimaryWeights,
    ): Boolean {
        if (!ctx.active || ctx.assignedDefenderId == null || ctx.primaryOffenseId == null) return false
        val primaryOnTurnover = turnoversAdded > 0 && credits.turnoverHandlerId == ctx.primaryOffenseId
        val primaryInvolved =
            credits.scorerId == ctx.primaryOffenseId ||
                credits.freeThrowShooterId == ctx.primaryOffenseId ||
                primaryOnTurnover
        if (!primaryInvolved) return false
        val rate = if (primaryOnTurnover) {
            weights.denyFoulOnTurnoverRate
        } else {
            weights.denyFoulOnPrimaryTouchRate
        }
        return random.nextDouble() < rate
    }

    fun logTag(ctx: Context): String? {
        if (!ctx.active || ctx.assignedDefenderId == null) return null
        return "deny primary ${ctx.opponentSlot.wire}→${ctx.assignedDefenderId}"
    }

    fun foulLogLabel(ctx: Context, personalFoulLine: String?): String =
        personalFoulLine?.let {
            "deny primary | ${ctx.opponentSlot.wire} | $it"
        } ?: "deny primary | ${ctx.opponentSlot.wire}"
}
