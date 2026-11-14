package dev.novux.dynasty.cbb.data.league

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dev.novux.dynasty.cbb.core.brackets.NciHeritageBracketEngine
import dev.novux.dynasty.cbb.core.brackets.NcbaOpeningRoundEngine
import dev.novux.dynasty.cbb.core.brackets.PostseasonUserGameResolver
import dev.novux.dynasty.cbb.core.brackets.PostseasonWeekBatchEngine
import dev.novux.dynasty.cbb.core.calendar.CalendarPhase
import dev.novux.dynasty.cbb.core.calendar.CalendarPosition
import dev.novux.dynasty.cbb.core.calendar.checkpoint.ProvisionalGameMark
import dev.novux.dynasty.cbb.core.balance.selectionTuning
import dev.novux.dynasty.cbb.core.injuries.InjuryOccurrenceEngine
import dev.novux.dynasty.cbb.core.league.LeagueBatchSimParams
import dev.novux.dynasty.cbb.core.league.LeagueGameResult
import dev.novux.dynasty.cbb.core.league.LeagueStandingsState
import dev.novux.dynasty.cbb.core.league.LeagueWeekBatchSimEngine
import dev.novux.dynasty.cbb.core.league.LeagueWeekBatchWeeklyRankings
import dev.novux.dynasty.cbb.data.balance.BalanceRulesetRepository
import dev.novux.dynasty.cbb.data.pack.PackRepository
import dev.novux.dynasty.cbb.data.sim.SimTuningRepository
import dev.novux.dynasty.cbb.core.balance.injuryTuning
import dev.novux.dynasty.cbb.core.preseason.tournament.EarlySeasonBracketEngine
import dev.novux.dynasty.cbb.core.preseason.tournament.EarlySeasonBracketSeason
import dev.novux.dynasty.cbb.core.schedule.PreseasonSchedulePlan
import dev.novux.dynasty.cbb.core.schedule.ScheduleGameKind
import dev.novux.dynasty.cbb.data.preseason.EarlySeasonBracketRepository
import dev.novux.dynasty.cbb.data.news.NewsFeedRepository
import dev.novux.dynasty.cbb.data.preseason.PreseasonScheduleRepository
import dev.novux.dynasty.cbb.database.DynastyDatabase
import kotlinx.coroutines.flow.first
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

private val Context.leagueStandingsStore: DataStore<Preferences> by preferencesDataStore(
    name = "league_standings",
)

