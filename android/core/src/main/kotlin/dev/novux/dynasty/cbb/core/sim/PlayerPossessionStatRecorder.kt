package dev.novux.dynasty.cbb.core.sim

import kotlin.random.Random

/** Attributes team possession deltas to on-floor players. */
object PlayerPossessionStatRecorder {
    fun record(
        offenseLedger: PlayerBoxScoreLedger,
        defenseLedger: PlayerBoxScoreLedger,
        offenseFloor: List<String>,
        defenseFloor: List<String>,
        usageWeights: Map<String, Int>,
        before: TeamBoxScore,
        after: TeamBoxScore,
        change: PossessionChange,
        turnoverTerminal: TurnoverTerminal?,
        foulStoppage: Boolean,
        blockedShot: Boolean = false,
        random: Random,
    ): PossessionPlayerCredits {
        if (offenseFloor.isEmpty()) return PossessionPlayerCredits()
        var scorerId: String? = null
        var assistId: String? = null
        var rebounderId: String? = null
        var freeThrowShooterId: String? = null
        val pts = after.points - before.points
        val fga = after.fieldGoalsAttempted - before.fieldGoalsAttempted
        val fgm = after.fieldGoalsMade - before.fieldGoalsMade
        val threeAtt = after.threePointsAttempted - before.threePointsAttempted
        val threeMade = after.threePointsMade - before.threePointsMade
        val turnovers = after.turnovers - before.turnovers
        val ftAttempts = after.freeThrowsAttempted - before.freeThrowsAttempted
        val ftMade = after.freeThrowsMade - before.freeThrowsMade

        var turnoverHandlerId: String? = null
        if (turnovers > 0) {
            val handler = pickWeighted(offenseFloor, usageWeights, random)
            turnoverHandlerId = handler
            offenseLedger.addTurnover(handler)
            if (defenseFloor.isNotEmpty() &&
                turnoverTerminal != TurnoverTerminal.ShotClockViolation &&
                (
                    turnoverTerminal == TurnoverTerminal.Steal ||
                        random.nextDouble() < 0.38
                    )
            ) {
                defenseLedger.addSteal(pickUniform(defenseFloor, random))
            }
        }

        if (fga > 0) {
            val shooter = pickWeighted(offenseFloor, usageWeights, random)
            scorerId = shooter
            val three = threeAtt > 0
            offenseLedger.addFieldGoalAttempt(shooter, three)
            if (fgm > 0) {
                offenseLedger.addFieldGoalMake(shooter, pts, three)
                val others = offenseFloor.filter { it != shooter }
                if (others.isNotEmpty() && random.nextDouble() < 0.58) {
                    val assister = pickWeighted(others, usageWeights, random)
                    assistId = assister
                    offenseLedger.addAssist(assister)
                }
            } else when {
                blockedShot && defenseFloor.isNotEmpty() -> {
                    defenseLedger.addBlock(pickUniform(defenseFloor, random))
                }
                change == PossessionChange.OffenseRetainsOffensiveRebound -> {
                    val reb = pickWeighted(offenseFloor, usageWeights, random)
                    rebounderId = reb
                    offenseLedger.addOffensiveRebound(reb)
                }
                change == PossessionChange.DefenseTakesBall && defenseFloor.isNotEmpty() -> {
                    val reb = pickUniform(defenseFloor, random)
                    rebounderId = reb
                    defenseLedger.addDefensiveRebound(reb)
                }
            }
        }

        if (ftAttempts > 0) {
            val shooter = pickWeighted(offenseFloor, usageWeights, random)
            freeThrowShooterId = shooter
            offenseLedger.addFreeThrows(shooter, ftMade, ftAttempts)
        }
        return PossessionPlayerCredits(
            scorerId = scorerId,
            assistId = assistId,
            rebounderId = rebounderId,
            freeThrowShooterId = freeThrowShooterId,
            turnoverHandlerId = turnoverHandlerId,
        )
    }

    private const val FREE_THROW_MAKE_RATE = 0.74

    fun sampleFreeThrowChain(attempts: Int, random: Random): Pair<Int, Int> {
        if (attempts <= 0) return 0 to 0
        var makes = 0
        repeat(attempts) {
            if (random.nextDouble() < FREE_THROW_MAKE_RATE) makes++
        }
        return makes to attempts
    }

    fun sampleBonusFreeThrows(random: Random): Pair<Int, Int> =
        sampleFreeThrowChain(attempts = 2, random = random)

    fun shouldShootBonusFreeThrows(
        before: TeamBoxScore,
        after: TeamBoxScore,
        foulStoppage: Boolean,
    ): Boolean =
        foulStoppage &&
            after.fieldGoalsAttempted == before.fieldGoalsAttempted &&
            after.turnovers == before.turnovers &&
            after.points == before.points

    private fun pickWeighted(ids: List<String>, weights: Map<String, Int>, random: Random): String {
        if (ids.size == 1) return ids.first()
        val total = ids.sumOf { weights[it] ?: 15 }
        if (total <= 0) return ids.random(random)
        var roll = random.nextInt(total)
        for (id in ids) {
            roll -= weights[id] ?: 15
            if (roll < 0) return id
        }
        return ids.last()
    }

    private fun pickUniform(ids: List<String>, random: Random): String = ids.random(random)
}
