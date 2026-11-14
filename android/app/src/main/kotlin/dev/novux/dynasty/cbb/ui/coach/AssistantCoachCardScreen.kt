package dev.novux.dynasty.cbb.ui.coach

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.novux.dynasty.cbb.R
import dev.novux.dynasty.cbb.core.coach.card.AssistantCoachCard
import dev.novux.dynasty.cbb.core.coach.card.deep.CoachCardDeepTabsSnapshot
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AssistantCoachCardScreen(
    card: AssistantCoachCard,
    seasonIndex: Int = 1,
    coachDeepTabs: CoachCardDeepTabsSnapshot? = null,
    selectedTab: CoachCardTab = CoachCardTab.Overview,
    onSelectedTabChange: (CoachCardTab) -> Unit = {},
    onBack: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val tabs = remember(card.hasHeadCoachCareer) {
        CoachCardTab.assistantTabs(card.hasHeadCoachCareer)
    }
  val money = remember { NumberFormat.getCurrencyInstance(Locale.US) }

    Column(modifier = modifier.fillMaxSize()) {
        CoachCardPersistentHeader(
            displayName = card.displayName,
            allowEdit = false,
            onBack = onBack,
            onEdit = {},
        )
        CoachCardIdentityStrip(
            schoolDisplayName = card.schoolDisplayName,
            roleLabel = card.role.label,
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
                CoachCardTab.Overview -> AssistantOverviewTab(card, money)
                CoachCardTab.History -> if (card.hasHeadCoachCareer) {
                    CoachCardHistoryDeepTab(coachDeepTabs)
                } else {
                    AssistantHistoryTab(card, seasonIndex)
                }
                CoachCardTab.Contract -> AssistantCoachContractTab(card)
                CoachCardTab.HallOfFame -> if (card.hasHeadCoachCareer) {
                    CoachCardHofDeepTab(coachDeepTabs)
                } else {
                    CoachCardPlaceholderTab(
                        title = stringResource(R.string.coach_tab_hof),
                        body = stringResource(R.string.coach_assistant_hof_body),
                    )
                }
                CoachCardTab.Draft -> if (card.hasHeadCoachCareer) {
                    CoachCardDraftDeepTab(coachDeepTabs)
                } else {
                    CoachCardPlaceholderTab(
                        title = stringResource(R.string.coach_tab_draft),
                        body = stringResource(R.string.coach_assistant_draft_body),
                    )
                }
            }
        }
    }
}

@Composable
private fun AssistantOverviewTab(
    card: AssistantCoachCard,
    money: NumberFormat,
) {
    Text(
        stringResource(R.string.coach_card_ratings_assistant),
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold,
    )
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(stringResource(R.string.coach_rating_recruiting, card.ratings.recruiting))
            Text(stringResource(R.string.coach_rating_development, card.ratings.development))
            Text(stringResource(R.string.coach_rating_scouting, card.ratings.scouting))
        }
    }
    Text(
        stringResource(
            R.string.coach_assistant_tenure,
            card.yearsInRole,
            card.yearsAtSchool,
        ),
        style = MaterialTheme.typography.bodyMedium,
    )
    Text(
        stringResource(R.string.coach_card_age, card.age),
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
    Text(
        stringResource(
            R.string.coach_contract_summary,
            card.contractLengthYears,
            card.contractYearsLeft,
        ),
        style = MaterialTheme.typography.bodyMedium,
    )
    if (card.annualSalaryUsd > 0) {
        Text(
            stringResource(R.string.coach_contract_salary, money.format(card.annualSalaryUsd)),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

@Composable
private fun AssistantHistoryTab(card: AssistantCoachCard, seasonIndex: Int) {
    Text(stringResource(R.string.coach_tab_history), style = MaterialTheme.typography.titleMedium)
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                stringResource(
                    R.string.coach_history_assistant_row,
                    seasonIndex,
                    card.schoolDisplayName,
                    card.role.label,
                    card.seasonRecordLabel,
                ),
                style = MaterialTheme.typography.bodyLarge,
            )
        }
    }
    Text(
        stringResource(R.string.coach_history_accumulates),
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

@Composable
internal fun CoachCardPlaceholderTab(title: String, body: String) {
    Text(title, style = MaterialTheme.typography.titleMedium)
    Text(body, style = MaterialTheme.typography.bodyMedium)
}
