package dev.novux.dynasty.cbb.ui.coach

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.novux.dynasty.cbb.R
import dev.novux.dynasty.cbb.core.coach.card.CoachCardContractTabPolicy
import java.text.NumberFormat
import java.util.Locale

/** QA traceability: [CoachCardContractTabPolicy.MEGA_ID]. */

/**
 * Contract tab scaffold: optional header, salary/buyout terms block, performance goals block, footer.
 */
@Suppress("unused")
private val coachContractTabQaId: String = CoachCardContractTabPolicy.MEGA_ID

@Composable
fun CoachCardContractTabShell(
    modifier: Modifier = Modifier,
    header: @Composable () -> Unit = {},
    termsBlock: @Composable () -> Unit,
    goalsBlock: (@Composable () -> Unit)? = null,
    footer: @Composable () -> Unit = {},
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        header()
        termsBlock()
        goalsBlock?.invoke()
        footer()
    }
}

/** Salary, buyout, and term length. */
@Composable
fun CoachContractTabTermsBlock(
    annualSalaryUsd: Long,
    buyoutUsd: Long,
    contractLengthYears: Int,
    contractYearsLeft: Int,
    modifier: Modifier = Modifier,
) {
    val money = remember { NumberFormat.getCurrencyInstance(Locale.US) }
    Card(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(stringResource(R.string.coach_contract_terms_title), fontWeight = FontWeight.SemiBold)
            Text(stringResource(R.string.coach_contract_salary, money.format(annualSalaryUsd)))
            Text(stringResource(R.string.coach_contract_buyout, money.format(buyoutUsd)))
            Text(
                stringResource(
                    R.string.coach_contract_length_detail,
                    contractLengthYears,
                    contractYearsLeft,
                ),
            )
        }
    }
}

/** Performance goals list. */
@Composable
fun CoachContractTabGoalsBlock(
    goals: List<String>,
    modifier: Modifier = Modifier,
) {
    if (goals.isEmpty()) return
    Card(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(stringResource(R.string.coach_contract_goals_title), fontWeight = FontWeight.SemiBold)
            goals.forEach { goal ->
                Text("• $goal", style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}
