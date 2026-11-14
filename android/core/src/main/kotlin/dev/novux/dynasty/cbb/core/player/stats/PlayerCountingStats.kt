package dev.novux.dynasty.cbb.core.player.stats

import kotlinx.serialization.Serializable

@Serializable
data class PlayerCountingStats(
    val points: Int = 0,
    val assists: Int = 0,
    val rebounds: Int = 0,
    val steals: Int = 0,
    val blocks: Int = 0,
    val turnovers: Int = 0,
    val fieldGoalsMade: Int = 0,
    val fieldGoalsAttempted: Int = 0,
    val threePointsMade: Int = 0,
    val threePointsAttempted: Int = 0,
    val freeThrowsMade: Int = 0,
    val freeThrowsAttempted: Int = 0,
) {
    fun minus(other: PlayerCountingStats): PlayerCountingStats = PlayerCountingStats(
        points = (points - other.points).coerceAtLeast(0),
        assists = (assists - other.assists).coerceAtLeast(0),
        rebounds = (rebounds - other.rebounds).coerceAtLeast(0),
        steals = (steals - other.steals).coerceAtLeast(0),
        blocks = (blocks - other.blocks).coerceAtLeast(0),
        turnovers = (turnovers - other.turnovers).coerceAtLeast(0),
        fieldGoalsMade = (fieldGoalsMade - other.fieldGoalsMade).coerceAtLeast(0),
        fieldGoalsAttempted = (fieldGoalsAttempted - other.fieldGoalsAttempted).coerceAtLeast(0),
        threePointsMade = (threePointsMade - other.threePointsMade).coerceAtLeast(0),
        threePointsAttempted = (threePointsAttempted - other.threePointsAttempted).coerceAtLeast(0),
        freeThrowsMade = (freeThrowsMade - other.freeThrowsMade).coerceAtLeast(0),
        freeThrowsAttempted = (freeThrowsAttempted - other.freeThrowsAttempted).coerceAtLeast(0),
    )

    operator fun plus(other: PlayerCountingStats): PlayerCountingStats = PlayerCountingStats(
        points = points + other.points,
        assists = assists + other.assists,
        rebounds = rebounds + other.rebounds,
        steals = steals + other.steals,
        blocks = blocks + other.blocks,
        turnovers = turnovers + other.turnovers,
        fieldGoalsMade = fieldGoalsMade + other.fieldGoalsMade,
        fieldGoalsAttempted = fieldGoalsAttempted + other.fieldGoalsAttempted,
        threePointsMade = threePointsMade + other.threePointsMade,
        threePointsAttempted = threePointsAttempted + other.threePointsAttempted,
        freeThrowsMade = freeThrowsMade + other.freeThrowsMade,
        freeThrowsAttempted = freeThrowsAttempted + other.freeThrowsAttempted,
    )

    companion object {
        val ZERO = PlayerCountingStats()
    }
}
