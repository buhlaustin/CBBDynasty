package dev.novux.dynasty.cbb.ui.recruiting

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import dev.novux.dynasty.cbb.R
import dev.novux.dynasty.cbb.core.balance.BalanceNilRules
import dev.novux.dynasty.cbb.core.nil.NilAssignCapLinePolicy
import dev.novux.dynasty.cbb.core.nil.NilBudgetLedger
import dev.novux.dynasty.cbb.core.nil.RecruitNilAssignableDisplayPolicy
import java.text.NumberFormat
import java.util.Locale

@Composable
fun RecruitNilAssignCapLine(
    nilAskUsd: Int,
    nilRules: BalanceNilRules,
    nilLedger: NilBudgetLedger? = null,
    priorReservationUsd: Long = 0L,
    modifier: Modifier = Modifier,
) {
    @Suppress("unused")
    val qaId = NilAssignCapLinePolicy.MEGA_ID
    @Suppress("unused")
    val assignableId = RecruitNilAssignableDisplayPolicy.MEGA_ID
    val currency = NumberFormat.getCurrencyInstance(Locale.US)
    val maxOffer = NilAssignCapLinePolicy.maxOfferUsd(nilAskUsd, nilRules)
    val assignable = nilLedger?.let {
        RecruitNilAssignableDisplayPolicy.assignableUsd(
            nilAskUsd,
            nilRules,
            it,
            priorReservationUsd,
        )
    }
    Column(modifier = modifier) {
        Text(
            text = stringResource(
                R.string.recruiting_nil_max_offer_line,
                currency.format(maxOffer),
                nilRules.maxOfferMultiplierOfAsk,
            ),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        if (assignable != null) {
            Text(
                text = stringResource(
                    R.string.recruiting_nil_assignable_line,
                    currency.format(assignable),
                ),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
            )
        }
    }
}