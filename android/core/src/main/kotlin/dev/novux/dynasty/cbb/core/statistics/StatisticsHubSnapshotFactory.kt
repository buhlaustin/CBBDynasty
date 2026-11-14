package dev.novux.dynasty.cbb.core.statistics

import dev.novux.dynasty.cbb.core.league.LeagueStandingsState
import dev.novux.dynasty.cbb.core.pack.IdentityPackIndex
import dev.novux.dynasty.cbb.core.player.DynastyPlayer
import dev.novux.dynasty.cbb.core.player.stats.PlayerCountingStats
import dev.novux.dynasty.cbb.core.player.stats.PlayerRateStats
import dev.novux.dynasty.cbb.core.player.stats.PlayerStatsEngine

data class StatLeaderCandidate(
    val playerId: String,
    val playerDisplayName: String,
    val teamId: String,
    val teamDisplayName: String,
    val conferenceId: String,
    val gamesPlayed: Int,
    val rates: PlayerRateStats,
    val isUserRosterPlayer: Boolean,
)

object StatisticsHubSnapshotFactory {
    private const val MIN_GAMES = 3
    private const val LEADER_LIMIT = 25

    fun build(
        packIndex: IdentityPackIndex,
        standings: LeagueStandingsState?,
        dynastyId: String,
        userTeamId: String,
        userConferenceId: String,
        userRoster: List<DynastyPlayer>,
        seasonRecords: Map<String, PlayerCountingStats>,
    ): StatisticsHubSnapshot {
        val seasonIndex = standings?.seasonIndex ?: 1
        val candidates = buildCandidates(
            packIndex = packIndex,
            dynastyId = dynastyId,
            userTeamId = userTeamId,
            standings = standings,
            userRoster = userRoster,
            seasonRecords = seasonRecords,
        )
        val hasStats = candidates.any { it.gamesPlayed >= MIN_GAMES }
        val national = leaderboards(candidates)
        val conferences = packIndex.allConferenceDisplays().map {
            ConferencePickerRow(it.conferenceId, it.displayName)
        }
        val conferenceLeaders = conferences.associate { conf ->
            conf.conferenceId to leaderboards(candidates.filter { it.conferenceId == conf.conferenceId })
        }
        return StatisticsHubSnapshot(
            seasonIndex = seasonIndex,
            userConferenceId = userConferenceId,
            hasStats = hasStats,
            conferences = conferences,
            national = national,
            conferenceLeadersById = conferenceLeaders,
        )
    }

    private fun buildCandidates(
        packIndex: IdentityPackIndex,
        dynastyId: String,
        userTeamId: String,
        standings: LeagueStandingsState?,
        userRoster: List<DynastyPlayer>,
        seasonRecords: Map<String, PlayerCountingStats>,
    ): List<StatLeaderCandidate> {
        val userById = userRoster.associateBy { it.playerId }
        val real = userRoster.mapNotNull { player ->
            val gp = player.role.rsGamesPlayedSeason
            if (gp < MIN_GAMES) return@mapNotNull null
            val totals = seasonRecords[player.playerId] ?: SyntheticStatLeaderEngine.seasonTotals(player, gp)
            val team = packIndex.team(userTeamId) ?: return@mapNotNull null
            candidate(player, team.teamId, team.displayName, team.conferenceId, gp, totals, isUser = true)
        }
        val synthetic = packIndex.allTeamDisplays()
            .filter { it.teamId != userTeamId }
            .mapNotNull { team ->
                val rec = standings?.recordFor(team.teamId)
                val gp = ((rec?.wins ?: 0) + (rec?.losses ?: 0)).coerceAtLeast(0)
                if (gp < MIN_GAMES) return@mapNotNull null
                val star = SyntheticStatLeaderEngine.starPlayer(dynastyId, team)
                val totals = SyntheticStatLeaderEngine.seasonTotals(star, gp)
                candidate(star, team.teamId, team.displayName, team.conferenceId, gp, totals, isUser = false)
            }
        val merged = (real + synthetic).distinctBy { it.playerId }
        return merged.ifEmpty {
            userRoster.filter { it.role.rsGamesPlayedSeason > 0 }.mapNotNull { player ->
                val team = packIndex.team(userTeamId) ?: return@mapNotNull null
                val gp = player.role.rsGamesPlayedSeason.coerceAtLeast(1)
                val totals = seasonRecords[player.playerId] ?: SyntheticStatLeaderEngine.seasonTotals(player, gp)
                candidate(player, team.teamId, team.displayName, team.conferenceId, gp, totals, isUser = userById.containsKey(player.playerId))
            }
        }
    }

    private fun candidate(
        player: DynastyPlayer,
        teamId: String,
        teamName: String,
        conferenceId: String,
        gp: Int,
        totals: PlayerCountingStats,
        isUser: Boolean,
    ) = StatLeaderCandidate(
        playerId = player.playerId,
        playerDisplayName = player.identity.displayName.ifBlank { player.playerId },
        teamId = teamId,
        teamDisplayName = teamName,
        conferenceId = conferenceId,
        gamesPlayed = gp,
        rates = PlayerStatsEngine.rateStats(totals, gp),
        isUserRosterPlayer = isUser,
    )

    private fun leaderboards(candidates: List<StatLeaderCandidate>): StatisticsLeaderboards =
        StatisticsLeaderboards(
            points = rank(candidates, StatLeaderCategory.Points),
            assists = rank(candidates, StatLeaderCategory.Assists),
            rebounds = rank(candidates, StatLeaderCategory.Rebounds),
            steals = rank(candidates, StatLeaderCategory.Steals),
            blocks = rank(candidates, StatLeaderCategory.Blocks),
        )

    private fun rank(candidates: List<StatLeaderCandidate>, category: StatLeaderCategory): List<StatLeaderRow> =
        candidates
            .filter { it.gamesPlayed >= MIN_GAMES }
            .sortedWith(
                compareByDescending<StatLeaderCandidate> { categoryValue(it, category) }
                    .thenByDescending { it.gamesPlayed }
                    .thenBy { it.playerDisplayName },
            )
            .take(LEADER_LIMIT)
            .mapIndexed { index, row ->
                StatLeaderRow(
                    rank = index + 1,
                    playerId = row.playerId,
                    playerDisplayName = row.playerDisplayName,
                    teamId = row.teamId,
                    teamDisplayName = row.teamDisplayName,
                    conferenceId = row.conferenceId,
                    gamesPlayed = row.gamesPlayed,
                    valuePerGame = categoryValue(row, category),
                    isUserRosterPlayer = row.isUserRosterPlayer,
                )
            }

    private fun categoryValue(row: StatLeaderCandidate, category: StatLeaderCategory): Double =
        when (category) {
            StatLeaderCategory.Points -> row.rates.pointsPerGame
            StatLeaderCategory.Assists -> row.rates.assistsPerGame
            StatLeaderCategory.Rebounds -> row.rates.reboundsPerGame
            StatLeaderCategory.Steals -> row.rates.stealsPerGame
            StatLeaderCategory.Blocks -> row.rates.blocksPerGame
        }
}
