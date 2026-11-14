package dev.novux.dynasty.cbb.core.sim

import dev.novux.dynasty.cbb.core.balance.BalanceFatigue

/**
 * Auto-pull players in foul trouble so weighted team ratings reflect floor changes
 * ([planning/simulation-engine.md#rotation--fouls-in-sim], MEGA-SIM-102).
 */
object FoulTroubleRotationPolicy {
    const val BENCH_FOULS_FIRST_HALF: Int = 3
    const val BENCH_FOULS_SECOND_HALF: Int = 4
    const val FLOOR_PLAYERS: Int = 5

    fun isFoulTroubleBenched(foulCount: Int, gameClockSeconds: Int): Boolean {
        if (foulCount <= 0) return false
        val secondHalf = gameClockSeconds <= GameSimClock.HALF_SECONDS
        val threshold = if (secondHalf) BENCH_FOULS_SECOND_HALF else BENCH_FOULS_FIRST_HALF
        return foulCount >= threshold
    }

    fun floorParticipants(
        roster: GameSimRosterInput,
        ledger: PersonalFoulLedger,
        gameClockSeconds: Int,
    ): List<GameSimParticipant> {
        val startersOnFloor = roster.participants
            .filter { p ->
                !ledger.isDisqualified(p.playerId) &&
                    !isFoulTroubleBenched(ledger.foulCount(p.playerId), gameClockSeconds)
            }
            .sortedByDescending { it.targetMinutes }
        if (startersOnFloor.size >= FLOOR_PLAYERS) {
            return startersOnFloor.take(FLOOR_PLAYERS)
        }
        val pulledIds = roster.participants
            .filter { p ->
                ledger.isDisqualified(p.playerId) ||
                    isFoulTroubleBenched(ledger.foulCount(p.playerId), gameClockSeconds)
            }
            .map { it.playerId }
            .toSet()
        val replacements = roster.participants
            .filter { it.playerId !in pulledIds && it !in startersOnFloor }
            .sortedByDescending { it.targetMinutes }
        val merged = (startersOnFloor + replacements).distinctBy { it.playerId }
        if (merged.size >= FLOOR_PLAYERS) return merged.take(FLOOR_PLAYERS)
        val anyEligible = roster.participants.filterNot { ledger.isDisqualified(it.playerId) }
        return (merged + anyEligible).distinctBy { it.playerId }.take(FLOOR_PLAYERS)
    }
}

fun GameSimRosterInput.weightedOffensiveOnFloor(
    foulLedger: PersonalFoulLedger,
    minutesLedger: SimFloorMinutesLedger,
    gameClockSeconds: Int,
    staminaLedger: SimStaminaLedger? = null,
    fatigueTuning: BalanceFatigue = BalanceFatigue.DEFAULT,
    comfort: dev.novux.dynasty.cbb.core.sim.tuning.SimPositionComfortModifiers =
        dev.novux.dynasty.cbb.core.sim.tuning.SimPositionComfortModifiers(),
): Int = weightedOnFloor(
    foulLedger,
    minutesLedger,
    gameClockSeconds,
    staminaLedger,
    fatigueTuning,
    offensiveSide = true,
    comfort = comfort,
    fallback = ::weightedOffensive,
)

fun GameSimRosterInput.weightedDefensiveOnFloor(
    foulLedger: PersonalFoulLedger,
    minutesLedger: SimFloorMinutesLedger,
    gameClockSeconds: Int,
    staminaLedger: SimStaminaLedger? = null,
    fatigueTuning: BalanceFatigue = BalanceFatigue.DEFAULT,
    comfort: dev.novux.dynasty.cbb.core.sim.tuning.SimPositionComfortModifiers =
        dev.novux.dynasty.cbb.core.sim.tuning.SimPositionComfortModifiers(),
): Int = weightedOnFloor(
    foulLedger,
    minutesLedger,
    gameClockSeconds,
    staminaLedger,
    fatigueTuning,
    offensiveSide = false,
    comfort = comfort,
    fallback = ::weightedDefensive,
)

private fun GameSimRosterInput.weightedOnFloor(
    foulLedger: PersonalFoulLedger,
    minutesLedger: SimFloorMinutesLedger,
    gameClockSeconds: Int,
    staminaLedger: SimStaminaLedger?,
    fatigueTuning: BalanceFatigue,
    offensiveSide: Boolean,
    comfort: dev.novux.dynasty.cbb.core.sim.tuning.SimPositionComfortModifiers,
    fallback: () -> Int,
): Int {
    val floor = SimFloorRotationPolicy.floorParticipants(
        this,
        foulLedger,
        minutesLedger,
        gameClockSeconds,
        staminaLedger,
        fatigueTuning,
    )
    if (floor.isEmpty()) return fallback()
    val fb = fallback()
    return if (offensiveSide) {
        SlotPlayedFloorRating.offensiveFromFloor(floor, fb, comfort)
    } else {
        SlotPlayedFloorRating.defensiveFromFloor(floor, fb, comfort)
    }
}
