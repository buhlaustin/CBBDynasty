package dev.novux.dynasty.cbb.data.preseason

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dev.novux.dynasty.cbb.core.preseason.tournament.RegularSeasonSlotBudget
import dev.novux.dynasty.cbb.core.schedule.PreseasonSchedulePlan
import dev.novux.dynasty.cbb.core.schedule.PreseasonScheduleSolver
import dev.novux.dynasty.cbb.core.schedule.PreseasonScheduleValidator
import dev.novux.dynasty.cbb.core.schedule.RegularSeasonScheduleBuilderUxPolicy
import dev.novux.dynasty.cbb.core.schedule.RegularSeasonScheduleSolveResult
import dev.novux.dynasty.cbb.core.schedule.ScheduleGameKind
import dev.novux.dynasty.cbb.core.schedule.ScheduledGameStub
import dev.novux.dynasty.cbb.core.schedule.ExhibitionGameDraft
import dev.novux.dynasty.cbb.core.schedule.ExhibitionScheduleUxPolicy
import dev.novux.dynasty.cbb.core.schedule.PreseasonScheduleConstants
import dev.novux.dynasty.cbb.core.pack.TeamDisplayOverlay
import dev.novux.dynasty.cbb.data.league.LeagueScheduleRepository
import dev.novux.dynasty.cbb.data.pack.PackRepository
import dev.novux.dynasty.cbb.data.recruiting.RecruitingEconomyRepository
import dev.novux.dynasty.cbb.database.DynastyDatabase
import kotlinx.coroutines.flow.first

private val Context.preseasonScheduleStore: DataStore<Preferences> by preferencesDataStore(
    name = "preseason_schedule",
)

