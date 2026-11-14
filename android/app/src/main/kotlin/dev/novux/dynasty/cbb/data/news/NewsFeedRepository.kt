package dev.novux.dynasty.cbb.data.news

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dev.novux.dynasty.cbb.core.injuries.GameSimInjuryEvent
import dev.novux.dynasty.cbb.core.league.LeagueGameResult
import dev.novux.dynasty.cbb.core.league.LeagueStandingsState
import dev.novux.dynasty.cbb.core.news.BracketologyNewsEngine
import dev.novux.dynasty.cbb.core.news.PollNetMovementNewsEngine
import dev.novux.dynasty.cbb.core.news.NewsHeadline
import dev.novux.dynasty.cbb.core.news.NewsHeadlinePublisher
import dev.novux.dynasty.cbb.core.news.ShallowNewsFeed
import dev.novux.dynasty.cbb.core.news.ShallowNewsFeedSnapshot
import dev.novux.dynasty.cbb.core.roster.RosterPlayer
import dev.novux.dynasty.cbb.data.preseason.IncomingClassSeedRepository
import kotlinx.coroutines.flow.first
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

private val Context.newsFeedStore: DataStore<Preferences> by preferencesDataStore(name = "news_feed")

class NewsFeedRepository(
    private val context: Context,
    private val incomingClassSeedRepository: IncomingClassSeedRepository,
) {
    suspend fun loadArchive(dynastyId: String, seasonIndex: Int): List<NewsHeadline> {
        val raw = context.newsFeedStore.data.first()[key(dynastyId, seasonIndex)] ?: return emptyList()
        return Json.decodeFromString(raw)
    }

    /** Publishes the Top 60 spotlight when the enrolling class pool exists (MEGA-RECR-053). */
    suspend fun publishRecruitingSpotlightWhenPoolReady(dynastyId: String, seasonIndex: Int) {
        ensureRecruitingSpotlight(dynastyId, seasonIndex)
    }

    suspend fun storiesFeed(dynastyId: String, seasonIndex: Int): List<NewsHeadline> {
        ensureRecruitingSpotlight(dynastyId, seasonIndex)
        return loadArchive(dynastyId, seasonIndex)
            .sortedWith(compareByDescending<NewsHeadline> { it.createdAtMs }.thenByDescending { it.priority })
    }

    suspend fun shallowFeed(
        dynastyId: String,
        seasonIndex: Int,
        rsWeekIndex: Int,
        userRoster: List<RosterPlayer>,
    ): List<NewsHeadline> =
        shallowFeedSnapshot(dynastyId, seasonIndex, rsWeekIndex, userRoster).headlines

    suspend fun shallowFeedSnapshot(
        dynastyId: String,
        seasonIndex: Int,
        rsWeekIndex: Int,
        userRoster: List<RosterPlayer>,
    ): ShallowNewsFeedSnapshot {
        ensureRecruitingSpotlight(dynastyId, seasonIndex)
        val archive = loadArchive(dynastyId, seasonIndex)
        val liveUser = userRoster.mapNotNull { player ->
            NewsHeadlinePublisher.userInjuryFromRoster(seasonIndex, rsWeekIndex, player)
        }
        return ShallowNewsFeed.snapshot(archive, liveUser)
    }

    suspend fun appendUserInjuries(
        dynastyId: String,
        seasonIndex: Int,
        rsWeekIndex: Int,
        playerNames: Map<String, String>,
        events: List<GameSimInjuryEvent>,
    ) {
        if (events.isEmpty()) return
        val headlines = events.map { event ->
            val name = playerNames[event.playerId] ?: event.playerId
            NewsHeadlinePublisher.userInjury(seasonIndex, rsWeekIndex, name, event)
        }
        appendAll(dynastyId, seasonIndex, headlines)
    }

    suspend fun appendAwardsHeadlines(
        dynastyId: String,
        seasonIndex: Int,
        headlines: List<NewsHeadline>,
    ) {
        if (headlines.isEmpty()) return
        val tagged = headlines.map {
            if (it.id.startsWith("award-")) it else it.copy(id = "award-${it.id}")
        }
        appendAll(dynastyId, seasonIndex, tagged)
    }

    suspend fun appendLeagueWeek(
        dynastyId: String,
        standings: LeagueStandingsState,
        userTeamId: String? = null,
        teamDisplayName: (String) -> String = { it },
    ) {
        val week = standings.lastCommittedRsWeek
        val headlines = mutableListOf<NewsHeadline>()
        NewsHeadlinePublisher.leagueInjuryWire(
            standings.seasonIndex,
            week,
            standings.aiInjuryRollsLastWeek,
        )?.let { headlines += it }
        standings.lastWeekResults
            .sortedByDescending { kotlin.math.abs(it.homeScore - it.awayScore) }
            .take(3)
            .forEach { result ->
                headlines += NewsHeadlinePublisher.leagueGameResult(standings.seasonIndex, result)
            }
        BracketologyNewsEngine.headlineForWeek(
            standings = standings,
            rsWeekIndex = week,
            teamDisplayName = teamDisplayName,
            userTeamId = userTeamId,
        )?.let { headlines += NewsHeadlinePublisher.bracketology(it) }
        PollNetMovementNewsEngine.headlineForWeek(
            standings = standings,
            rsWeekIndex = week,
            teamDisplayName = teamDisplayName,
            userTeamId = userTeamId,
        )?.let { headlines += it }
        appendAll(dynastyId, seasonIndex = standings.seasonIndex, headlines)
    }

    private suspend fun ensureRecruitingSpotlight(dynastyId: String, seasonIndex: Int) {
        val storyId = dev.novux.dynasty.cbb.core.preseason.PreseasonNewsFeedPolicy.top60StoryId(seasonIndex)
        val archive = loadArchive(dynastyId, seasonIndex)
        if (archive.any { it.id == storyId }) return
        if (!incomingClassSeedRepository.isSeeded(dynastyId, seasonIndex)) return
        val headline = incomingClassSeedRepository.loadHeadline(dynastyId, seasonIndex)?.headline ?: return
        appendAll(
            dynastyId,
            seasonIndex,
            listOf(NewsHeadlinePublisher.recruitingSpotlight(seasonIndex, headline)),
        )
    }

    private suspend fun appendAll(dynastyId: String, seasonIndex: Int, headlines: List<NewsHeadline>) {
        if (headlines.isEmpty()) return
        val existing = loadArchive(dynastyId, seasonIndex)
        val merged = (existing + headlines).distinctBy { it.id }.takeLast(200)
        context.newsFeedStore.edit {
            it[key(dynastyId, seasonIndex)] = Json.encodeToString(merged)
        }
    }

    private fun key(dynastyId: String, seasonIndex: Int) =
        stringPreferencesKey("news_${dynastyId}_$seasonIndex")
}
