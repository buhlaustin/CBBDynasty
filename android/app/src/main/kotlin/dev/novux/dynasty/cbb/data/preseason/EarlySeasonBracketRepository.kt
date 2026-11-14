package dev.novux.dynasty.cbb.data.preseason

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dev.novux.dynasty.cbb.core.preseason.tournament.EarlySeasonBracketEngine
import dev.novux.dynasty.cbb.core.preseason.tournament.EarlySeasonBracketSeason
import dev.novux.dynasty.cbb.core.preseason.tournament.EarlySeasonEventCatalog
import dev.novux.dynasty.cbb.core.preseason.tournament.EarlySeasonFieldPlanner
import dev.novux.dynasty.cbb.core.preseason.tournament.TournamentInviteResolution
import dev.novux.dynasty.cbb.data.pack.PackRepository
import dev.novux.dynasty.cbb.data.recruiting.RecruitingEconomyRepository
import dev.novux.dynasty.cbb.database.DynastyDatabase
import kotlinx.coroutines.flow.first
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

private val Context.earlySeasonBracketStore: DataStore<Preferences> by preferencesDataStore(
    name = "early_season_brackets",
)

class EarlySeasonBracketRepository(
    private val context: Context,
    private val packRepository: PackRepository,
    private val tournamentInvitesRepository: TournamentInvitesRepository,
    private val database: DynastyDatabase,
    private val recruitingEconomyRepository: RecruitingEconomyRepository,
) {
    suspend fun load(dynastyId: String, seasonIndex: Int): EarlySeasonBracketSeason? {
        val raw = context.earlySeasonBracketStore.data.first()[key(dynastyId, seasonIndex)] ?: return null
        return Json.decodeFromString(raw)
    }

    suspend fun bootstrapForSeason(dynastyId: String, seasonIndex: Int) {
        if (load(dynastyId, seasonIndex) != null) return
        val coach = database.dynastyCoachDao().getByDynastyId(dynastyId) ?: return
        val packId = database.dynastyMetadataDao().getById(dynastyId)?.packId
        val prestige = recruitingEconomyRepository.resolveProgramPrestigeForDynasty(dynastyId, packId)
        val invites = tournamentInvitesRepository.loadState(dynastyId, seasonIndex, prestige)
        val userEventId = (invites.resolution as? TournamentInviteResolution.Accepted)?.event?.templateId
        val teams = packRepository.activeSession().index.allTeamDisplays()
        val seed = dynastyId.hashCode().toLong() xor seasonIndex.toLong()
        val fields = EarlySeasonFieldPlanner.planFields(
            teams = teams,
            userTeamId = coach.userTeamId,
            userAcceptedEventId = userEventId,
            seed = seed,
        )
        val snapshots = fields.mapNotNull { (templateId, participants) ->
            val event = EarlySeasonEventCatalog.byId(templateId) ?: return@mapNotNull null
            if (participants.size < 2) return@mapNotNull null
            templateId to EarlySeasonBracketEngine.bootstrapSnapshot(
                event = event,
                seasonIndex = seasonIndex,
                participantIds = participants.take(event.teamCount),
                seed = seed xor templateId.hashCode().toLong(),
            )
        }.toMap()
        persist(
            dynastyId,
            EarlySeasonBracketSeason(seasonIndex = seasonIndex, brackets = snapshots),
        )
    }

    suspend fun persist(dynastyId: String, season: EarlySeasonBracketSeason) {
        context.earlySeasonBracketStore.edit {
            it[key(dynastyId, season.seasonIndex)] = Json.encodeToString(season)
        }
    }

    private fun key(dynastyId: String, seasonIndex: Int) =
        stringPreferencesKey("brackets_${dynastyId}_$seasonIndex")
}