class PreseasonScheduleRepository(
    private val context: Context,
    private val packRepository: PackRepository,
    private val database: DynastyDatabase,
    private val tournamentInvitesRepository: TournamentInvitesRepository,
    private val leagueScheduleRepository: LeagueScheduleRepository,
    private val recruitingEconomyRepository: RecruitingEconomyRepository,
) {
    /** [TournamentInvitesLivePrestigePolicy] */
    private suspend fun liveProgramPrestige(dynastyId: String): Int {
        val packId = database.dynastyMetadataDao().getById(dynastyId)?.packId
        return recruitingEconomyRepository.resolveProgramPrestigeForDynasty(dynastyId, packId)
    }
    suspend fun loadExhibitionCount(dynastyId: String, seasonIndex: Int): Int =
        context.preseasonScheduleStore.data.first()[exhibitionKey(dynastyId, seasonIndex)] ?: 0

    suspend fun loadPlan(dynastyId: String, seasonIndex: Int): PreseasonSchedulePlan? {
        val raw = context.preseasonScheduleStore.data.first()[planKey(dynastyId, seasonIndex)] ?: return null
        return decodePlan(raw)
    }

    suspend fun loadExhibitionDrafts(dynastyId: String, seasonIndex: Int): List<ExhibitionGameDraft> {
        val count = loadExhibitionCount(dynastyId, seasonIndex)
        if (count == 0) return emptyList()
        val raw = context.preseasonScheduleStore.data.first()[exhibitionDetailsKey(dynastyId, seasonIndex)]
        val decoded = decodeExhibitionDrafts(raw).take(count)
        return if (decoded.size == count) {
            decoded
        } else {
            List(count) { index ->
                ExhibitionGameDraft(exhibitionWeekIndex = (index + 1).coerceAtMost(PreseasonScheduleConstants.MAX_EXHIBITION_GAMES))
            }
        }
    }

    suspend fun loadNonConferenceOpponents(dynastyId: String): List<TeamDisplayOverlay> {
        val coach = database.dynastyCoachDao().getByDynastyId(dynastyId) ?: return emptyList()
        val userTeam = packRepository.activeSession().index.team(coach.userTeamId) ?: return emptyList()
        return packRepository.activeSession().index.allTeamDisplays()
            .filter { it.conferenceId != userTeam.conferenceId }
            .sortedBy { it.displayName }
    }

    suspend fun saveExhibitions(
        dynastyId: String,
        seasonIndex: Int,
        count: Int,
        drafts: List<ExhibitionGameDraft>,
    ) {
        val capped = count.coerceIn(0, PreseasonScheduleConstants.MAX_EXHIBITION_GAMES)
        require(ExhibitionScheduleUxPolicy.validateDraft(capped, drafts.take(capped))) {
            "Invalid exhibition draft"
        }
        context.preseasonScheduleStore.edit { prefs ->
            prefs[exhibitionKey(dynastyId, seasonIndex)] = capped
            if (capped == 0) {
                prefs.remove(exhibitionDetailsKey(dynastyId, seasonIndex))
            } else {
                prefs[exhibitionDetailsKey(dynastyId, seasonIndex)] = encodeExhibitionDrafts(drafts.take(capped))
            }
        }
    }

    suspend fun loadRegularSeasonBuilderContext(
        dynastyId: String,
        seasonIndex: Int,
    ): RegularSeasonScheduleBuilderUxPolicy.BuilderContext? {
        val coach = database.dynastyCoachDao().getByDynastyId(dynastyId) ?: return null
        val userTeam = packRepository.activeSession().index.team(coach.userTeamId) ?: return null
        val conference = packRepository.activeSession().index.conference(userTeam.conferenceId) ?: return null
        val inviteState = tournamentInvitesRepository.loadState(
            dynastyId,
            seasonIndex,
            liveProgramPrestige(dynastyId),
        )
        return RegularSeasonScheduleBuilderUxPolicy.BuilderContext(
            budget = inviteState.slotBudget,
            conferenceTemplateGames = conference.leagueGameCount,
            exhibitionCount = loadExhibitionCount(dynastyId, seasonIndex),
        )
    }

    suspend fun buildRegularSeasonSchedule(
        dynastyId: String,
        seasonIndex: Int,
    ): RegularSeasonScheduleSolveResult = when (val plan = solveRegularSeasonPlan(dynastyId, seasonIndex)) {
        null -> RegularSeasonScheduleSolveResult.MissingContext
        else -> {
            val budget = tournamentInvitesRepository.loadState(
                dynastyId,
                seasonIndex,
                liveProgramPrestige(dynastyId),
            ).slotBudget
            val validation = PreseasonScheduleValidator.validate(plan, budget)
            if (!validation.isValid) {
                RegularSeasonScheduleSolveResult.Invalid(validation)
            } else {
                persistPlan(dynastyId, seasonIndex, plan)
                leagueScheduleRepository.generateAndPersist(dynastyId, seasonIndex)
                RegularSeasonScheduleSolveResult.Saved(plan)
            }
        }
    }

    suspend fun solveAndSaveRegularSeason(dynastyId: String, seasonIndex: Int): PreseasonSchedulePlan? =
        when (val outcome = buildRegularSeasonSchedule(dynastyId, seasonIndex)) {
            is RegularSeasonScheduleSolveResult.Saved -> outcome.plan
            else -> null
        }

    private suspend fun solveRegularSeasonPlan(dynastyId: String, seasonIndex: Int): PreseasonSchedulePlan? {
        val coach = database.dynastyCoachDao().getByDynastyId(dynastyId) ?: return null
        val index = packRepository.activeSession().index
        val userTeam = index.team(coach.userTeamId) ?: return null
        val conference = index.conference(userTeam.conferenceId) ?: return null
        val confOpponents = index.allTeamDisplays()
            .filter { it.conferenceId == userTeam.conferenceId && it.teamId != userTeam.teamId }
        val nonConf = index.allTeamDisplays()
            .filter { it.conferenceId != userTeam.conferenceId }
        val prestige = liveProgramPrestige(dynastyId)
        val inviteState = tournamentInvitesRepository.loadState(dynastyId, seasonIndex, prestige)
        val budget = inviteState.slotBudget
        val exhibitions = loadExhibitionCount(dynastyId, seasonIndex)
        val exhibitionStubs = ExhibitionScheduleUxPolicy.draftsToStubs(loadExhibitionDrafts(dynastyId, seasonIndex))
            .takeIf { it.size == exhibitions }
        val plan = PreseasonScheduleSolver.solve(
            userTeam = userTeam,
            conference = conference,
            conferenceOpponents = confOpponents,
            nonConferencePool = nonConf,
            budget = budget,
            tournamentResolution = inviteState.resolution,
            exhibitionCount = exhibitions,
            seed = dynastyId.hashCode().toLong() xor seasonIndex.toLong(),
            exhibitionStubs = exhibitionStubs,
        )
        return plan
    }

    private suspend fun persistPlan(dynastyId: String, seasonIndex: Int, plan: PreseasonSchedulePlan) {
        context.preseasonScheduleStore.edit { prefs ->
            prefs[planKey(dynastyId, seasonIndex)] = encodePlan(plan)
        }
    }

    private fun exhibitionKey(dynastyId: String, seasonIndex: Int) =
        intPreferencesKey("exhibitions_${dynastyId}_$seasonIndex")

    private fun exhibitionDetailsKey(dynastyId: String, seasonIndex: Int) =
        stringPreferencesKey("exhibition_details_${dynastyId}_$seasonIndex")

    private fun encodeExhibitionDrafts(drafts: List<ExhibitionGameDraft>): String =
        drafts.joinToString("\n") { draft ->
            listOf(
                draft.opponentTeamId.escape(),
                draft.opponentDisplayName.escape(),
                if (draft.homeGame) "H" else "A",
                draft.exhibitionWeekIndex.toString(),
            ).joinToString("|")
        }

    private fun decodeExhibitionDrafts(raw: String?): List<ExhibitionGameDraft> {
        if (raw.isNullOrBlank()) return emptyList()
        return raw.lineSequence().mapNotNull { line ->
            val parts = line.split("|")
            if (parts.size < 4) return@mapNotNull null
            val week = parts[3].toIntOrNull() ?: return@mapNotNull null
            ExhibitionGameDraft(
                opponentTeamId = parts[0].unescape(),
                opponentDisplayName = parts[1].unescape(),
                homeGame = parts[2] == "H",
                exhibitionWeekIndex = week,
            )
        }.toList()
    }

    private fun planKey(dynastyId: String, seasonIndex: Int) =
        stringPreferencesKey("plan_${dynastyId}_$seasonIndex")

    private fun encodePlan(plan: PreseasonSchedulePlan): String {
        val header = "${plan.conferenceGameCount}|${plan.nonConferenceGameCount}|${plan.earlyEventGameCount}"
        val lines = (plan.exhibitions + plan.regularSeason).joinToString("\n") { encodeGame(it) }
        return "$header\n$lines"
    }

    private fun encodeGame(game: ScheduledGameStub): String =
        listOf(
            game.kind.name,
            game.opponentTeamId,
            game.opponentDisplayName.escape(),
            if (game.homeGame) "H" else "A",
            game.rsWeekIndex?.toString().orEmpty(),
        ).joinToString("|")

    private fun decodePlan(raw: String): PreseasonSchedulePlan? {
        val split = raw.split("\n", limit = 2)
        if (split.size != 2) return null
        val header = split[0].split("|")
        if (header.size < 3) return null
        val games = split[1].lineSequence().mapNotNull { decodeGame(it) }.toList()
        val exhibitions = games.filter { it.kind == ScheduleGameKind.Exhibition }
        val regular = games.filter { it.kind != ScheduleGameKind.Exhibition }
        return PreseasonSchedulePlan(
            exhibitions = exhibitions,
            regularSeason = regular,
            conferenceGameCount = header[0].toIntOrNull() ?: 0,
            nonConferenceGameCount = header[1].toIntOrNull() ?: 0,
            earlyEventGameCount = header[2].toIntOrNull() ?: 0,
        )
    }

    private fun decodeGame(line: String): ScheduledGameStub? {
        val p = line.split("|")
        if (p.size < 5) return null
        val kind = runCatching { ScheduleGameKind.valueOf(p[0]) }.getOrNull() ?: return null
        return ScheduledGameStub(
            kind = kind,
            opponentTeamId = p[1],
            opponentDisplayName = p[2].unescape(),
            homeGame = p[3] == "H",
            rsWeekIndex = p[4].toIntOrNull(),
        )
    }

    private fun String.escape(): String = replace("|", "%7C")
    private fun String.unescape(): String = replace("%7C", "|")
}
