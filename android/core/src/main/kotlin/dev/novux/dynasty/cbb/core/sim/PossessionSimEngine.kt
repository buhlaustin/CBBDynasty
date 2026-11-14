package dev.novux.dynasty.cbb.core.sim

import dev.novux.dynasty.cbb.core.strategy.LateGamePossessionPolicy
import dev.novux.dynasty.cbb.core.strategy.ResolvedSchemeEffects
import dev.novux.dynasty.cbb.core.sim.tuning.SimEventWeights
import dev.novux.dynasty.cbb.core.sim.tuning.SimEventWeightsResolver
import dev.novux.dynasty.cbb.core.sim.tuning.SimTripBucketWeights
import dev.novux.dynasty.cbb.core.sim.tuning.SimTuning
import kotlin.math.max
import kotlin.math.min
import kotlin.random.Random

/**
 * Possession-based regulation sim — [planning/simulation-engine.md#possession-loop--clock].
 */
object PossessionSimEngine {
    fun simulate(input: GameSimInput): GameSimResult =
        BoxScoreCoherenceSim.simulateWithRetry(input, SimulateOptions(), injuryRoll = null)

    fun simulate(
        input: GameSimInput,
        options: SimulateOptions = SimulateOptions(),
        injuryRoll: GameSimInjuryRollInput? = null,
    ): GameSimResult = BoxScoreCoherenceSim.simulateWithRetry(input, options, injuryRoll)

    internal fun simulateUnchecked(
        input: GameSimInput,
        options: SimulateOptions = SimulateOptions(),
        injuryRoll: GameSimInjuryRollInput? = null,
    ): GameSimResult {
        val session = PossessionSimSession(
            input,
            options.resume,
            options.stopAtHalftime,
            injuryRoll = injuryRoll,
        )
        while (session.hasNext()) {
            val step = session.step()
            if (step.haltedAtHalftime) {
                return session.toResult(isComplete = false)
            }
        }
        GameSimOvertime.playUntilWinner(session)
        var result = session.toResult(isComplete = true)
        if (injuryRoll != null) {
            result = GameSimInjuryRoll.attach(input, result, injuryRoll)
        }
        return result
    }

    internal data class Sides(
        val offense: GameSimTeamInput,
        val defense: GameSimTeamInput,
        val box: TeamBoxScore,
        val label: String,
    )

    internal data class TripSample(
        val rawSeconds: Int,
        val foulStoppage: Boolean,
        /** Defense commits intentional foul this trip (foul-early strategy). */
        val intentionalFoul: Boolean = false,
        /** Short hack-a foul trip without normal FGA terminal. */
        val foulEarlyHacka: Boolean = false,
    )

    internal fun sampleTripForLateGame(
        tuning: SimTuning,
        random: Random,
        decision: LateGamePossessionPolicy.PossessionDecision,
        eventWeights: SimEventWeights = SimEventWeightsResolver.DEFAULT,
    ): TripSample =
        when (decision) {
            LateGamePossessionPolicy.PossessionDecision.IntentionalFoulTrip ->
                when (
                    FoulEarlyLateGamePolicy.rollTripKind(random, eventWeights.foulEarly)
                ) {
                    FoulEarlyLateGamePolicy.TripKind.IntentionalFoul ->
                        foulStoppageTripSample(tuning, random).copy(intentionalFoul = true)
                    FoulEarlyLateGamePolicy.TripKind.HackaQuickFoul ->
                        hackaQuickFoulTripSample(
                            tuning,
                            random,
                            eventWeights.foulEarly.hackaTripMaxSeconds,
                        ).copy(foulEarlyHacka = true)
                    FoulEarlyLateGamePolicy.TripKind.Normal ->
                        sampleTrip(tuning, random, eventWeights)
                }
            LateGamePossessionPolicy.PossessionDecision.ProtectLeadPace -> {
                val base = sampleTrip(tuning, random, eventWeights)
                if (base.foulStoppage &&
                    ProtectLeadLateGamePolicy.shouldAvoidFoulStoppage(random, eventWeights.protectLead)
                ) {
                    quickShotTripSample(tuning, random)
                } else {
                    base
                }
            }
            LateGamePossessionPolicy.PossessionDecision.None -> sampleTrip(tuning, random, eventWeights)
        }

