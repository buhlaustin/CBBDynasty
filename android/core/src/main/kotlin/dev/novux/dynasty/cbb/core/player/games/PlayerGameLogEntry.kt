package dev.novux.dynasty.cbb.core.player.games

import kotlinx.serialization.Serializable

@Serializable
data class PlayerGameLogEntry(
    val gameKey: String,
    val rsWeekIndex: Int,
    val opponentLabel: String,
    val isHome: Boolean,
    val teamScore: Int,
    val opponentScore: Int,
    val points: Int,
    val assists: Int,
    val rebounds: Int,
    val steals: Int,
    val blocks: Int,
    val turnovers: Int,
    val fieldGoalsMade: Int = 0,
    val fieldGoalsAttempted: Int = 0,
    val threePointsMade: Int = 0,
    val threePointsAttempted: Int = 0,
    val freeThrowsMade: Int = 0,
    val freeThrowsAttempted: Int = 0,
    val minutes: Int = 0,
    val fromSimArchive: Boolean = false,
) {
    val won: Boolean get() = teamScore > opponentScore
    val resultLabel: String get() = if (won) "W" else "L"
    val scoreLabel: String get() = "$teamScore–$opponentScore"
}
