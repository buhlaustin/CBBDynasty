package dev.novux.dynasty.cbb.ui.coach

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.novux.dynasty.cbb.R
import dev.novux.dynasty.cbb.core.coach.card.HeadCoachCard
import dev.novux.dynasty.cbb.core.coach.contract.HeadCoachPerformanceGoalsEngine
import java.text.NumberFormat
import java.util.Locale

/** Overview tab contract snapshot ([MEGA-COACH-035]): term, years left, program prestige. */
@Composable
fun CoachOverviewContractCard(
    card: HeadCoachCard,
    programPrestige: Int,
    modifier: Modifier = Modifier,
) {
    val money = remember { NumberFormat.getCurrencyInstance(Locale.US) }
    val performanceGoals = remember(card.schoolDisplayName, programPrestige) {
        HeadCoachPerformanceGoalsEngine.build(programPrestige, card.schoolDisplayName)
    }
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                stringResource(R.string.coach_overview_contract_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                stringResource(
                    R.string.coach_contract_length_detail,
                    card.contractLengthYears,
                    card.contractYearsLeft,
                ),
                style = MaterialTheme.typography.bodyLarge,
            )
            Text(
                stringResource(R.string.school_hub_prestige, programPrestige),
                style = MaterialTheme.typography.bodyMedium,
            )
            Text(
                stringResource(R.string.coach_contract_salary, money.format(card.annualSalaryUsd)),
                style = MaterialTheme.typography.bodyMedium,
            )
            Text(
                stringResource(R.string.coach_contract_goals_title),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
            )
            performanceGoals.displayLines().forEach { line ->
                Text("• $line", style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}
