package dev.novux.dynasty.cbb.ui.recruiting

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import dev.novux.dynasty.cbb.R
import dev.novux.dynasty.cbb.core.balance.BalanceNilRules
import dev.novux.dynasty.cbb.core.nil.NilAssignCapLinePolicy
import dev.novux.dynasty.cbb.core.nil.RecruitNilPartialOfferPolicy
import java.text.NumberFormat

@Composable
fun RecruitNilPartialOfferControls(
    nilAskUsd: Int,
    nilRules: BalanceNilRules,
    currentOfferUsd: Long,
    currency: NumberFormat,
    onApplyPartialOffer: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    val cap = remember(nilAskUsd, nilRules) {
        NilAssignCapLinePolicy.maxOfferUsd(nilAskUsd, nilRules)
    }
    val fraction = remember(cap, currentOfferUsd) {
        if (cap <= 0L) 0f else (currentOfferUsd.toFloat() / cap.toFloat()).coerceIn(0f, 1f)
    }
    var sliderValue by remember(cap, currentOfferUsd) { mutableFloatStateOf(fraction) }
    Text(
        text = stringResource(R.string.recruiting_nil_assign_partial_hint),
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(bottom = 4.dp),
    )
    val previewUsd = RecruitNilPartialOfferPolicy.partialOfferUsd(nilAskUsd, nilRules, sliderValue)
    Text(
        text = stringResource(
            R.string.recruiting_nil_partial_slider_label,
            currency.format(previewUsd),
            currency.format(cap),
        ),
        style = MaterialTheme.typography.bodySmall,
    )
    Slider(
        value = sliderValue,
        onValueChange = { sliderValue = it },
        onValueChangeFinished = {
            onApplyPartialOffer(
                RecruitNilPartialOfferPolicy.partialOfferUsd(nilAskUsd, nilRules, sliderValue),
            )
        },
        modifier = modifier.fillMaxWidth(),
    )
}