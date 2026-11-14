package dev.novux.dynasty.cbb.data.history

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dev.novux.dynasty.cbb.core.awards.YearlyAwardsSeasonArchive
import dev.novux.dynasty.cbb.core.history.BalanceHistoryParser
import dev.novux.dynasty.cbb.core.history.DynastyHistoryArchive
import dev.novux.dynasty.cbb.core.history.HistoryHubSnapshot
import dev.novux.dynasty.cbb.core.history.HistorySeasonFinalizeEngine
import dev.novux.dynasty.cbb.core.news.NewsHeadlinePublisher
import dev.novux.dynasty.cbb.data.balance.BalanceRulesetRepository
import dev.novux.dynasty.cbb.data.coach.CoachAchievementsRepository
import dev.novux.dynasty.cbb.data.coach.CoachCareerSeasonRepository
import dev.novux.dynasty.cbb.data.league.LeagueWeekBatchRepository
import dev.novux.dynasty.cbb.data.news.NewsFeedRepository
import dev.novux.dynasty.cbb.data.pack.PackRepository
import dev.novux.dynasty.cbb.data.roster.DynastyRosterRepository
import dev.novux.dynasty.cbb.database.DynastyDatabase
import kotlinx.coroutines.flow.first
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

private val Context.dynastyHistoryStore: DataStore<Preferences> by preferencesDataStore(name = "dynasty_history")

class HistoryRepository(
    private val context: Context,
    private val database: DynastyDatabase,
    private val packRepository: PackRepository,
    private val leagueWeekBatchRepository: LeagueWeekBatchRepository,
    private val dynastyRosterRepository: DynastyRosterRepository,
    private val balanceRulesetRepository: BalanceRulesetRepository,
    private val newsFeedRepository: NewsFeedRepository,
    private val coachAchievementsRepository: CoachAchievementsRepository,
    private val coachCareerSeasonRepository: CoachCareerSeasonRepository,
) {
    suspend fun loadArchive(dynastyId: String): DynastyHistoryArchive {
        val raw = context.dynastyHistoryStore.data.first()[archiveKey(dynastyId)] ?: return DynastyHistoryArchive(dynastyId)
        return Json.decodeFromString(raw)
    }

    suspend fun loadHubSnapshot(dynastyId: String, seasonIndex: Int): HistoryHubSnapshot {
        val archive = loadArchive(dynastyId)
        return HistorySeasonFinalizeEngine.toHubSnapshot(archive, seasonIndex)
    }

    suspend fun finalizeSeason(
        dynastyId: String,
        seasonIndex: Int,
        awards: YearlyAwardsSeasonArchive,
    ) {
        val existing = loadArchive(dynastyId)
        if (existing.yearCards.any { it.seasonIndex == seasonIndex }) return
        val coach = database.dynastyCoachDao().getByDynastyId(dynastyId) ?: return
        val standings = leagueWeekBatchRepository.loadStandings(dynastyId, seasonIndex) ?: return
        val roster = dynastyRosterRepository.listPlayers(dynastyId)
        val index = packRepository.activeSession().index
        val tuning = BalanceHistoryParser.parse(balanceRulesetRepository.activeRuleset().history)
        val previousGoat = existing.goatSnapshot.firstOrNull()?.displayName
        val next = HistorySeasonFinalizeEngine.finalizeSeason(
            dynastyId = dynastyId,
            existing = existing,
            seasonIndex = seasonIndex,
            userTeamId = coach.userTeamId,
            roster = roster,
            standings = standings,
            awards = awards,
            packIndex = index,
            tuning = tuning,
        )
        persist(next)
        coachAchievementsRepository.finalizeSeason(dynastyId, standings)
        val yearCard = next.yearCards.lastOrNull { it.seasonIndex == seasonIndex }
        coachCareerSeasonRepository.archiveCompletedSeason(
            dynastyId = dynastyId,
            seasonIndex = seasonIndex,
            userTeamId = coach.userTeamId,
            standings = standings,
            yearCard = yearCard,
        )
        val newGoat = next.goatSnapshot.firstOrNull()?.displayName
        if (newGoat != null && newGoat != previousGoat) {
            val headline = NewsHeadlinePublisher.yearlyAward(
                seasonIndex,
                dev.novux.dynasty.cbb.core.awards.AwardHonorLine(
                    awardLabel = "GOAT #1",
                    winnerName = newGoat,
                    schoolName = next.goatSnapshot.first().schoolLabel,
                ),
                priorityBoost = 5,
            )
            newsFeedRepository.appendAwardsHeadlines(dynastyId, seasonIndex, listOf(headline))
        }
        if (next.yearCards.lastOrNull()?.nationalHofInductees?.isNotEmpty() == true) {
            val hof = next.yearCards.last()
            val headline = NewsHeadlinePublisher.yearlyAward(
                seasonIndex,
                dev.novux.dynasty.cbb.core.awards.AwardHonorLine(
                    awardLabel = "National Hall of Fame",
                    winnerName = hof.nationalHofInductees.joinToString(", "),
                    schoolName = "Class of $seasonIndex",
                ),
            )
            newsFeedRepository.appendAwardsHeadlines(dynastyId, seasonIndex, listOf(headline))
        }
    }

    private suspend fun persist(archive: DynastyHistoryArchive) {
        context.dynastyHistoryStore.edit {
            it[archiveKey(archive.dynastyId)] = Json.encodeToString(archive)
        }
    }

    private fun archiveKey(dynastyId: String) = stringPreferencesKey("history_$dynastyId")
}
