package dev.novux.dynasty.cbb.ui.offseason.leaving

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import dev.novux.dynasty.cbb.R
import dev.novux.dynasty.cbb.core.balance.BalanceNilRules
import dev.novux.dynasty.cbb.core.nil.NilAssignCapLinePolicy
import dev.novux.dynasty.cbb.core.nil.NilAssignRejectReason
import dev.novux.dynasty.cbb.core.offseason.weekone.WeekOneNilRetentionPolicy
import dev.novux.dynasty.cbb.core.qa.WeekOneNilRetentionPath
import dev.novux.dynasty.cbb.core.roster.RosterPlayer
import dev.novux.dynasty.cbb.core.save.LoadedDynasty
import dev.novux.dynasty.cbb.data.nil.NilAnnualPoolRepository
import dev.novux.dynasty.cbb.data.nil.NilBudgetRepository
import dev.novux.dynasty.cbb.data.roster.DynastyRosterRepository
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeekOneNilRetentionSheet(
    player: RosterPlayer?,
    loaded: LoadedDynasty,
    nilRules: BalanceNilRules,
    nilBudgetRepository: NilBudgetRepository,
    nilAnnualPoolRepository: NilAnnualPoolRepository,
    rosterRepository: DynastyRosterRepository,
    onDismiss: () -> Unit,
    onPlayersChanged: (List<RosterPlayer>) -> Unit,
) {
    if (player == null) return
    @Suppress("unused")
    val qaId = WeekOneNilRetentionPath.MEGA_ID
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val currency = NumberFormat.getCurrencyInstance(Locale.US)
    val askUsd = WeekOneNilRetentionPolicy.retentionAskUsd(player)
    val suggested = WeekOneNilRetentionPolicy.suggestedOfferUsd(player)
    val targetId = WeekOneNilRetentionPolicy.ledgerTargetId(player.playerId)

    suspend fun applyOffer(offerUsd: Long) {
        val poolUsd = nilAnnualPoolRepository.effectivePoolUsd(loaded)
        val rosterCommitted = rosterRepository.listPlayers(loaded.dynastyId)
            .sumOf { it.nilUsdPerYear }
        val result = nilBudgetRepository.assignOffer(
            loaded.dynastyId,
            loaded.metadata.seasonIndex,
            targetId,
            askUsd,
            offerUsd,
            nilRules,
            poolUsd,
            rosterCommittedUsd = rosterCommitted,
        )
        if (!result.accepted) {
            val msg = when (result.rejectReason) {
                NilAssignRejectReason.ExceedsAskMultiplier ->
                    context.getString(R.string.week_one_nil_rejection_cap)
                NilAssignRejectReason.ExceedsRemainingBudget ->
                    context.getString(R.string.week_one_nil_rejection_budget)
                else -> context.getString(R.string.week_one_nil_rejection_generic)
            }
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
            return
        }
        val updated = rosterRepository.applyNilRetentionBump(loaded.dynastyId, player.playerId, result.clampedOfferUsd)
        onPlayersChanged(updated)
        onDismiss()
    }

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                stringResource(R.string.week_one_nil_sheet_title, player.displayLabel()),
                style = MaterialTheme.typography.titleMedium,
            )
            Text(
                stringResource(
                    R.string.week_one_nil_current_deal,
                    currency.format(player.nilUsdPerYear),
                ),
                style = MaterialTheme.typography.bodyMedium,
            )
            Text(
                stringResource(
                    R.string.week_one_nil_suggested_offer,
                    currency.format(suggested),
                ),
                style = MaterialTheme.typography.bodyMedium,
            )
            RecruitNilAssignCapLine(nilAskUsd = askUsd, nilRules = nilRules)
            val ledgerCap = NilAssignCapLinePolicy.maxOfferUsd(askUsd, nilRules)
            Text(
                stringResource(R.string.week_one_nil_ledger_note),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Button(
                onClick = {
                    scope.launch { applyOffer(suggested.coerceAtMost(ledgerCap)) }
                },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(stringResource(R.string.week_one_nil_apply_suggested))
            }
            OutlinedButton(
                onClick = {
                    scope.launch { applyOffer(0L) }
                },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(stringResource(R.string.week_one_nil_clear_deal))
            }
        }
    }
}