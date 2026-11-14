package dev.novux.dynasty.cbb.core.carousel

/**
 * GDD carousel AI factors — firings, extensions, poach immunity ([planning/game-design.md#carousel-ai-behavior-spec], [MEGA-GDD-119]).
 */
object CarouselAiBehaviorTable {
    const val MEGA_ID: String = "MEGA-GDD-119"

    enum class Factor {
        /** Last season wins → job security. */
        LastSeasonWins,
        /** Postseason run → extension bias + short poach immunity. */
        PostseasonRun,
        /** Mid-major success vs program prestige → poach / firing mismatch. */
        PrestigeMismatch,
        /** Coach age / tenure → regression to mean on firing. */
        AgeTenure,
        /** Portal exodus → elevated firing risk. */
        PortalExodus,
    }

    const val FIRING_RISK_FIRING_THRESHOLD: Int = 58
    const val EXTENSION_MIN_AFFINITY: Int = 52

    /** Positive points increase firing risk; negative points reduce it. */
    fun firingRiskDelta(factor: Factor, row: CarouselLeagueCoachRow): Int =
        when (factor) {
            Factor.LastSeasonWins -> when {
                row.wins <= 12 -> 28
                row.wins <= 17 -> 12
                row.wins <= 22 -> 0
                else -> -18
            }
            Factor.PostseasonRun -> when {
                row.deepPostseasonRun -> -22
                row.madePostseason -> -10
                else -> 8
            }
            Factor.PrestigeMismatch -> {
                val delta = row.wins - expectedWinsForPrestige(row.programPrestige)
                when {
                    delta >= 6 && row.programPrestige <= 55 -> -6
                    delta <= -8 && row.programPrestige >= 80 -> 20
                    delta <= -5 -> 10
                    else -> 0
                }
            }
            Factor.AgeTenure -> {
                val tenure = row.tenureYears.coerceAtLeast(0)
                val age = row.coachAge.coerceAtLeast(30)
                when {
                    tenure >= 12 && age >= 58 -> 14
                    tenure <= 2 -> -4
                    else -> 0
                }
            }
            Factor.PortalExodus -> (row.portalDepartures.coerceIn(0, 8) * 5)
        }

    fun totalFiringRisk(row: CarouselLeagueCoachRow): Int =
        Factor.entries.sumOf { firingRiskDelta(it, row).coerceAtLeast(-30) }.coerceIn(0, 100)

    /** Higher affinity → AD extends rather than parts ways. */
    fun extensionAffinity(row: CarouselLeagueCoachRow): Int {
        var score = 45
        score -= totalFiringRisk(row) / 2
        if (row.madePostseason) score += 12
        if (row.deepPostseasonRun) score += 10
        if (row.contractYearsLeft <= 1) score += 8
        if (row.jobSecurityScore >= 75) score += 15
        return score.coerceIn(0, 100)
    }

    fun shouldPoachImmunity(row: CarouselLeagueCoachRow): Boolean =
        row.deepPostseasonRun && row.jobSecurityScore >= 60

    private fun expectedWinsForPrestige(prestige: Int): Int =
        when {
            prestige >= 90 -> 24
            prestige >= 80 -> 21
            prestige >= 65 -> 18
            prestige >= 50 -> 15
            else -> 12
        }
}
