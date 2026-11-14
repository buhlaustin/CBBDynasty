package dev.novux.dynasty.cbb.core.sim

import dev.novux.dynasty.cbb.core.balance.BalanceFatigue

/**
 * In-game subs when a player's floor minutes diverge from rotation targets
 * ([planning/simulation-engine.md], MEGA-SIM-103).
 */
object RotationBandFloorPolicy {
    const val MINUTES_AHEAD_BAND: Double = 2.0
    const val MINUTES_BEHIND_BAND: Double = 2.0

    fun expectedMinutesNow(targetMinutes: Int, gameClockRemaining: Int): Double {
        val elapsed = GameSimClock.REGULATION_SECONDS - gameClockRemaining
        val progress = elapsed.toDouble() / GameSimClock.REGULATION_SECONDS.toDouble()
        return targetMinutes * progress
    }

    fun shouldRotateOut(
        participant: GameSimParticipant,
        minutesLedger: SimFloorMinutesLedger,
        gameClockRemaining: Int,
    ): Boolean {
        val expected = expectedMinutesNow(participant.targetMinutes, gameClockRemaining)
        return minutesLedger.floorMinutes(participant.playerId) > expected + MINUTES_AHEAD_BAND
    }

    fun shouldPrioritizeSubIn(
        participant: GameSimParticipant,
        minutesLedger: SimFloorMinutesLedger,
        gameClockRemaining: Int,
    ): Boolean {
        val expected = expectedMinutesNow(participant.targetMinutes, gameClockRemaining)
        return minutesLedger.floorMinutes(participant.playerId) + MINUTES_BEHIND_BAND < expected
    }

    fun applyMinuteBands(
        roster: GameSimRosterInput,
        foulTroubleFloor: List<GameSimParticipant>,
        foulLedger: PersonalFoulLedger,
        minutesLedger: SimFloorMinutesLedger,
        gameClockRemaining: Int,
        staminaLedger: SimStaminaLedger? = null,
        fatigueTuning: BalanceFatigue = BalanceFatigue.DEFAULT,
    ): List<GameSimParticipant> {
        val floor = foulTroubleFloor
            .filterNot { shouldRotateOut(it, minutesLedger, gameClockRemaining) }
            .filterNot { p ->
                staminaLedger != null &&
                    SimFatigueRotationPolicy.shouldPull(p, staminaLedger, fatigueTuning)
            }
            .toMutableList()
        val onFloorIds = floor.map { it.playerId }.toMutableSet()
        while (floor.size < FoulTroubleRotationPolicy.FLOOR_PLAYERS) {
            val candidate = roster.participants
                .asSequence()
                .filter { it.playerId !in onFloorIds }
                .filter { !foulLedger.isDisqualified(it.playerId) }
                .filter {
                    !FoulTroubleRotationPolicy.isFoulTroubleBenched(
                        foulLedger.foulCount(it.playerId),
                        gameClockRemaining,
                    )
                }
                .filter { !shouldRotateOut(it, minutesLedger, gameClockRemaining) }
                .filter { p ->
                    staminaLedger == null ||
                        !SimFatigueRotationPolicy.shouldPull(p, staminaLedger, fatigueTuning)
                }
                .sortedWith(
                    compareByDescending<GameSimParticipant> {
                        shouldPrioritizeSubIn(it, minutesLedger, gameClockRemaining)
                    }.thenByDescending { p ->
                        staminaLedger?.let { SimFatigueRotationPolicy.staminaSortKey(p, it) } ?: 0
                    }.thenByDescending { it.targetMinutes },
                )
                .firstOrNull()
                ?: break
            floor.add(candidate)
            onFloorIds.add(candidate.playerId)
        }
        return floor.take(FoulTroubleRotationPolicy.FLOOR_PLAYERS)
    }
}

object SimFloorRotationPolicy {
    fun floorParticipants(
        roster: GameSimRosterInput,
        foulLedger: PersonalFoulLedger,
        minutesLedger: SimFloorMinutesLedger,
        gameClockSeconds: Int,
        staminaLedger: SimStaminaLedger? = null,
        fatigueTuning: BalanceFatigue = BalanceFatigue.DEFAULT,
    ): List<GameSimParticipant> {
        val foulFloor = FoulTroubleRotationPolicy.floorParticipants(roster, foulLedger, gameClockSeconds)
        return RotationBandFloorPolicy.applyMinuteBands(
            roster,
            foulFloor,
            foulLedger,
            minutesLedger,
            gameClockSeconds,
            staminaLedger,
            fatigueTuning,
        )
    }
}
