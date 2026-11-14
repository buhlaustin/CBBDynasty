package dev.novux.dynasty.cbb.data.statistics

import dev.novux.dynasty.cbb.core.statistics.StatisticsHubSnapshot
import dev.novux.dynasty.cbb.core.statistics.StatisticsHubSnapshotFactory
import dev.novux.dynasty.cbb.data.league.LeagueWeekBatchRepository
import dev.novux.dynasty.cbb.data.pack.PackRepository
import dev.novux.dynasty.cbb.data.player.PlayerStatsRepository
import dev.novux.dynasty.cbb.data.roster.DynastyRosterRepository
import dev.novux.dynasty.cbb.database.DynastyDatabase

class StatisticsHubRepository(
    private val packRepository: PackRepository,
    private val leagueWeekBatchRepository: LeagueWeekBatchRepository,
    private val dynastyRosterRepository: DynastyRosterRepository,
    private val playerStatsRepository: PlayerStatsRepository,
    private val database: DynastyDatabase,
) {
    suspend fun load(dynastyId: String, seasonIndex: Int): StatisticsHubSnapshot {
        val index = packRepository.activeSession().index
        val standings = leagueWeekBatchRepository.loadStandings(dynastyId, seasonIndex)
        val coach = database.dynastyCoachDao().getByDynastyId(dynastyId)
        val userTeamId = coach?.userTeamId.orEmpty()
        val userConferenceId = index.team(userTeamId)?.conferenceId.orEmpty()
        val roster = dynastyRosterRepository.listDynastyPlayers(dynastyId)
        val records = playerStatsRepository.seasonTotalsByPlayer(dynastyId, seasonIndex)
        return StatisticsHubSnapshotFactory.build(
            packIndex = index,
            standings = standings,
            dynastyId = dynastyId,
            userTeamId = userTeamId,
            userConferenceId = userConferenceId,
            userRoster = roster,
            seasonRecords = records,
        )
    }
}
