package dev.novux.dynasty.cbb.core.coach.contract

/** AD contract performance targets ([MEGA-COACH-120]). */
enum class CoachPerformanceGoalKind {
    WinTotal,
    Conference,
    Postseason,
}

data class CoachPerformanceGoalLine(
    val kind: CoachPerformanceGoalKind,
    val targetDescription: String,
)

data class HeadCoachPerformanceGoals(
    val lines: List<CoachPerformanceGoalLine>,
) {
    fun displayLines(): List<String> = lines.map { line ->
        when (line.kind) {
            CoachPerformanceGoalKind.WinTotal -> "Win total: ${line.targetDescription}"
            CoachPerformanceGoalKind.Conference -> "Conference: ${line.targetDescription}"
            CoachPerformanceGoalKind.Postseason -> "Postseason: ${line.targetDescription}"
        }
    }
}

object HeadCoachPerformanceGoalsEngine {
    const val MEGA_ID = "MEGA-COACH-120"

    private const val TYPICAL_CONFERENCE_GAMES = 18

    fun build(programPrestige: Int, schoolDisplayName: String): HeadCoachPerformanceGoals {
        val prestige = programPrestige.coerceIn(1, 99)
        val winFloor = winTotalFloor(prestige)
        val confWins = conferenceWinsFloor(prestige)
        val postseason = postseasonFloor(prestige, schoolDisplayName)
        return HeadCoachPerformanceGoals(
            lines = listOf(
                CoachPerformanceGoalLine(
                    kind = CoachPerformanceGoalKind.WinTotal,
                    targetDescription = "At least $winFloor wins",
                ),
                CoachPerformanceGoalLine(
                    kind = CoachPerformanceGoalKind.Conference,
                    targetDescription = "At least $confWins conference wins (~.500+ vs league schedule)",
                ),
                CoachPerformanceGoalLine(
                    kind = CoachPerformanceGoalKind.Postseason,
                    targetDescription = postseason,
                ),
            ),
        )
    }

    fun winTotalFloor(programPrestige: Int): Int {
        val prestige = programPrestige.coerceIn(1, 99)
        return 12 + ((prestige - 1) * 14 / 98)
    }

    fun conferenceWinsFloor(programPrestige: Int): Int {
        val prestige = programPrestige.coerceIn(1, 99)
        val winPct = 0.42 + (prestige / 99.0) * 0.28
        return (TYPICAL_CONFERENCE_GAMES * winPct).toInt().coerceAtLeast(7)
    }

    fun postseasonFloor(programPrestige: Int, schoolDisplayName: String): String {
        val prestige = programPrestige.coerceIn(1, 99)
        return when {
            prestige >= 78 -> "Super 16 or better ($schoolDisplayName standard)"
            prestige >= 50 -> "National postseason — Opening Round floor"
            else -> "Conference tournament quarterfinal or better"
        }
    }
}
