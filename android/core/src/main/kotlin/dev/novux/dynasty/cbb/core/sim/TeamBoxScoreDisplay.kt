package dev.novux.dynasty.cbb.core.sim

/** Team tab aggregates for box score UI ([MEGA-SIM-131]). */
data class TeamBoxScoreDisplay(
    val box: TeamBoxScore,
    val fieldGoalPct: Int,
    val threePointPct: Int,
    val freeThrowPct: Int,
    val offensiveRebounds: Int,
    val defensiveRebounds: Int,
) {
    companion object {
        fun from(box: TeamBoxScore, playerLines: List<PlayerBoxScore>): TeamBoxScoreDisplay =
            TeamBoxScoreDisplay(
                box = box,
                fieldGoalPct = pct(box.fieldGoalsMade, box.fieldGoalsAttempted),
                threePointPct = pct(box.threePointsMade, box.threePointsAttempted),
                freeThrowPct = pct(box.freeThrowsMade, box.freeThrowsAttempted),
                offensiveRebounds = playerLines.sumOf { it.offensiveRebounds },
                defensiveRebounds = playerLines.sumOf { it.defensiveRebounds },
            )

        private fun pct(made: Int, attempted: Int): Int =
            if (attempted <= 0) 0 else (made * 100) / attempted
    }
}
