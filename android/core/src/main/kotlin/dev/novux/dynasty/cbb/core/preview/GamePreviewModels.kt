package dev.novux.dynasty.cbb.core.preview

import dev.novux.dynasty.cbb.core.school.SchoolHubOverview

enum class GamePreviewVenue {
    Home,
    Away,
    Neutral,
}

data class TeamPreviewSide(
    val teamId: String,
    val displayName: String,
    val offensiveRating: Int,
    val defensiveRating: Int,
    val rpiIndex: Double,
) {
    companion object {
        fun fromOverview(overview: SchoolHubOverview): TeamPreviewSide = TeamPreviewSide(
            teamId = overview.teamId,
            displayName = overview.displayName,
            offensiveRating = overview.teamOffRating,
            defensiveRating = overview.teamDefRating,
            rpiIndex = RpiIndex.fromOverview(overview),
        )

        fun fromPrestige(teamId: String, displayName: String, prestige: Int): TeamPreviewSide {
            val p = prestige.coerceIn(1, 99)
            return TeamPreviewSide(
                teamId = teamId,
                displayName = displayName,
                offensiveRating = 58 + (p * 0.35).toInt(),
                defensiveRating = 56 + (p * 0.33).toInt(),
                rpiIndex = RpiIndex.fromPrestige(p, 0, 0),
            )
        }
    }
}

data class HeadToHeadMeeting(
    val seasonLabel: String,
    val userScore: Int,
    val opponentScore: Int,
)

data class HeadToHeadRecord(
    val userWins: Int,
    val userLosses: Int,
    val meetings: List<HeadToHeadMeeting>,
)

data class GamePreviewRequest(
    val user: TeamPreviewSide,
    val opponent: TeamPreviewSide,
    val venue: GamePreviewVenue,
    val headToHead: HeadToHeadRecord = HeadToHeadRecord(0, 0, emptyList()),
)

data class GamePreviewResult(
    val userWinPercent: Int,
    val predictedUserScore: Int,
    val predictedOpponentScore: Int,
    val headToHead: HeadToHeadRecord,
    val userScouting: GamePreviewTeamScouting? = null,
    val opponentScouting: GamePreviewTeamScouting? = null,
)
