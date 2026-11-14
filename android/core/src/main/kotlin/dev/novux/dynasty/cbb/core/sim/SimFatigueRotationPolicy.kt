package dev.novux.dynasty.cbb.core.sim

import dev.novux.dynasty.cbb.core.balance.BalanceFatigue
import dev.novux.dynasty.cbb.core.fatigue.FatigueEngine

/**
 * Fatigue-aware substitution priority during the possession loop (MEGA-SIM-104).
 */
object SimFatigueRotationPolicy {
    const val QA_ID: String = "MEGA-SIM-104"
    const val SHELL_MEGA_ID: String = "MEGA-SHELL-082"

    fun shouldPull(participant: GameSimParticipant, ledger: SimStaminaLedger, tuning: BalanceFatigue): Boolean =
        FatigueEngine.shouldPullInRotation(ledger.stamina(participant.playerId), tuning)

    fun staminaSortKey(participant: GameSimParticipant, ledger: SimStaminaLedger): Int =
        ledger.stamina(participant.playerId)
}