    private fun foulStoppageTripSample(tuning: SimTuning, random: Random): TripSample {
        val range = tuning.timeUsedSeconds.foulStoppage
        val lo = range.firstOrNull() ?: GameSimClock.MIN_TRIP_SECONDS
        val hi = range.lastOrNull() ?: GameSimClock.MAX_TRIP_SECONDS
        val raw = if (lo >= hi) lo else random.nextInt(lo, hi + 1)
        return TripSample(
            rawSeconds = raw.coerceIn(GameSimClock.MIN_TRIP_SECONDS, GameSimClock.MAX_TRIP_SECONDS),
            foulStoppage = true,
        )
    }

    private fun hackaQuickFoulTripSample(tuning: SimTuning, random: Random, maxSeconds: Int): TripSample {
        val cap = maxSeconds.coerceIn(GameSimClock.MIN_TRIP_SECONDS, GameSimClock.MAX_TRIP_SECONDS)
        val range = tuning.timeUsedSeconds.quickTurnover
        val lo = range.firstOrNull() ?: GameSimClock.MIN_TRIP_SECONDS
        val hi = minOf(range.lastOrNull() ?: cap, cap)
        val raw = if (lo >= hi) lo else random.nextInt(lo, hi + 1)
        return TripSample(
            rawSeconds = raw.coerceIn(GameSimClock.MIN_TRIP_SECONDS, cap),
            foulStoppage = true,
        )
    }

    private fun quickShotTripSample(tuning: SimTuning, random: Random): TripSample {
        val range = tuning.timeUsedSeconds.quickShot
        val lo = range.firstOrNull() ?: GameSimClock.MIN_TRIP_SECONDS
        val hi = range.lastOrNull() ?: GameSimClock.MAX_TRIP_SECONDS
        val raw = if (lo >= hi) lo else random.nextInt(lo, hi + 1)
        return TripSample(
            rawSeconds = raw.coerceIn(GameSimClock.MIN_TRIP_SECONDS, GameSimClock.MAX_TRIP_SECONDS),
            foulStoppage = false,
        )
    }

    internal fun sampleTrip(
        tuning: SimTuning,
        random: Random,
        eventWeights: SimEventWeights = SimEventWeightsResolver.DEFAULT,
    ): TripSample {
        val bucket = pickTripBucket(eventWeights.tripBuckets, random)
        val foulStoppage = bucket == 2
        val range = when (bucket) {
            0 -> tuning.timeUsedSeconds.quickTurnover
            1 -> tuning.timeUsedSeconds.quickShot
            else -> tuning.timeUsedSeconds.foulStoppage
        }
        val lo = range.firstOrNull() ?: GameSimClock.MIN_TRIP_SECONDS
        val hi = range.lastOrNull() ?: GameSimClock.MAX_TRIP_SECONDS
        val raw = if (lo >= hi) lo else random.nextInt(lo, hi + 1)
        return TripSample(
            rawSeconds = raw.coerceIn(GameSimClock.MIN_TRIP_SECONDS, GameSimClock.MAX_TRIP_SECONDS),
            foulStoppage = foulStoppage,
        )
    }

    internal fun pacedTripSecondsFromRaw(
        tuning: SimTuning,
        rawSeconds: Int,
        tripPaceScale: Double = 1.0,
    ): Int {
        val targetPerTeam = (tuning.possessionsPerGameTarget.min + tuning.possessionsPerGameTarget.max) / 2
        val targetTrips = targetPerTeam * 2
        val baselineAvg = 12.0
        val scale = (GameSimClock.REGULATION_SECONDS.toDouble() / targetTrips) / baselineAvg
        val adjusted = (rawSeconds * scale * tripPaceScale).toInt()
        return adjusted.coerceIn(GameSimClock.MIN_TRIP_SECONDS, GameSimClock.MAX_TRIP_SECONDS)
    }

    internal fun pacedTripSeconds(tuning: SimTuning, random: Random, tripPaceScale: Double = 1.0): Int =
        pacedTripSecondsFromRaw(tuning, sampleTrip(tuning, random).rawSeconds, tripPaceScale)

    internal fun sampleTripSeconds(
        tuning: SimTuning,
        random: Random,
        eventWeights: SimEventWeights = SimEventWeightsResolver.DEFAULT,
    ): Int = sampleTrip(tuning, random, eventWeights).rawSeconds

    private fun pickTripBucket(weights: SimTripBucketWeights, random: Random): Int {
        val ordered = listOf(weights.quickTurnover, weights.quickShot, weights.foulStoppage)
        val total = ordered.sum()
        if (total <= 0.0) return random.nextInt(3)
        var roll = random.nextDouble() * total
        ordered.forEachIndexed { index, weight ->
            roll -= weight
            if (roll < 0) return index
        }
        return 2
    }

