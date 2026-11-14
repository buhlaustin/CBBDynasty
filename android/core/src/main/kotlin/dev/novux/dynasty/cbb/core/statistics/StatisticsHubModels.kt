package dev.novux.dynasty.cbb.core.statistics

/** Per-game leader row for Statistics hub ([`game-frame.md`](game-frame.md#statistics-hub)). */
data class StatLeaderRow(
    val rank: Int,
    val playerId: String,
    val playerDisplayName: String,
    val teamId: String,
    val teamDisplayName: String,
    val conferenceId: String,
    val gamesPlayed: Int,
    val valuePerGame: Double,
    val isUserRosterPlayer: Boolean,
)

data class StatisticsLeaderboards(
    val points: List<StatLeaderRow>,
    val assists: List<StatLeaderRow>,
    val rebounds: List<StatLeaderRow>,
    val steals: List<StatLeaderRow>,
    val blocks: List<StatLeaderRow>,
)

data class ConferencePickerRow(
    val conferenceId: String,
    val displayName: String,
)

data class StatisticsHubSnapshot(
    val seasonIndex: Int,
    val userConferenceId: String,
    val hasStats: Boolean,
    val conferences: List<ConferencePickerRow>,
    val national: StatisticsLeaderboards,
    val conferenceLeadersById: Map<String, StatisticsLeaderboards>,
)

enum class StatLeaderCategory {
    Points,
    Assists,
    Rebounds,
    Steals,
    Blocks,
}
