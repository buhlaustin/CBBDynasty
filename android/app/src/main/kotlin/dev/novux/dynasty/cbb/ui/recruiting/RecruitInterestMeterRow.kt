package dev.novux.dynasty.cbb.ui.recruiting

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import dev.novux.dynasty.cbb.R
import dev.novux.dynasty.cbb.core.recruiting.RecruitInterestMeterDisplay
import dev.novux.dynasty.cbb.core.recruiting.RecruitInterestMeterUiPolicy

@Composable
fun RecruitInterestMeterRow(
    baselineInterest: Int,
    liveInterest: Int,
    modifier: Modifier = Modifier,
) {
    @Suppress("unused")
    val meterPolicyId = RecruitInterestMeterUiPolicy.MEGA_ID
    val live = RecruitInterestMeterDisplay.clampedLiveInterest(liveInterest)
    val gain = RecruitInterestMeterDisplay.spendGainPoints(baselineInterest, liveInterest)
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(4.dp)) {
        LinearProgressIndicator(
            progress = { RecruitInterestMeterDisplay.progressFraction(liveInterest) },
            modifier = Modifier.fillMaxWidth(),
        )
        Text(
            text = if (gain > 0) {
                stringResource(R.string.recruiting_interest_meter_with_gain, live, baselineInterest.coerceIn(0, 100))
            } else {
                stringResource(R.string.recruiting_interest_meter, live)
            },
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
