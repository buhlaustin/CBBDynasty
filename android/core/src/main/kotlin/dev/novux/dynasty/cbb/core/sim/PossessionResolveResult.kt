package dev.novux.dynasty.cbb.core.sim

enum class PossessionChange {
    DefenseTakesBall,
    OffenseRetainsOffensiveRebound,
}

/** Distinguishes turnover flavors in logs and future stat splits (MEGA-SIM-087). */
enum class TurnoverTerminal {
    Generic,
    ShotClockViolation,
    Steal,
    BadPass,
    Travel,
    OffensiveFoul,
}

data class PossessionResolveResult(
    val box: TeamBoxScore,
    val change: PossessionChange,
    val turnoverTerminal: TurnoverTerminal? = null,
    /** drive→kick→shot chain text for possession log (MEGA-SIM-089). */
    val subPhaseChain: String? = null,
    val blockedShot: Boolean = false,
    val foulOnBlock: Boolean = false,
    val shootingFoul: Boolean = false,
    val andOne: Boolean = false,
    /** Scheduled FT attempts before session rolls makes (1 = and-1, 2/3 = miss foul). */
    val freeThrowAttempts: Int = 0,
)
