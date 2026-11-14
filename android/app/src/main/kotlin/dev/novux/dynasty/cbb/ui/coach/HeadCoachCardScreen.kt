package dev.novux.dynasty.cbb.ui.coach

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.novux.dynasty.cbb.R
import dev.novux.dynasty.cbb.core.coach.achievements.HeadCoachAchievements
import dev.novux.dynasty.cbb.core.coach.card.HeadCoachCard
import dev.novux.dynasty.cbb.core.coach.card.HeadCoachRatings
import dev.novux.dynasty.cbb.core.coach.contract.HeadCoachContractEngine
import dev.novux.dynasty.cbb.core.coach.contract.HeadCoachExtensionOffer
import dev.novux.dynasty.cbb.core.coach.employment.CoachEmploymentStatus
import dev.novux.dynasty.cbb.core.coach.security.CoachSecuritySnapshot
import dev.novux.dynasty.cbb.core.coach.security.HeadCoachCarouselContractPolicy
import dev.novux.dynasty.cbb.core.coach.skill.CoachSkillBranch
import dev.novux.dynasty.cbb.core.coach.skill.CoachSkillPointState
import dev.novux.dynasty.cbb.core.coach.card.deep.CoachCardDeepTabsSnapshot
import dev.novux.dynasty.cbb.core.coach.skill.CoachSkillTreeEngine
import dev.novux.dynasty.cbb.core.school.SchoolHubOverview
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HeadCoachCardScreen(
    card: HeadCoachCard,
    userCoachSecurity: CoachSecuritySnapshot? = null,
    skillPointBank: Int? = null,
    skillState: CoachSkillPointState? = null,
    displayRatings: HeadCoachRatings = card.ratings,
    programPrestige: Int = 50,
    seasonIndex: Int = 1,
    onSpendSkill: (CoachSkillBranch) -> Unit = {},
    onAutoSpendChanged: (Boolean) -> Unit = {},
    onOpenStaffTab: () -> Unit = {},
    employmentStatus: CoachEmploymentStatus = CoachEmploymentStatus.Employed,
    onVoluntaryResign: () -> Unit = {},
    onOpenJobCenter: () -> Unit = {},
    schoolHubOverview: SchoolHubOverview? = null,
    onOpenSchoolHub: (() -> Unit)? = null,
    coachAchievements: HeadCoachAchievements? = null,
    coachDeepTabs: CoachCardDeepTabsSnapshot? = null,
    extensionOffer: HeadCoachExtensionOffer? = null,
    onAcceptExtension: () -> Unit = {},
    onDeclineExtension: () -> Unit = {},
    selectedTab: CoachCardTab = CoachCardTab.Overview,
    onSelectedTabChange: (CoachCardTab) -> Unit = {},
    isUserCoach: Boolean = true,
    onBack: () -> Unit = {},
    allowEdit: Boolean = false,
    onEdit: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val tabs = CoachCardTab.HEAD_COACH_ORDER
    val contractDetail = remember(card, userCoachSecurity, programPrestige) {
        HeadCoachContractEngine.derive(card, userCoachSecurity, programPrestige)
    }
    var showResignConfirm by remember { mutableStateOf(false) }
    var showTerminationConfirm by remember { mutableStateOf(false) }
    var showEditHint by remember { mutableStateOf(false) }
    val money = remember { NumberFormat.getCurrencyInstance(Locale.US) }
    val securityForUi = userCoachSecurity?.takeIf { isUserCoach }

    Column(modifier = modifier.fillMaxSize()) {
        CoachCardPersistentHeader(
            displayName = card.displayName,
            allowEdit = allowEdit,
            onBack = onBack,
            onEdit = { showEditHint = true },
        )
        CoachCardIdentityStrip(
            schoolDisplayName = card.schoolDisplayName,
            roleLabel = stringResource(R.string.coach_card_role_head),
            seasonRecordLabel = card.seasonRecordLabel,
        )
        CoachCardTabRow(
            tabs = tabs,
            selectedTab = selectedTab,
            onTabSelected = onSelectedTabChange,
        )
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            when (selectedTab) {
                CoachCardTab.Overview -> HeadCoachOverviewTab(
                    card = card,
                    userCoachSecurity = securityForUi,
                    skillPointBank = skillPointBank,
                    skillState = skillState?.takeIf { isUserCoach },
                    displayRatings = displayRatings,
                    programPrestige = programPrestige,
                    schoolHubOverview = schoolHubOverview,
                    onOpenSchoolHub = onOpenSchoolHub,
                    coachAchievements = coachAchievements,
                    onSpendSkill = onSpendSkill,
                    onAutoSpendChanged = onAutoSpendChanged,
                    onOpenContractTab = { onSelectedTabChange(CoachCardTab.Contract) },
                    isUserCoach = isUserCoach,
                )
                CoachCardTab.Contract -> HeadCoachContractTab(
                    card = card,
                    contractDetail = contractDetail,
                    userCoachSecurity = securityForUi,
                    employmentStatus = employmentStatus,
                    isUserCoach = isUserCoach,
                    onViewAdTrustOnStaff = onOpenStaffTab,
                    onResignTap = { showResignConfirm = true },
                    onTerminationTap = { showTerminationConfirm = true },
                    onOpenJobCenter = onOpenJobCenter,
                    extensionOffer = extensionOffer.takeIf { isUserCoach },
                    onAcceptExtension = onAcceptExtension,
                    onDeclineExtension = onDeclineExtension,
                )
                CoachCardTab.History -> CoachCardHistoryDeepTab(coachDeepTabs)
                CoachCardTab.HallOfFame -> CoachCardHofDeepTab(coachDeepTabs)
                CoachCardTab.Draft -> CoachCardDraftDeepTab(coachDeepTabs)
            }
        }
    }

    if (showResignConfirm) {
        AlertDialog(
            onDismissRequest = { showResignConfirm = false },
            title = { Text(stringResource(R.string.coach_contract_resign_title)) },
            text = {
                Text(
                    stringResource(
                        R.string.coach_contract_resign_body_buyout,
                        money.format(card.buyoutUsd),
                    ),
                )
            },
            dismissButton = {
                TextButton(onClick = { showResignConfirm = false }) {
                    Text(stringResource(android.R.string.cancel))
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showResignConfirm = false
                        onVoluntaryResign()
                    },
                ) {
                    Text(stringResource(R.string.coach_contract_resign_confirm))
                }
            },
        )
    }

    if (showTerminationConfirm) {
        AlertDialog(
            onDismissRequest = { showTerminationConfirm = false },
            title = { Text(stringResource(R.string.coach_contract_termination_title)) },
            text = {
                Text(
                    stringResource(
                        R.string.coach_contract_termination_body,
                        money.format(card.buyoutUsd),
                    ),
                )
            },
            dismissButton = {
                TextButton(onClick = { showTerminationConfirm = false }) {
                    Text(stringResource(android.R.string.cancel))
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showTerminationConfirm = false
                        onVoluntaryResign()
                    },
                ) {
                    Text(stringResource(R.string.coach_contract_termination_confirm))
                }
            },
        )
    }

    if (showEditHint) {
        AlertDialog(
            onDismissRequest = { showEditHint = false },
            title = { Text(stringResource(R.string.coach_card_edit)) },
            text = { Text(stringResource(R.string.coach_card_edit_hint)) },
            confirmButton = {
                TextButton(onClick = {
                    showEditHint = false
                    onEdit()
                }) {
                    Text(stringResource(R.string.coach_card_edit_ack))
                }
            },
        )
    }
}

