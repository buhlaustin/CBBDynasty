package dev.novux.dynasty.cbb.core.sim

import kotlin.random.Random

/**
 * Turnover log flavors — steal / bad pass / travel / offensive foul
 * ([planning/simulation-engine.md#what-can-happen-on-a-possession], MEGA-SIM-092).
 */
object PossessionTurnoverFlavorPolicy {
    const val QA_ID: String = "MEGA-SIM-092"

    fun sampleForDrivePhase(random: Random): TurnoverTerminal {
        val roll = random.nextDouble()
        return when {
            roll < 0.35 -> TurnoverTerminal.Travel
            roll < 0.70 -> TurnoverTerminal.BadPass
            roll < 0.90 -> TurnoverTerminal.OffensiveFoul
            else -> TurnoverTerminal.Steal
        }
    }

    fun sampleForKickPhase(random: Random): TurnoverTerminal {
        val roll = random.nextDouble()
        return when {
            roll < 0.55 -> TurnoverTerminal.BadPass
            roll < 0.85 -> TurnoverTerminal.Steal
            else -> TurnoverTerminal.Travel
        }
    }

    fun logLabel(terminal: TurnoverTerminal?): String =
        when (terminal) {
            TurnoverTerminal.ShotClockViolation -> "TO shot-clock"
            TurnoverTerminal.Steal -> "TO steal"
            TurnoverTerminal.BadPass -> "TO bad pass"
            TurnoverTerminal.Travel -> "TO travel"
            TurnoverTerminal.OffensiveFoul -> "TO offensive foul"
            TurnoverTerminal.Generic,
            null,
            -> "TO"
        }

    fun chainSuffix(terminal: TurnoverTerminal): String =
        when (terminal) {
            TurnoverTerminal.Steal -> "TO steal"
            TurnoverTerminal.BadPass -> "TO bad pass"
            TurnoverTerminal.Travel -> "TO travel"
            TurnoverTerminal.OffensiveFoul -> "TO offensive foul"
            else -> "TO"
        }
}
