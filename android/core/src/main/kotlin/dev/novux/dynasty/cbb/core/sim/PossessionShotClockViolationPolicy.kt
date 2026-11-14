package dev.novux.dynasty.cbb.core.sim

/** Shot-clock violation terminal turnover in the possession loop ([MEGA-SIM-004]). */
object PossessionShotClockViolationPolicy {
    const val MEGA_ID: String = "MEGA-SIM-004"

    val turnoverTerminal: TurnoverTerminal = TurnoverTerminal.ShotClockViolation
}