@Composable
private fun HeadCoachOverviewTab(
    card: HeadCoachCard,
    userCoachSecurity: CoachSecuritySnapshot?,
    skillPointBank: Int?,
    skillState: CoachSkillPointState?,
    displayRatings: HeadCoachRatings,
    programPrestige: Int,
    schoolHubOverview: SchoolHubOverview?,
    onOpenSchoolHub: (() -> Unit)?,
    coachAchievements: HeadCoachAchievements?,
    onSpendSkill: (CoachSkillBranch) -> Unit,
    onAutoSpendChanged: (Boolean) -> Unit,
    onOpenContractTab: () -> Unit,
    isUserCoach: Boolean,
) {
    CoachOverviewTeamCard(
        schoolDisplayName = card.schoolDisplayName,
        seasonRecordLabel = card.seasonRecordLabel,
        schoolHubOverview = schoolHubOverview,
        onOpenSchoolHub = onOpenSchoolHub,
    )
    CoachOverviewContractCard(card = card, programPrestige = programPrestige)
    val overviewAchievements = coachAchievements
        ?: remember(card.seasonRecordLabel) {
            HeadCoachAchievements.fromSeasonRecordLabel(card.seasonRecordLabel)
        }
    CoachOverviewAchievementsCard(achievements = overviewAchievements)
    userCoachSecurity?.let { security ->
        @Suppress("unused")
        val qaContract = HeadCoachCarouselContractPolicy.MEGA_ID
        JobSecurityMeter(
            snapshot = security,
            showFactorDetails = HeadCoachCarouselContractPolicy
                .showJobSecurityFactorDetailsForUserCoach(isUserCoach),
            onNavigateToContract = onOpenContractTab,
        )
    }
    skillPointBank?.takeIf { it > 0 }?.let { bank ->
        Text(
            stringResource(R.string.coach_skill_point_bank, bank),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary,
        )
    }
    RatingCard(
        title = stringResource(R.string.coach_card_ratings_headline),
        lines = listOf(
            stringResource(R.string.coach_rating_offensive, displayRatings.offensive),
            stringResource(R.string.coach_rating_defensive, displayRatings.defensive),
            stringResource(R.string.coach_rating_training, displayRatings.training),
            stringResource(R.string.coach_rating_recruiting, displayRatings.recruiting),
        ),
    )
    if (isUserCoach) {
        RatingCard(
            title = stringResource(R.string.coach_card_strategy),
            lines = listOf(
                stringResource(R.string.coach_scheme_offense, card.offensiveSchemeLabel),
                stringResource(R.string.coach_scheme_defense, card.defensiveSchemeLabel),
            ),
        )
    }
    skillState?.let { skills ->
        SkillTreePanel(
            state = skills,
            bank = skillPointBank ?: skills.unspentBank,
            onSpendSkill = onSpendSkill,
            onAutoSpendChanged = onAutoSpendChanged,
        )
    }
    card.almaMaterLabel?.let {
        Text(stringResource(R.string.coach_alma_mater, it), style = MaterialTheme.typography.bodyMedium)
    }
    Text(
        stringResource(R.string.coach_card_age, card.age),
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

@Composable
private fun SkillTreePanel(
    state: CoachSkillPointState,
    bank: Int,
    onSpendSkill: (CoachSkillBranch) -> Unit,
    onAutoSpendChanged: (Boolean) -> Unit,
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                stringResource(R.string.coach_skill_tree_title),
                fontWeight = FontWeight.SemiBold,
            )
            CoachSkillBranch.entries.forEach { branch ->
                val level = CoachSkillTreeEngine.level(state, branch)
                val canSpend = bank >= CoachSkillTreeEngine.POINTS_PER_LEVEL &&
                    level < CoachSkillTreeEngine.MAX_LEVEL
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        stringResource(
                            R.string.coach_skill_branch_level,
                            branch.label,
                            level,
                            CoachSkillTreeEngine.MAX_LEVEL,
                        ),
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    OutlinedButton(onClick = { onSpendSkill(branch) }, enabled = canSpend) {
                        Text(stringResource(R.string.coach_skill_spend_point))
                    }
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(stringResource(R.string.coach_skill_auto_spend), style = MaterialTheme.typography.bodyMedium)
                Switch(checked = state.autoSpendEnabled, onCheckedChange = onAutoSpendChanged)
            }
        }
    }
}

@Composable
private fun RatingCard(title: String, lines: List<String>) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(title, fontWeight = FontWeight.SemiBold)
            lines.forEach { Text(it, style = MaterialTheme.typography.bodyMedium) }
        }
    }
}
