package dev.novux.dynasty.cbb.core.sim

/** Closes attribution gaps so player counting stats sum to the team box. */
object PlayerBoxReconciler {
    fun reconcile(team: TeamBoxScore, ledger: PlayerBoxScoreLedger) {
        val lines = ledger.snapshot()
        if (lines.isEmpty()) return
        val anchor = lines.maxByOrNull { it.minutesSeconds }?.playerId ?: lines.first().playerId
        val cur = lines.first { it.playerId == anchor }
        fun gap(teamVal: Int, sum: Int) = teamVal - sum
        ledger.replace(
            anchor,
            cur.copy(
                points = (cur.points + gap(team.points, lines.sumOf { it.points })).coerceAtLeast(0),
                fieldGoalsAttempted = (cur.fieldGoalsAttempted + gap(team.fieldGoalsAttempted, lines.sumOf { it.fieldGoalsAttempted })).coerceAtLeast(0),
                fieldGoalsMade = (cur.fieldGoalsMade + gap(team.fieldGoalsMade, lines.sumOf { it.fieldGoalsMade })).coerceAtLeast(0),
                threePointsAttempted = (cur.threePointsAttempted + gap(team.threePointsAttempted, lines.sumOf { it.threePointsAttempted })).coerceAtLeast(0),
                threePointsMade = (cur.threePointsMade + gap(team.threePointsMade, lines.sumOf { it.threePointsMade })).coerceAtLeast(0),
                freeThrowsAttempted = (cur.freeThrowsAttempted + gap(team.freeThrowsAttempted, lines.sumOf { it.freeThrowsAttempted })).coerceAtLeast(0),
                freeThrowsMade = (cur.freeThrowsMade + gap(team.freeThrowsMade, lines.sumOf { it.freeThrowsMade })).coerceAtLeast(0),
                turnovers = (cur.turnovers + gap(team.turnovers, lines.sumOf { it.turnovers })).coerceAtLeast(0),
                personalFouls = (cur.personalFouls + gap(team.personalFouls, lines.sumOf { it.personalFouls })).coerceAtLeast(0),
            ),
        )
    }
}
