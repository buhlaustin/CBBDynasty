package dev.novux.dynasty.cbb.data.coach

import dev.novux.dynasty.cbb.core.coach.card.HeadCoachCard
import dev.novux.dynasty.cbb.core.coach.card.deep.CoachCardDeepTabsEngine
import dev.novux.dynasty.cbb.core.coach.card.deep.CoachCardDeepTabsSnapshot
import dev.novux.dynasty.cbb.core.coach.card.history.CoachCareerSeasonMergeEngine
import dev.novux.dynasty.cbb.data.history.HistoryRepository
import dev.novux.dynasty.cbb.data.league.LeagueWeekBatchRepository
import dev.novux.dynasty.cbb.data.offseason.OffseasonWeekTwoRepository
import dev.novux.dynasty.cbb.data.pack.PackRepository
import dev.novux.dynasty.cbb.database.DynastyDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class CoachCardDeepTabsRepository(
    private val database: DynastyDatabase,
    private val historyRepository: HistoryRepository,
    private val coachAchievementsRepository: CoachAchievementsRepository,
    private val leagueWeekBatchRepository: LeagueWeekBatchRepository,
    private val offseasonWeekTwoRepository: OffseasonWeekTwoRepository,
    private val packRepository: PackRepository,
    private val coachCareerSeasonRepository: CoachCareerSeasonRepository,
) {
    suspend fun loadHeadCoachDeepTabs(
        dynastyId: String,
        seasonIndex: Int,
        card: HeadCoachCard,
    ): CoachCardDeepTabsSnapshot = loadDeepTabs(
        dynastyId = dynastyId,
        seasonIndex = seasonIndex,
        schoolDisplayName = card.schoolDisplayName,
        currentRecordLabel = card.seasonRecordLabel,
    )

    suspend fun loadDeepTabs(
        dynastyId: String,
        seasonIndex: Int,
        schoolDisplayName: String,
        currentRecordLabel: String,
    ): CoachCardDeepTabsSnapshot = withContext(Dispatchers.IO) {
        val coach = database.dynastyCoachDao().getByDynastyId(dynastyId)
            ?: return@withContext CoachCardDeepTabsSnapshot.empty(dynastyId)
        val userTeamId = coach.userTeamId
        val ledger = coachAchievementsRepository.loadLedger(dynastyId)
        val archive = historyRepository.loadArchive(dynastyId)
        val seasonIndexes = (ledger.rolledSeasons + seasonIndex).distinct()
        val standingsBySeason = seasonIndexes.associateWith { s ->
            leagueWeekBatchRepository.loadStandings(dynastyId, s)
        }
        val draftBySeason = if (seasonIndex <= 0) {
            emptyMap()
        } else {
            (1..seasonIndex).associateWith { s ->
                offseasonWeekTwoRepository.loadPublishedResults(dynastyId, s)
            }
        }
        val packIndex = packRepository.activeSession().index
        val userConferenceId = packIndex.team(userTeamId)?.conferenceId
        val conferenceTeamIds = userConferenceId
            ?.let { confId -> packIndex.teamsInConference(confId).map { it.teamId }.toSet() }
            ?: emptySet()
        val built = CoachCardDeepTabsEngine.build(
            dynastyId = dynastyId,
            userTeamId = userTeamId,
            schoolDisplayName = schoolDisplayName,
            currentSeasonIndex = seasonIndex,
            currentRecordLabel = currentRecordLabel,
            rolledSeasons = ledger.rolledSeasons,
            archive = archive,
            standingsBySeason = standingsBySeason,
            draftBySeason = draftBySeason,
            conferenceTeamIds = conferenceTeamIds,
            userConferenceId = userConferenceId,
        )
        val persisted = coachCareerSeasonRepository.listForDynasty(dynastyId)
        built.copy(
            seasonHistory = CoachCareerSeasonMergeEngine.merge(
                persisted = persisted,
                liveRows = built.seasonHistory,
                currentSeasonIndex = seasonIndex,
            ),
        )
    }
}
