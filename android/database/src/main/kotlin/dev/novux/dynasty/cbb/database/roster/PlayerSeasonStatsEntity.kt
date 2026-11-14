package dev.novux.dynasty.cbb.database.roster

import androidx.room.Entity

/** Per-player season totals + game log JSON for Stats/Games tabs ([MEGA-COACH-195]). */
@Entity(
    tableName = "player_season_stats",
    primaryKeys = ["dynastyId", "playerId", "seasonIndex"],
)
data class PlayerSeasonStatsEntity(
    val dynastyId: String,
    val playerId: String,
    val seasonIndex: Int,
    val gamesPlayed: Int,
    val schoolDisplayName: String?,
    val points: Int,
    val assists: Int,
    val rebounds: Int,
    val steals: Int,
    val blocks: Int,
    val turnovers: Int,
    val fieldGoalsMade: Int,
    val fieldGoalsAttempted: Int,
    val threePointsMade: Int,
    val threePointsAttempted: Int,
    val freeThrowsMade: Int,
    val freeThrowsAttempted: Int,
    val gameLogJson: String,
)
