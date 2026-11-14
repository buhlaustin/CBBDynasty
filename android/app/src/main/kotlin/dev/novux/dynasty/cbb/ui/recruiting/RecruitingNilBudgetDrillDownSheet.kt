package dev.novux.dynasty.cbb.ui.recruiting

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.novux.dynasty.cbb.R
import dev.novux.dynasty.cbb.core.nil.NilBudgetDrillDown
import dev.novux.dynasty.cbb.core.nil.NilBudgetLineItem
import java.text.NumberFormat

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecruitingNilBudgetDrillDownSheet(
    drillDown: NilBudgetDrillDown,
    currency: NumberFormat,
    onDismiss: () -> Unit,
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = stringResource(R.string.recruiting_nil_drilldown_title),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = stringResource(
                    R.string.recruiting_nil_budget,
                    currency.format(drillDown.ledger.nilRemainingUsd),
                    currency.format(drillDown.ledger.nilPoolAnnualUsd),
                ),
                style = MaterialTheme.typography.bodyLarge,
            )
            SummaryRow(
                label = stringResource(R.string.recruiting_nil_drilldown_committed_total),
                value = currency.format(drillDown.ledger.nilCommittedUsd),
            )
            SummaryRow(
                label = stringResource(R.string.recruiting_nil_drilldown_reserved_total),
                value = currency.format(drillDown.ledger.nilReservedUsd),
            )
            HorizontalDivider()
            SectionHeader(stringResource(R.string.recruiting_nil_drilldown_committed_header))
            if (drillDown.committedLines.isEmpty()) {
                EmptyLine()
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(drillDown.committedLines, key = { it.prospectId ?: it.label }) { line ->
                        LineItemRow(line, currency)
                    }
                }
            }
            SectionHeader(stringResource(R.string.recruiting_nil_drilldown_reserved_header))
            if (drillDown.reservedLines.isEmpty()) {
                EmptyLine()
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(drillDown.reservedLines, key = { it.prospectId ?: it.label }) { line ->
                        LineItemRow(line, currency)
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionHeader(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.SemiBold,
    )
}

@Composable
private fun EmptyLine() {
    Text(
        text = stringResource(R.string.recruiting_nil_drilldown_empty),
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

@Composable
private fun SummaryRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, style = MaterialTheme.typography.bodyMedium)
        Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun LineItemRow(line: NilBudgetLineItem, currency: NumberFormat) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(line.label, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
        Text(currency.format(line.amountUsd), style = MaterialTheme.typography.bodyMedium)
    }
}
