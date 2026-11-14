package dev.novux.dynasty.cbb.core.sim

/** Post-run sanity checks ([planning/simulation-engine.md#statistical-coherence]). */
object BoxScoreCoherence {
    fun assertValid(result: GameSimResult) {
        require(result.homeScore == result.homeBox.points) {
            "home score ${result.homeScore} != box ${result.homeBox.points}"
        }
        require(result.awayScore == result.awayBox.points) {
            "away score ${result.awayScore} != box ${result.awayBox.points}"
        }
        require(result.homeBox.fieldGoalsMade <= result.homeBox.fieldGoalsAttempted)
        require(result.awayBox.fieldGoalsMade <= result.awayBox.fieldGoalsAttempted)
        require(result.homeBox.threePointsMade <= result.homeBox.threePointsAttempted)
        require(result.awayBox.threePointsMade <= result.awayBox.threePointsAttempted)
        require(result.homeBox.threePointsMade <= result.homeBox.fieldGoalsMade)
        require(result.awayBox.threePointsMade <= result.awayBox.fieldGoalsMade)
        require(result.homeBox.possessions > 0 && result.awayBox.possessions > 0)
        require(result.homeBox.freeThrowsMade <= result.homeBox.freeThrowsAttempted)
        require(result.awayBox.freeThrowsMade <= result.awayBox.freeThrowsAttempted)
        assertPlayerSums(result.homeBox, result.homePlayerBox)
        assertPlayerSums(result.awayBox, result.awayPlayerBox)
        TeamPlayerMinutesCoherence.assertTeamMinutes(result)
    }

    private fun assertPlayerSums(team: TeamBoxScore, players: List<PlayerBoxScore>) {
        if (players.isEmpty()) return
        require(players.sumOf { it.points } == team.points) {
            "team points ${team.points} != player sum ${players.sumOf { it.points }}"
        }
        require(players.sumOf { it.fieldGoalsAttempted } == team.fieldGoalsAttempted)
        require(players.sumOf { it.fieldGoalsMade } == team.fieldGoalsMade)
        require(players.sumOf { it.threePointsAttempted } == team.threePointsAttempted)
        require(players.sumOf { it.threePointsMade } == team.threePointsMade)
        require(players.sumOf { it.freeThrowsAttempted } == team.freeThrowsAttempted)
        require(players.sumOf { it.freeThrowsMade } == team.freeThrowsMade)
        require(players.sumOf { it.turnovers } == team.turnovers)
        require(players.sumOf { it.personalFouls } == team.personalFouls)
        players.forEach { line ->
            require(line.fieldGoalsMade <= line.fieldGoalsAttempted)
            require(line.threePointsMade <= line.threePointsAttempted)
            require(line.threePointsMade <= line.fieldGoalsMade)
            require(line.freeThrowsMade <= line.freeThrowsAttempted)
        }
    }
}
