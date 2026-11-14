package dev.novux.dynasty.cbb.core.preview

data class GamePreviewScoutingLine(
    val pointsPerGame: Double,
    val reboundsPerGame: Double,
    val offensiveEfficiency: Int,
    val pace: Double,
    val netRank: Int?,
    val mediaPollRank: Int?,
)

data class GamePreviewTeamScouting(
    val teamId: String,
    val displayName: String,
    val line: GamePreviewScoutingLine,
)
