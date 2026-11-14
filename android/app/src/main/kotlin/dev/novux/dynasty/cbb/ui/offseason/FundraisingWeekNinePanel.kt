package dev.novux.dynasty.cbb.ui.offseason

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.novux.dynasty.cbb.R
import dev.novux.dynasty.cbb.core.offseason.weeknine.FacilityCategory
import dev.novux.dynasty.cbb.core.offseason.weeknine.FacilityUpgradePolicy
import dev.novux.dynasty.cbb.core.offseason.weeknine.FundraisingEventType
import dev.novux.dynasty.cbb.core.offseason.weeknine.ProgramEconomyState
import dev.novux.dynasty.cbb.data.offseason.OffseasonWeekNineRepository
import dev.novux.dynasty.cbb.data.recruiting.RecruitingEconomyRepository
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale

@Composable
fun FundraisingWeekNinePanel(
    dynastyId: String,
    seasonIndex: Int,
    weekNineRepository: OffseasonWeekNineRepository,
    programPrestige: Int = RecruitingEconomyRepository.DEFAULT_PROGRAM_PRESTIGE,
    modifier: Modifier = Modifier,
    economyState: ProgramEconomyState? = null,
    onEconomyLoaded: (ProgramEconomyState) -> Unit = {},
    onOpenFullHub: (() -> Unit)? = null,
    compactSummary: Boolean = false,
) {
    var state by remember { mutableStateOf(economyState) }
    val scope = rememberCoroutineScope()
    val currency = remember { NumberFormat.getCurrencyInstance(Locale.US) }

    suspend fun refresh() {
        state = weekNineRepository.loadEconomy(dynastyId, seasonIndex)
        state?.let(onEconomyLoaded)
    }

    LaunchedEffect(dynastyId, seasonIndex, economyState) {
        if (economyState != null) {
            state = economyState
        } else {
            refresh()
        }
    }

    val economy = state ?: return

    if (compactSummary) {
        Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = stringResource(R.string.fundraising_title),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = stringResource(
                    R.string.fundraising_dashboard_summary,
                    currency.format(economy.cashUsd),
                    currency.format(economy.nilBoostUsd),
                ),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            onOpenFullHub?.let { open ->
                OutlinedButton(onClick = open, modifier = Modifier.fillMaxWidth()) {
                    Text(stringResource(R.string.dashboard_task_open))
                }
            }
        }
        return
    }

    FundraisingWeekNineContent(
        economy = economy,
        currency = currency,
        modifier = modifier,
        onRunEvent = { event ->
            scope.launch {
                weekNineRepository.runFundraising(dynastyId, seasonIndex, event, programPrestige)
                refresh()
            }
        },
        onSellNaming = {
            scope.launch {
                weekNineRepository.sellNamingRights(dynastyId, seasonIndex)
                refresh()
            }
        },
        onUpgrade = { category ->
            scope.launch {
                weekNineRepository.purchaseFacilityUpgrade(dynastyId, seasonIndex, category)
                refresh()
            }
        },
    )
}

@Composable
private fun FundraisingWeekNineContent(
    economy: ProgramEconomyState,
    currency: NumberFormat,
    modifier: Modifier = Modifier,
    onRunEvent: (FundraisingEventType) -> Unit,
    onSellNaming: () -> Unit,
    onUpgrade: (FacilityCategory) -> Unit,
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = stringResource(R.string.fundraising_title),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
        )
        Text(
            text = stringResource(
                R.string.fundraising_balances,
                currency.format(economy.cashUsd),
                currency.format(economy.nilBoostUsd),
            ),
            style = MaterialTheme.typography.bodyLarge,
        )
        Text(
            text = stringResource(R.string.fundraising_events_heading),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
        )
        FundraisingEventType.entries.forEach { event ->
            val done = event in economy.completedEvents
            OutlinedButton(
                onClick = { onRunEvent(event) },
                enabled = !done,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    if (done) {
                        stringResource(R.string.fundraising_event_done, event.label)
                    } else {
                        stringResource(R.string.fundraising_run_event, event.label)
                    },
                )
            }
        }
        OutlinedButton(
            onClick = onSellNaming,
            enabled = !economy.namingRightsSold,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(
                if (economy.namingRightsSold) {
                    stringResource(R.string.fundraising_naming_sold)
                } else {
                    stringResource(R.string.fundraising_naming_rights)
                },
            )
        }
        Text(
            text = stringResource(R.string.fundraising_facilities_heading),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(top = 8.dp),
        )
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            items(FacilityCategory.entries) { category ->
                val grade = economy.facilityGrades.grade(category)
                val cost = FacilityUpgradePolicy.upgradeCostUsd(grade)
                val canBuy = FacilityUpgradePolicy.canAfford(economy.cashUsd, economy.facilityGrades, category)
                Card(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Column {
                            Text(category.label, fontWeight = FontWeight.Medium)
                            Text(stringResource(R.string.fundraising_facility_grade, grade))
                        }
                        OutlinedButton(
                            onClick = { onUpgrade(category) },
                            enabled = canBuy,
                        ) {
                            Text(stringResource(R.string.fundraising_upgrade_cost, currency.format(cost)))
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FundraisingWeekNineBottomSheet(
    dynastyId: String,
    seasonIndex: Int,
    weekNineRepository: OffseasonWeekNineRepository,
    programPrestige: Int,
    onDismiss: () -> Unit,
    onEconomyLoaded: (ProgramEconomyState) -> Unit = {},
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Column(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .verticalScroll(rememberScrollState()),
        ) {
            Text(
                text = stringResource(R.string.fundraising_sheet_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(bottom = 8.dp),
            )
            FundraisingWeekNinePanel(
                dynastyId = dynastyId,
                seasonIndex = seasonIndex,
                weekNineRepository = weekNineRepository,
                programPrestige = programPrestige,
                onEconomyLoaded = onEconomyLoaded,
            )
        }
    }
}
