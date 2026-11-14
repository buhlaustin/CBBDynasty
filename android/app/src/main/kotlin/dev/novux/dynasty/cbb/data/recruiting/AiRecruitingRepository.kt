package dev.novux.dynasty.cbb.data.recruiting

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dev.novux.dynasty.cbb.core.balance.BalanceNilAnnualPoolParser
import dev.novux.dynasty.cbb.core.balance.BalanceRecruitingActions
import dev.novux.dynasty.cbb.core.balance.BalanceRecruitingActionsParser
import dev.novux.dynasty.cbb.core.balance.BalanceRecruitingEconomy
import dev.novux.dynasty.cbb.core.balance.BalanceRecruitingParser
import dev.novux.dynasty.cbb.core.balance.BalanceRecruitingRecommendedParser
import dev.novux.dynasty.cbb.core.calendar.CalendarPhase
import dev.novux.dynasty.cbb.core.nil.NilAnnualPoolEngine
import dev.novux.dynasty.cbb.core.recruiting.RecruitingCycleLedger
import dev.novux.dynasty.cbb.core.recruiting.RecruitingCyclePolicy
import dev.novux.dynasty.cbb.core.recruiting.RecruitingPipelineStates
import dev.novux.dynasty.cbb.core.recruiting.SchoolRecruitingContext
import dev.novux.dynasty.cbb.core.recruiting.ai.AiLeagueProgramGenerator
import dev.novux.dynasty.cbb.core.recruiting.ai.AiProgramSnapshot
import dev.novux.dynasty.cbb.core.recruiting.ai.AiRecruitingWeekResult
import dev.novux.dynasty.cbb.core.recruiting.ai.AiRecruitingWeekRunner
import dev.novux.dynasty.cbb.core.save.DynastySchema
import dev.novux.dynasty.cbb.core.save.LoadedDynasty
import dev.novux.dynasty.cbb.data.pack.PackRepository
import dev.novux.dynasty.cbb.data.preseason.IncomingClassSeedRepository
import dev.novux.dynasty.cbb.database.DynastyDatabase
import kotlinx.coroutines.flow.first

private val Context.aiRecruitingStore: DataStore<Preferences> by preferencesDataStore(
    name = "ai_recruiting",
)

class AiRecruitingRepository(
    private val context: Context,
    private val nilAnnualPoolRepository: dev.novux.dynasty.cbb.data.nil.NilAnnualPoolRepository,
    private val dynastyAutomationRepository: dev.novux.dynasty.cbb.data.settings.DynastyAutomationRepository,
    private val incomingClassSeedRepository: IncomingClassSeedRepository,
    private val recruitingEconomyRepository: RecruitingEconomyRepository,
    private val packRepository: PackRepository,
    private val database: DynastyDatabase,
) {
    suspend fun runLeagueWeekIfActive(
        dynasty: LoadedDynasty,
        ledger: RecruitingCycleLedger,
        userManualPointsSpent: Int = 0,
    ): AiRecruitingWeekResult? {
        val position = dynasty.weekCheckpoint.activePosition
        if (position.phase == CalendarPhase.SeasonReview) return null
        val economy = economyFrom(dynasty)
        val recommended = BalanceRecruitingRecommendedParser.parse(dynasty.balanceRuleset.recruiting)
        val pool = incomingClassSeedRepository.ensurePool(dynasty.dynastyId, dynasty.metadata.seasonIndex)
        val userProgramId = userProgramId(dynasty.dynastyId)
        val userSchool = resolveUserSchool(dynasty) ?: return null
        val offseasonWeek = position.weekIndex.takeIf { position.phase == CalendarPhase.Offseason }
        val hubOpen = RecruitingCyclePolicy.hubAccessible(position.phase, offseasonWeek)
        val automation = dynastyAutomationRepository.load(dynasty.dynastyId)
        val actionCosts = actionsFrom(dynasty)
        val result = AiRecruitingWeekRunner.runWeek(
            prospectPool = pool,
            economy = economy,
            recommended = recommended,
            userProgramId = userProgramId,
            userSchool = userSchool,
            userPointsAvailable = ledger.pointsAvailable,
            userPointsSpentByPlayer = userManualPointsSpent,
            autoMaintainUserSchool = hubOpen && automation.recruitingAutoMaintain,
            leaguePrograms = leagueProgramsFromPack(dynasty),
            actionCosts = actionCosts,
        )
        persistSummary(dynasty.dynastyId, result)
        return result
    }

    suspend fun loadLastSummaryLine(dynastyId: String): String? {
        val prefs = context.aiRecruitingStore.data.first()
        return prefs[prefKey(dynastyId)]
    }

    private suspend fun persistSummary(dynastyId: String, result: AiRecruitingWeekResult) {
        val line = "league=${result.leagueProgramsProcessed};userSpent=${result.userPointsSpent};" +
            "auto=${result.userAutoMaintained}"
        context.aiRecruitingStore.edit { prefs ->
            prefs[prefKey(dynastyId)] = line
        }
    }

    private fun economyFrom(dynasty: LoadedDynasty): BalanceRecruitingEconomy =
        BalanceRecruitingParser.parse(dynasty.balanceRuleset.recruiting)

    private fun actionsFrom(dynasty: LoadedDynasty): BalanceRecruitingActions =
        BalanceRecruitingActionsParser.parse(dynasty.balanceRuleset.recruiting)

    private fun userProgramId(dynastyId: String) = "user-$dynastyId"

    private suspend fun resolveUserSchool(dynasty: LoadedDynasty): SchoolRecruitingContext? {
        nilAnnualPoolRepository.userSchoolRecruitingContext(dynasty)?.let { return it }
        val coach = database.dynastyCoachDao().getByDynastyId(dynasty.dynastyId) ?: return null
        val packId = dynasty.summary.packId ?: DynastySchema.DEFAULT_PACK_ID
        val team = packRepository.restorePack(packId).index.team(coach.userTeamId) ?: return null
        val nilTables = BalanceNilAnnualPoolParser.parse(dynasty.balanceRuleset.nil)
        val nilUsd = NilAnnualPoolEngine.resolve(team.conferenceId, team.programPrestige, nilTables)
        return SchoolRecruitingContext(
            programPrestige = team.programPrestige,
            schoolHomeState = team.homeState,
            pipelineStates = RecruitingPipelineStates.forHomeState(team.homeState),
            nilPoolAnnualUsd = nilUsd.toInt(),
        )
    }

    private suspend fun leagueProgramsFromPack(dynasty: LoadedDynasty): List<AiProgramSnapshot> {
        val coach = database.dynastyCoachDao().getByDynastyId(dynasty.dynastyId)
        val packId = dynasty.summary.packId ?: DynastySchema.DEFAULT_PACK_ID
        val session = packRepository.restorePack(packId)
        val nilTables = BalanceNilAnnualPoolParser.parse(dynasty.balanceRuleset.nil)
        return AiLeagueProgramGenerator.fromPackTeams(
            teams = session.index.allTeamDisplays(),
            excludeTeamId = coach?.userTeamId,
        ) { team ->
            NilAnnualPoolEngine.resolve(team.conferenceId, team.programPrestige, nilTables)
        }
    }

    private fun prefKey(dynastyId: String) = stringPreferencesKey("ai_week_$dynastyId")
}