    internal fun resolveShotClockViolation(offense: TeamBoxScore): PossessionResolveResult {
        val box = offense.copy(
            possessions = offense.possessions + 1,
            turnovers = offense.turnovers + 1,
        )
        return PossessionResolveResult(
            box,
            PossessionChange.DefenseTakesBall,
            turnoverTerminal = PossessionShotClockViolationPolicy.turnoverTerminal,
        )
    }

    internal fun resolvePossessionForSession(
        offense: TeamBoxScore,
        offRating: Int,
        defRating: Int,
        effects: ResolvedSchemeEffects,
        random: Random,
        lateClockTrip: Boolean = false,
        eventWeights: SimEventWeights = SimEventWeightsResolver.DEFAULT,
        protectLeadDefense: Boolean = false,
    ): PossessionResolveResult =
        PossessionSubPhaseEngine.resolve(
            offense,
            offRating,
            defRating,
            effects,
            random,
            lateClockTrip,
            eventWeights,
            protectLeadDefense,
        )

    /** Miss terminal → OReb or DReb change ([MEGA-SIM-090]). */
    internal fun resolveMissRebound(
        afterMiss: TeamBoxScore,
        random: Random,
        offensiveReboundRate: Double = SimEventWeightsResolver.DEFAULT.offensiveReboundRate,
    ): PossessionResolveResult = offensiveReboundOrDefReb(afterMiss, random, offensiveReboundRate)

    private fun offensiveReboundOrDefReb(
        afterMiss: TeamBoxScore,
        random: Random,
        offensiveReboundRate: Double,
    ): PossessionResolveResult {
        val oreb = random.nextDouble() < offensiveReboundRate.coerceIn(0.0, 1.0)
        return if (oreb) {
            PossessionResolveResult(afterMiss, PossessionChange.OffenseRetainsOffensiveRebound)
        } else {
            PossessionResolveResult(afterMiss, PossessionChange.DefenseTakesBall)
        }
    }

    internal fun formatPossessionLog(
        used: Int,
        clockRemaining: Int,
        offense: String,
        before: TeamBoxScore,
        after: TeamBoxScore,
        shotClockBefore: Int,
        shotClockAfter: Int,
        change: PossessionChange,
        turnoverTerminal: TurnoverTerminal? = null,
        foulNote: String? = null,
        lateGameTag: String? = null,
        subPhaseChain: String? = null,
        blockedShot: Boolean = false,
        foulOnBlock: Boolean = false,
        shootingFoul: Boolean = false,
        playerAttribution: String? = null,
        intentionalFoul: Boolean = false,
    ): String {
        val clock = formatClock(clockRemaining)
        val pointsScored = after.points - before.points
        val turnedOver = after.turnovers > before.turnovers
        val madeThree = after.threePointsMade > before.threePointsMade
        val shotAttempted = after.fieldGoalsAttempted > before.fieldGoalsAttempted
        val scoreLine = when {
            intentionalFoul -> "intentional foul stop"
            pointsScored > 0 && !shotAttempted -> "FT +$pointsScored"
            pointsScored > 0 && madeThree -> "+3 FG"
            pointsScored > 0 -> "+2 FG"
            turnedOver -> PossessionTurnoverFlavorPolicy.logLabel(turnoverTerminal)
            change == PossessionChange.OffenseRetainsOffensiveRebound ->
                "miss | OREB +1 | retain possession"
            blockedShot && foulOnBlock ->
                "miss | BLK | foul on block | change possession"
            blockedShot ->
                "miss | BLK | change possession"
            shootingFoul && after.fieldGoalsAttempted > before.fieldGoalsAttempted ->
                "miss | shooting foul"
            after.fieldGoalsAttempted > before.fieldGoalsAttempted &&
                change == PossessionChange.DefenseTakesBall ->
                "miss | DREB | change possession"
            after.fieldGoalsAttempted > before.fieldGoalsAttempted -> "miss"
            else -> "possession"
        }
        val foulSuffix = foulNote?.let { " | $it" }.orEmpty()
        val lateSuffix = lateGameTag?.let { " | $it" }.orEmpty()
        val phaseSuffix = subPhaseChain?.let { " | $it" }.orEmpty()
        val playerSuffix = playerAttribution?.let { " | $it" }.orEmpty()
        return "$clock | $offense | ${used}s trip | SC $shotClockBefore→$shotClockAfter | $scoreLine$playerSuffix$foulSuffix$lateSuffix$phaseSuffix"
    }

    private fun formatClock(seconds: Int): String {
        val clamped = max(0, seconds)
        val minutes = clamped / 60
        val secs = clamped % 60
        return "%d:%02d".format(minutes, secs)
    }
}
