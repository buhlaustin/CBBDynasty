package dev.novux.dynasty.cbb.ui.newgame

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.unit.dp
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import dev.novux.dynasty.cbb.core.save.NewGameDraft
import dev.novux.dynasty.cbb.core.save.NewGameSignValidator
import dev.novux.dynasty.cbb.data.save.ActiveDynastySession
import kotlinx.coroutines.launch
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import dev.novux.dynasty.cbb.R
import dev.novux.dynasty.cbb.core.coach.CoachCreateValidator
import dev.novux.dynasty.cbb.core.pack.ConferenceDisplayOverlay
import dev.novux.dynasty.cbb.core.pack.TeamDisplayOverlay
import dev.novux.dynasty.cbb.core.balance.BalanceNilAnnualPoolParser
import dev.novux.dynasty.cbb.core.balance.BalanceNilAnnualPoolTables
import dev.novux.dynasty.cbb.core.balance.BalanceRecruitingEconomy
import dev.novux.dynasty.cbb.core.balance.BalanceRecruitingParser
import dev.novux.dynasty.cbb.core.save.NewGameSchoolSummaryPreviewEngine
import dev.novux.dynasty.cbb.data.balance.BalanceRulesetRepository
import dev.novux.dynasty.cbb.data.pack.PackRepository
import dev.novux.dynasty.cbb.data.save.SaveSlotRepository

