package dev.novux.dynasty.cbb.core.sim

import dev.novux.dynasty.cbb.core.sim.tuning.SimEventWeights
import dev.novux.dynasty.cbb.core.sim.tuning.SimEventWeightsResolver
import dev.novux.dynasty.cbb.core.strategy.ResolvedSchemeEffects
import kotlin.random.Random

/**
 * Optional possession sub-phases **drive → kick → shot** before terminal outcome
 * ([planning/simulation-engine.md#why-track-a-shot-clock], MEGA-SIM-089).
 */
object PossessionSubPhaseEngine {
    const val QA_ID: String = "MEGA-SIM-089"

    fun resolve(
        offense: TeamBoxScore,
        offRating: Int,
        defRating: Int,
        effects: ResolvedSchemeEffects,
        random: Random,
        lateClockTrip: Boolean = false,
        eventWeights: SimEventWeights = SimEventWeightsResolver.DEFAULT,
        protectLeadDefense: Boolean = false,
    ): PossessionResolveResult {
        val box = offense.copy(possessions = offense.possessions + 1)
        val chain = mutableListOf("drive")
        val sub = eventWeights.turnoverSubPhase
        val baseTo = effects.turnoverChance.coerceIn(0.05, 0.40)
        if (random.nextDouble() < baseTo * sub.driveShare) {
            val flavor = PossessionTurnoverFlavorPolicy.sampleForDrivePhase(random)
            return turnoverResult(box, chain, flavor)
        }
        chain += "kick"
        if (random.nextDouble() < baseTo * sub.kickShare) {
            val flavor = PossessionTurnoverFlavorPolicy.sampleForKickPhase(random)
            return turnoverResult(box, chain, flavor)
        }
        chain += "shot"
        return resolveShotTerminal(
            box,
            offRating,
            defRating,
            effects,
            random,
            chain,
            lateClockTrip,
            eventWeights,
            protectLeadDefense,
        )
    }

    private fun turnoverResult(
        box: TeamBoxScore,
        chain: List<String>,
        terminal: TurnoverTerminal,
    ): PossessionResolveResult =
        PossessionResolveResult(
            box = box.copy(turnovers = box.turnovers + 1),
            change = PossessionChange.DefenseTakesBall,
            turnoverTerminal = terminal,
            subPhaseChain = chain.joinToString("→") +
                "→${PossessionTurnoverFlavorPolicy.chainSuffix(terminal)}",
        )

    private fun resolveShotTerminal(
        box: TeamBoxScore,
        offRating: Int,
        defRating: Int,
        effects: ResolvedSchemeEffects,
        random: Random,
        chain: List<String>,
        lateClockTrip: Boolean,
        eventWeights: SimEventWeights,
        protectLeadDefense: Boolean,
    ): PossessionResolveResult {
        val edge = (offRating - defRating).coerceIn(-30, 30)
        val makeBias = (
            eventWeights.makeRateForEdge(edge) + effects.makeRateAdjust + eventWeights.ratingGapMakeRate.kickLookBonus
            ).coerceIn(eventWeights.ratingGapMakeRate.min, eventWeights.ratingGapMakeRate.max)
        val three = random.nextDouble() < effects.threePointAttemptRate
        val attempt = box.fieldGoalsAttempted + 1
        val made = random.nextDouble() < makeBias
        var next = box.copy(fieldGoalsAttempted = attempt)
        if (three) {
            next = next.copy(threePointsAttempted = next.threePointsAttempted + 1)
        }
        val chainLabel = chain.joinToString("→")
        if (made) {
            val pts = if (three) 3 else 2
            next = next.copy(
                fieldGoalsMade = next.fieldGoalsMade + 1,
                points = next.points + pts,
                threePointsMade = next.threePointsMade + if (three) 1 else 0,
            )
            val andOneMultiplier = if (protectLeadDefense) {
                eventWeights.protectLead.andOneFoulMultiplier *
                    ProtectLeadLateGamePolicy.shootingFoulRateMultiplier(three, eventWeights.protectLead)
            } else {
                1.0
            }
            val andOne = PossessionShootingFoulPolicy.rollAndOne(
                lateClockTrip,
                random,
                eventWeights,
                andOneMultiplier,
            )
            val ftAttempts = if (andOne) 1 else 0
            val suffix = if (andOne) {
                PossessionShootingFoulPolicy.chainSuffix(andOne = true, attempts = ftAttempts)
            } else {
                ""
            }
            return PossessionResolveResult(
                next,
                PossessionChange.DefenseTakesBall,
                subPhaseChain = listOfNotNull("$chainLabel | make", suffix.takeIf { it.isNotEmpty() })
                    .joinToString(" | "),
                shootingFoul = andOne,
                andOne = andOne,
                freeThrowAttempts = ftAttempts,
            )
        }
        val block = PossessionBlockPolicy.rollOnMiss(defRating, offRating, random, eventWeights)
        if (block.blocked) {
            return PossessionResolveResult(
                box = next,
                change = PossessionChange.DefenseTakesBall,
                subPhaseChain = "$chainLabel | miss | BLK",
                blockedShot = true,
                foulOnBlock = block.foulOnBlock,
            )
        }
        val missFoulMultiplier = if (protectLeadDefense) {
            ProtectLeadLateGamePolicy.shootingFoulRateMultiplier(three, eventWeights.protectLead)
        } else {
            1.0
        }
        if (PossessionShootingFoulPolicy.rollMissShootingFoul(lateClockTrip, random, eventWeights, missFoulMultiplier)) {
            val ftAttempts = PossessionShootingFoulPolicy.attemptsForMissFoul(three)
            return PossessionResolveResult(
                box = next,
                change = PossessionChange.DefenseTakesBall,
                subPhaseChain = "$chainLabel | miss | ${PossessionShootingFoulPolicy.chainSuffix(andOne = false, attempts = ftAttempts)}",
                shootingFoul = true,
                freeThrowAttempts = ftAttempts,
            )
        }
        val reb = PossessionSimEngine.resolveMissRebound(next, random, eventWeights.offensiveReboundRate)
        return reb.copy(subPhaseChain = "$chainLabel | miss")
    }
}
