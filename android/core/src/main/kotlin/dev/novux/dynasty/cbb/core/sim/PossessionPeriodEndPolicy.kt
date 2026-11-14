package dev.novux.dynasty.cbb.core.sim

import dev.novux.dynasty.cbb.core.sim.tuning.SimPeriodEndRules

/**
 * Release-time scoring when the game or half clock hits 0:00 (MEGA-SIM-100).
 */
object PossessionPeriodEndPolicy {
    const val QA_ID: String = "MEGA-SIM-100"

    data class Context(
        val gamePeriodEnding: Boolean,
        val halfPeriodEnding: Boolean,
        val pointsScored: Int,
        val fieldGoalMade: Boolean,
        val freeThrowsAttempted: Boolean,
    )

    fun applyScoringRules(
        before: TeamBoxScore,
        after: TeamBoxScore,
        rules: SimPeriodEndRules,
        clockBeforeTrip: Int,
        clockAfterTrip: Int,
        inOvertime: Boolean,
    ): TeamBoxScore {
        val ctx = context(before, after, clockBeforeTrip, clockAfterTrip, inOvertime)
        if (!ctx.gamePeriodEnding && !ctx.halfPeriodEnding) return after
        if (ctx.pointsScored <= 0) return after
        val allowed = when {
            ctx.fieldGoalMade -> rules.countMadeBasketAtPeriodEnd
            ctx.freeThrowsAttempted && !ctx.fieldGoalMade -> rules.countFreeThrowsAtPeriodEnd
            else -> rules.countMadeBasketAtPeriodEnd
        }
        return if (allowed) after else stripTripScoring(before, after)
    }

    fun logTag(ctx: Context): String? {
        if (!ctx.gamePeriodEnding && !ctx.halfPeriodEnding) return null
        return when {
            ctx.pointsScored > 0 && ctx.halfPeriodEnding ->
                "half buzzer: +${ctx.pointsScored} counts"
            ctx.pointsScored > 0 && ctx.gamePeriodEnding ->
                "0:00 release: +${ctx.pointsScored} counts"
            ctx.gamePeriodEnding -> "0:00"
            else -> null
        }
    }

    fun context(
        before: TeamBoxScore,
        after: TeamBoxScore,
        clockBeforeTrip: Int,
        clockAfterTrip: Int,
        inOvertime: Boolean,
    ): Context {
        val gamePeriodEnding = clockBeforeTrip > 0 && clockAfterTrip <= 0
        val halfPeriodEnding = !inOvertime &&
            clockBeforeTrip > GameSimClock.HALF_SECONDS &&
            clockAfterTrip <= GameSimClock.HALF_SECONDS
        return Context(
            gamePeriodEnding = gamePeriodEnding,
            halfPeriodEnding = halfPeriodEnding,
            pointsScored = after.points - before.points,
            fieldGoalMade = after.fieldGoalsMade > before.fieldGoalsMade,
            freeThrowsAttempted = after.freeThrowsAttempted > before.freeThrowsAttempted,
        )
    }

    private fun stripTripScoring(before: TeamBoxScore, after: TeamBoxScore): TeamBoxScore =
        after.copy(
            points = before.points,
            fieldGoalsMade = before.fieldGoalsMade,
            fieldGoalsAttempted = before.fieldGoalsAttempted,
            threePointsMade = before.threePointsMade,
            threePointsAttempted = before.threePointsAttempted,
            freeThrowsMade = before.freeThrowsMade,
            freeThrowsAttempted = before.freeThrowsAttempted,
        )
}