class LeagueWeekBatchRepository(
    private val context: Context,
    private val leagueScheduleRepository: LeagueScheduleRepository,
    private val preseasonScheduleRepository: PreseasonScheduleRepository,
    private val earlySeasonBracketRepository: EarlySeasonBracketRepository,
    private val newsFeedRepository: NewsFeedRepository,
    private val database: DynastyDatabase,
    private val packRepository: PackRepository,
    private val balanceRulesetRepository: BalanceRulesetRepository,
    private val simTuningRepository: SimTuningRepository,
) {
    suspend fun loadStandings(dynastyId: String, seasonIndex: Int): LeagueStandingsState? {
        val raw = context.leagueStandingsStore.data.first()[key(dynastyId, seasonIndex)] ?: return null
        return Json.decodeFromString(raw)
    }

    suspend fun afterWeekAdvance(
        dynastyId: String,
        seasonIndex: Int,
        committedWeek: CalendarPosition,
        userGamesCommitted: Map<String, ProvisionalGameMark>,
    ) {
        if (!LeagueWeekBatchSimEngine.phaseSupportsBatch(committedWeek.phase)) return
        val coach = database.dynastyCoachDao().getByDynastyId(dynastyId) ?: return
        val userTeamId = coach.userTeamId
        val seed = dynastyId.hashCode().toLong() + seasonIndex + committedWeek.weekIndex
        when (committedWeek.phase) {
            CalendarPhase.Postseason -> afterPostseasonWeekAdvance(
                dynastyId,
                seasonIndex,
                committedWeek.weekIndex,
                userTeamId,
                userGamesCommitted,
                seed,
            )
            CalendarPhase.RegularSeason -> {
                val schedule = leagueScheduleRepository.load(dynastyId, seasonIndex) ?: return
                var standings = loadStandings(dynastyId, seasonIndex)
                    ?: LeagueWeekBatchSimEngine.bootstrap(
                        seasonIndex,
                        schedule.schedules.map { it.teamId },
                    )
                val plan = preseasonScheduleRepository.loadPlan(dynastyId, seasonIndex)
                val bracketSeason = earlySeasonBracketRepository.load(dynastyId, seasonIndex)
                val userResult = buildUserResult(
                    userTeamId,
                    committedWeek.weekIndex,
                    plan,
                    userGamesCommitted,
                    bracketSeason,
                )
                if (bracketSeason != null && bracketSeason.brackets.isNotEmpty()) {
                    val eventOutcome = EarlySeasonBracketEngine.simWeekForLeague(
                        season = bracketSeason,
                        standings = standings,
                        rsWeekIndex = committedWeek.weekIndex,
                        userTeamId = userTeamId,
                        userResults = userResult?.let { listOf(it) }.orEmpty(),
                        seed = seed,
                    )
                    standings = eventOutcome.standings
                    earlySeasonBracketRepository.persist(dynastyId, eventOutcome.season)
                }
                val prestige = packRepository.activeSession().index.allTeamDisplays()
                    .associate { it.teamId to it.programPrestige }
                val ruleset = balanceRulesetRepository.activeRuleset()
                val injuryTuning = ruleset.injuryTuning()
                val batchParams = LeagueBatchSimParams(
                    tuning = simTuningRepository.activeTuning(),
                    programPrestigeByTeamId = prestige,
                    phase = committedWeek.phase,
                    injuryBank = InjuryOccurrenceEngine.bankFromBalance(ruleset.injuries.bank),
                    injuryOccurrence = injuryTuning.occurrence,
                )
                val batch = LeagueWeekBatchSimEngine.runRegularSeasonWeek(
                    schedule = schedule,
                    standings = standings,
                    rsWeekIndex = committedWeek.weekIndex,
                    userTeamId = userTeamId,
                    userResult = userResult,
                    seed = seed,
                    batch = batchParams,
                )
                val withNet = attachWeeklyNet(batch.standings, committedWeek.weekIndex)
                persist(dynastyId, withNet)
                val packIndex = packRepository.activeSession().index
                newsFeedRepository.appendLeagueWeek(
                    dynastyId = dynastyId,
                    standings = withNet,
                    userTeamId = userTeamId,
                    teamDisplayName = { id -> packIndex.team(id)?.displayName ?: id },
                )
            }
            else -> Unit
        }
    }

    private suspend fun afterPostseasonWeekAdvance(
        dynastyId: String,
        seasonIndex: Int,
        postseasonWeek: Int,
        userTeamId: String,
        userGamesCommitted: Map<String, ProvisionalGameMark>,
        seed: Long,
    ) {
        val schedule = leagueScheduleRepository.load(dynastyId, seasonIndex)
        var standings = loadStandings(dynastyId, seasonIndex)
            ?: schedule?.let {
                LeagueWeekBatchSimEngine.bootstrap(seasonIndex, it.schedules.map { s -> s.teamId })
            }
            ?: return
        if (standings.selectionSunday == null) return
        val selection = standings.selectionSunday!!
        standings = standings.copy(
            ncbaOpeningRound = standings.ncbaOpeningRound ?: NcbaOpeningRoundEngine.planFromSelection(selection),
            nciBracket = standings.nciBracket ?: NciHeritageBracketEngine.planNci(selection),
            heritageBracket = standings.heritageBracket ?: NciHeritageBracketEngine.planHeritage(selection),
        )
        val userResult = buildUserPostseasonResult(
            userTeamId,
            postseasonWeek,
            userGamesCommitted,
            standings,
        )
        val index = packRepository.activeSession().index
        val conferenceIdByTeam = index.allTeamIds().associateWith { id ->
            index.team(id)?.conferenceId ?: "unknown"
        }
        val next = PostseasonWeekBatchEngine.runPostseasonWeek(
            standings = standings,
            postseasonWeek = postseasonWeek,
            userTeamId = userTeamId,
            userResult = userResult,
            conferenceIdByTeam = conferenceIdByTeam,
            seed = seed,
        )
        persist(dynastyId, next)
    }

    private fun buildUserPostseasonResult(
        userTeamId: String,
        postseasonWeek: Int,
        committed: Map<String, ProvisionalGameMark>,
        standings: LeagueStandingsState,
    ): LeagueGameResult? {
        val mark = committed.values.firstOrNull() ?: return null
        val matchup = PostseasonUserGameResolver.findMatchup(standings, userTeamId, postseasonWeek) ?: return null
        val (homeTeamId, awayTeamId) = matchup
        val (homeScore, awayScore) = if (userTeamId == homeTeamId) {
            mark.homeScore to mark.awayScore
        } else {
            mark.awayScore to mark.homeScore
        }
        return LeagueGameResult(
            homeTeamId = homeTeamId,
            awayTeamId = awayTeamId,
            homeScore = homeScore,
            awayScore = awayScore,
            rsWeekIndex = postseasonWeek,
        )
    }

    private suspend fun attachWeeklyNet(
        standings: LeagueStandingsState,
        weekIndex: Int,
    ): LeagueStandingsState {
        if (standings.seasonResults.isEmpty()) return standings
        val prestige = packRepository.activeSession().index.allTeamDisplays()
            .associate { it.teamId to it.programPrestige }
        val tuning = balanceRulesetRepository.activeRuleset().selectionTuning()
        return LeagueWeekBatchWeeklyRankings.attach(
            standings = standings,
            programPrestige = prestige,
            weekIndex = weekIndex,
            tuning = tuning,
        )
    }

    private fun buildUserResult(
        userTeamId: String,
        rsWeekIndex: Int,
        plan: PreseasonSchedulePlan?,
        committed: Map<String, ProvisionalGameMark>,
        bracketSeason: EarlySeasonBracketSeason?,
    ): LeagueGameResult? {
        val mark = committed.values.firstOrNull() ?: return null
        val game = plan?.regularSeason?.firstOrNull { it.rsWeekIndex == rsWeekIndex } ?: return null
        if (game.kind == ScheduleGameKind.EarlyEvent) {
            val snapshot = bracketSeason?.brackets?.values?.firstOrNull { it.isParticipant(userTeamId) }
                ?: return null
            val matchup = EarlySeasonBracketEngine.pairingsForWeek(snapshot, rsWeekIndex)
                .firstOrNull { userTeamId == it.homeTeamId || userTeamId == it.awayTeamId }
                ?: return null
            val (homeScore, awayScore) = if (userTeamId == matchup.homeTeamId) {
                mark.homeScore to mark.awayScore
            } else {
                mark.awayScore to mark.homeScore
            }
            return LeagueGameResult(
                homeTeamId = matchup.homeTeamId,
                awayTeamId = matchup.awayTeamId,
                homeScore = homeScore,
                awayScore = awayScore,
                rsWeekIndex = rsWeekIndex,
            )
        }
        val homeTeam = if (game.homeGame) userTeamId else game.opponentTeamId
        val awayTeam = if (game.homeGame) game.opponentTeamId else userTeamId
        return LeagueGameResult(
            homeTeamId = homeTeam,
            awayTeamId = awayTeam,
            homeScore = mark.homeScore,
            awayScore = mark.awayScore,
            rsWeekIndex = rsWeekIndex,
        )
    }

    suspend fun persistStandings(dynastyId: String, state: LeagueStandingsState) {
        persist(dynastyId, state)
    }

    private suspend fun persist(dynastyId: String, state: LeagueStandingsState) {
        context.leagueStandingsStore.edit {
            it[key(dynastyId, state.seasonIndex)] = Json.encodeToString(state)
        }
    }

    private fun key(dynastyId: String, seasonIndex: Int) =
        stringPreferencesKey("standings_${dynastyId}_$seasonIndex")
}
