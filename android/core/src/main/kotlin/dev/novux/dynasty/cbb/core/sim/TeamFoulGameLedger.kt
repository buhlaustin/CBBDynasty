package dev.novux.dynasty.cbb.core.sim

/**
 * Team fouls per regulation half for bonus logic and box summaries (MEGA-SIM-105).
 */
data class TeamFoulHalfSummary(
    val homeFirstHalf: Int = 0,
    val awayFirstHalf: Int = 0,
    val homeSecondHalf: Int = 0,
    val awaySecondHalf: Int = 0,
) {
    fun homeRegulationTotal(): Int = homeFirstHalf + homeSecondHalf
    fun awayRegulationTotal(): Int = awayFirstHalf + awaySecondHalf
}

class TeamFoulGameLedger(
    resume: MidGameState? = null,
) {
    private val currentHalf = TeamFoulHalfLedger(
        initialHome = resume?.homeTeamFoulsInHalf ?: 0,
        initialAway = resume?.awayTeamFoulsInHalf ?: 0,
    )
    private var homeFirstHalfFouls: Int = resume?.homeTeamFoulsFirstHalf ?: 0
    private var awayFirstHalfFouls: Int = resume?.awayTeamFoulsFirstHalf ?: 0
    private var inSecondHalf: Boolean =
        (resume?.homeTeamFoulsFirstHalf ?: 0) > 0 ||
            (resume?.awayTeamFoulsFirstHalf ?: 0) > 0 ||
            (resume?.gameClockSeconds ?: GameSimClock.REGULATION_SECONDS) <= GameSimClock.HALF_SECONDS

    fun recordDefensiveFoul(defenseIsHome: Boolean): Int =
        currentHalf.recordDefensiveFoul(defenseIsHome)

    fun onRegulationHalfBoundaryCrossed() {
        if (inSecondHalf) return
        homeFirstHalfFouls = currentHalf.homeFoulsInHalf
        awayFirstHalfFouls = currentHalf.awayFoulsInHalf
        inSecondHalf = true
        currentHalf.reset()
    }

    fun onOvertimePeriodStart() {
        currentHalf.reset()
    }

    fun currentHomeFoulsInHalf(): Int = currentHalf.homeFoulsInHalf
    fun currentAwayFoulsInHalf(): Int = currentHalf.awayFoulsInHalf

    fun midGameSnapshot(): Pair<Int, Int> = currentHalf.snapshot()

    fun summary(): TeamFoulHalfSummary =
        TeamFoulHalfSummary(
            homeFirstHalf = if (inSecondHalf) homeFirstHalfFouls else currentHalf.homeFoulsInHalf,
            awayFirstHalf = if (inSecondHalf) awayFirstHalfFouls else currentHalf.awayFoulsInHalf,
            homeSecondHalf = if (inSecondHalf) currentHalf.homeFoulsInHalf else 0,
            awaySecondHalf = if (inSecondHalf) currentHalf.awayFoulsInHalf else 0,
        )

    fun restoreMidGameFields(): MidGameTeamFoulFields = MidGameTeamFoulFields(
        homeTeamFoulsInHalf = currentHalf.homeFoulsInHalf,
        awayTeamFoulsInHalf = currentHalf.awayFoulsInHalf,
        homeTeamFoulsFirstHalf = homeFirstHalfFouls,
        awayTeamFoulsFirstHalf = awayFirstHalfFouls,
    )
}

data class MidGameTeamFoulFields(
    val homeTeamFoulsInHalf: Int,
    val awayTeamFoulsInHalf: Int,
    val homeTeamFoulsFirstHalf: Int,
    val awayTeamFoulsFirstHalf: Int,
)
