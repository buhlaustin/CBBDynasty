package dev.novux.dynasty.cbb.ui.coach

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.novux.dynasty.cbb.R
import dev.novux.dynasty.cbb.core.coach.security.CoachSecuritySnapshot
import dev.novux.dynasty.cbb.core.coach.security.JobSecurityFactorContribution
import dev.novux.dynasty.cbb.core.qa.CoachJobSecurityContractLinkPath

@Composable
fun JobSecurityMeter(
    snapshot: CoachSecuritySnapshot,
    modifier: Modifier = Modifier,
    showFactorDetails: Boolean = true,
    onNavigateToContract: (() -> Unit)? = null,
) {
    @Suppress("unused")
    val qaTrace = CoachJobSecurityContractLinkPath.MEGA_ID
    val job = snapshot.jobSecurity
    val cardModifier = if (onNavigateToContract != null) {
        modifier
            .fillMaxWidth()
            .clickable(onClick = onNavigateToContract)
    } else {
        modifier.fillMaxWidth()
    }
    Card(modifier = cardModifier) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                stringResource(R.string.job_security_title),
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                stringResource(
                    R.string.job_security_meter,
                    job.score,
                    job.tier.label,
                    job.trend.symbol,
                ),
                style = MaterialTheme.typography.bodyLarge,
            )
            if (showFactorDetails) {
                job.factors.forEach { factor ->
                    val chip = when (factor.contribution) {
                        JobSecurityFactorContribution.Helping -> stringResource(R.string.job_security_helping)
                        JobSecurityFactorContribution.Hurting -> stringResource(R.string.job_security_hurting)
                        JobSecurityFactorContribution.Neutral -> stringResource(R.string.job_security_neutral)
                    }
                    Text(
                        "${factor.label} ($chip): ${factor.summary}",
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }
            if (onNavigateToContract != null) {
                Text(
                    stringResource(R.string.job_security_open_contract),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        }
    }
}

@Composable
fun AdTrustMeter(
    snapshot: CoachSecuritySnapshot,
    modifier: Modifier = Modifier,
) {
    Card(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(stringResource(R.string.ad_trust_title), fontWeight = FontWeight.SemiBold)
            Text(
                stringResource(
                    R.string.ad_trust_meter,
                    snapshot.adTrust.score,
                    snapshot.adTrust.band.label,
                ),
                style = MaterialTheme.typography.bodyLarge,
            )
            Text(
                stringResource(R.string.ad_trust_staff_hint),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