/** Multi-step **New** flow — [planning/game-start.md#new-game-wizard-overview]. */
@Composable
fun NewGameWizardScreen(
    saveSlotRepository: SaveSlotRepository,
    packRepository: PackRepository,
    balanceRulesetRepository: BalanceRulesetRepository,
    onExitWizard: () -> Unit,
    onNoEmptySlot: () -> Unit,
    onDynastyCreated: () -> Unit,
) {
    val navController = rememberNavController()
    val scope = rememberCoroutineScope()
    var slotGatePassed by remember { mutableStateOf(false) }
    var draft by remember { mutableStateOf(NewGameDraft()) }
    var coachShowErrors by remember { mutableStateOf(false) }
    var schoolShowErrors by remember { mutableStateOf(false) }
    var schools by remember { mutableStateOf<List<TeamDisplayOverlay>>(emptyList()) }
    var conferences by remember { mutableStateOf<List<ConferenceDisplayOverlay>>(emptyList()) }
    var packId by remember { mutableStateOf(dev.novux.dynasty.cbb.core.save.DynastySchema.DEFAULT_PACK_ID) }
    var signError by remember { mutableStateOf<SignContractError?>(null) }
    var signing by remember { mutableStateOf(false) }
    var wizardRecruitingEconomy by remember { mutableStateOf<BalanceRecruitingEconomy?>(null) }
    var wizardNilTables by remember { mutableStateOf<BalanceNilAnnualPoolTables?>(null) }
    var activePackLabel by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        val shipped = balanceRulesetRepository.ensureShippedDefaultLoaded()
        wizardRecruitingEconomy = BalanceRecruitingParser.parse(shipped.recruiting)
        wizardNilTables = BalanceNilAnnualPoolParser.parse(shipped.nil)
        saveSlotRepository.ensureInitialized()
        val hasSlot = saveSlotRepository.firstEmptySlotIndex() != null
        if (!hasSlot) {
            onNoEmptySlot()
            onExitWizard()
        } else {
            slotGatePassed = true
        }
    }

    LaunchedEffect(slotGatePassed) {
        if (!slotGatePassed) return@LaunchedEffect
        val session = packRepository.ensureEmbeddedLoaded()
        schools = session.index.allTeamDisplays()
        conferences = session.index.allConferenceDisplays()
        packId = session.packId
        activePackLabel = session.leagueDisplayName
    }

    if (!slotGatePassed) {
        return
    }

    NavHost(
        navController = navController,
        startDestination = NewGameStep.routeFor(NewGameStep.Coach),
        modifier = Modifier.fillMaxSize(),
    ) {
        NewGameStep.entries.forEach { step ->
            composable(NewGameStep.routeFor(step)) {
                val isLast = step == NewGameStep.SignContract
                val isCoach = step == NewGameStep.Coach
                val isSchool = step == NewGameStep.School
                NewGameStepScaffold(
                    step = step,
                    onBack = {
                        when (step.previous) {
                            null -> onExitWizard()
                            else -> navController.popBackStack()
                        }
                    },
                    onNext = if (isLast) {
                        null
                    } else {
                        {
                            when {
                                isCoach && CoachCreateValidator.validate(draft.coach) != null ->
                                    coachShowErrors = true
                                isSchool && (
                                    draft.selectedTeamId == null ||
                                        !draft.schoolSummaryAcknowledged
                                    ) ->
                                    schoolShowErrors = true
                                else ->
                                    step.next?.let { next ->
                                        navController.navigate(NewGameStep.routeFor(next))
                                    }
                            }
                        }
                    },
                    nextLabel = stringResource(
                        if (isLast) R.string.new_step_sign_title else R.string.new_wizard_next,
                    ),
                ) {
                    when (step) {
                        NewGameStep.Coach -> CoachInfoStepContent(
                            profile = draft.coach,
                            schools = schools,
                            onProfileChange = { draft = draft.copy(coach = it) },
                            showValidationErrors = coachShowErrors,
                        )
                        NewGameStep.Archetype -> ArchetypeStepContent(
                            selected = draft.archetype,
                            onSelected = { draft = draft.copy(archetype = it) },
                        )
                        NewGameStep.Schemes -> SchemesStepContent(
                            offensive = draft.offensiveScheme,
                            defensive = draft.defensiveScheme,
                            onOffensiveChange = { draft = draft.copy(offensiveScheme = it) },
                            onDefensiveChange = { draft = draft.copy(defensiveScheme = it) },
                        )
                        NewGameStep.Settings -> SettingsStepContent(
                            settings = draft.dynastySettings,
                            activePackLabel = activePackLabel,
                            onChange = { draft = draft.copy(dynastySettings = it) },
                        )
                        NewGameStep.School -> {
                            Column {
                                if (schoolShowErrors) {
                                    Text(
                                        text = stringResource(R.string.new_school_err_pick),
                                        color = MaterialTheme.colorScheme.error,
                                        style = MaterialTheme.typography.bodySmall,
                                        modifier = Modifier.padding(bottom = 8.dp),
                                    )
                                }
                                val economy = wizardRecruitingEconomy
                                val nilTables = wizardNilTables
                                if (economy != null && nilTables != null) {
                                    SchoolStepContent(
                                        schools = schools,
                                        conferences = conferences,
                                        unlockAll = draft.unlockAllSchools,
                                        onUnlockAllChange = {
                                            draft = draft.copy(unlockAllSchools = it)
                                        },
                                        selectedTeamId = draft.selectedTeamId,
                                        onSelectTeam = { draft = draft.copy(selectedTeamId = it) },
                                        summaryAcknowledged = draft.schoolSummaryAcknowledged,
                                        onSummaryAcknowledged = {
                                            draft = draft.copy(schoolSummaryAcknowledged = true)
                                        },
                                        recruitingEconomy = economy,
                                        nilPoolTables = nilTables,
                                        modifier = Modifier.fillMaxSize(),
                                    )
                                }
                            }
                        }
                        NewGameStep.SignContract -> {
                            val schoolName = schools.firstOrNull {
                                it.teamId == draft.selectedTeamId
                            }?.displayName.orEmpty()
                            val contractPreview = wizardRecruitingEconomy?.let { economy ->
                                wizardNilTables?.let { nilTables ->
                                    schools.firstOrNull { it.teamId == draft.selectedTeamId }?.let { team ->
                                        NewGameSchoolSummaryPreviewEngine.build(team, economy, nilTables)
                                    }
                                }
                            }
                            SignContractStepContent(
                                draft = draft,
                                schoolDisplayName = schoolName,
                                contractNilPreview = contractPreview,
                                signError = signError,
                                signingInProgress = signing,
                                onSign = {
                                    scope.launch {
                                        signing = true
                                        signError = null
                                        if (NewGameSignValidator.validate(draft) != null) {
                                            signError = SignContractError.IncompleteWizard
                                            signing = false
                                            return@launch
                                        }
                                        val loaded = saveSlotRepository.signNewGameFromWizard(
                                            draft = draft,
                                            schoolDisplayName = schoolName,
                                            packId = packId,
                                        )
                                        signing = false
                                        if (loaded == null) {
                                            signError = SignContractError.SlotUnavailable
                                        } else {
                                            ActiveDynastySession.set(loaded)
                                            onDynastyCreated()
                                        }
                                    }
                                },
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun NewGameStepPlaceholderBody(step: NewGameStep) {
    Text(
        text = stringResource(R.string.new_step_placeholder),
        style = MaterialTheme.typography.bodyLarge,
    )
}
