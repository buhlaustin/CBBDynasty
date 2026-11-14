package dev.novux.dynasty.cbb.data.coach

import dev.novux.dynasty.cbb.core.coach.card.deep.CoachCardDeepTabsEngine
import dev.novux.dynasty.cbb.core.coach.card.history.CoachSeasonHistoryRow
import dev.novux.dynasty.cbb.core.history.HistoryYearCard
import dev.novux.dynasty.cbb.core.league.LeagueStandingsState
import dev.novux.dynasty.cbb.data.pack.PackRepository
import dev.novux.dynasty.cbb.database.DynastyDatabase
import dev.novux.dynasty.cbb.database.dynasty.toEntity
import dev.novux.dynasty.cbb.database.dynasty.toRow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class CoachCareerSeasonRepository(
    private val database: DynastyDatabase,
    private val packRepository: PackRepository,
) {
    private val dao = database.coachCareerSeasonDao()

    suspend fun listForDynasty(dynastyId: String): List<CoachSeasonHistoryRow> = withContext(Dispatchers.IO) {
        dao.listForDynasty(dynastyId).map { it.toRow() }
    }

    suspend fun archiveCompletedSeason(
        dynastyId: String,
        seasonIndex: Int,
        userTeamId: String,
        standings: LeagueStandingsState,
        yearCard: HistoryYearCard?,
    ) = withContext(Dispatchers.IO) {
        val index = packRepository.activeSession().index
        val schoolDisplayName = index.team(userTeamId)?.displayName ?: "School"
        val userConferenceId = index.team(userTeamId)?.conferenceId
        val conferenceTeamIds = userConferenceId
            ?.let { confId -> index.teamsInConference(confId).map { it.teamId }.toSet() }
            ?: emptySet()
        val row = CoachCardDeepTabsEngine.buildSeasonHistoryRow(
            seasonIndex = seasonIndex,
            schoolDisplayName = schoolDisplayName,
            currentSeasonIndex = seasonIndex,
            currentRecordLabel = standings.recordFor(userTeamId).let { "${it.wins}-${it.losses}" },
            userTeamId = userTeamId,
            yearCard = yearCard,
            standings = standings,
            conferenceTeamIds = conferenceTeamIds,
            userConferenceId = userConferenceId,
        )
        dao.upsert(row.toEntity(dynastyId, archivedAtEpochMillis = System.currentTimeMillis()))
    }

    suspend fun deleteForDynasty(dynastyId: String) = withContext(Dispatchers.IO) {
        dao.deleteForDynasty(dynastyId)
    }
}
