package dev.novux.dynasty.cbb.ui.coach

import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import dev.novux.dynasty.cbb.R
import dev.novux.dynasty.cbb.core.coach.security.CoachSecuritySnapshot
import dev.novux.dynasty.cbb.core.coach.security.JobSecurityPreviewTierChipPolicy
import dev.novux.dynasty.cbb.core.coach.security.JobSecurityTier

/** Lightweight job security affordance for coach quick preview ([MEGA-COACH-188]). */
@Composable
fun JobSecurityTierChip(
    snapshot: CoachSecuritySnapshot,
    modifier: Modifier = Modifier,
) {
    @Suppress("unused")
    val qaId = JobSecurityPreviewTierChipPolicy.MEGA_ID
    val job = snapshot.jobSecurity
    val containerColor = when (job.tier) {
        JobSecurityTier.Secure, JobSecurityTier.Stable -> MaterialTheme.colorScheme.primaryContainer
        JobSecurityTier.Watch -> MaterialTheme.colorScheme.surfaceVariant
        JobSecurityTier.HotSeat, JobSecurityTier.Critical -> MaterialTheme.colorScheme.errorContainer
    }
    AssistChip(
        onClick = {},
        enabled = false,
        label = {
            Text(
                stringResource(
                    R.string.coach_preview_job_security_chip,
                    job.score,
                    job.tier.label,
                    job.trend.symbol,
                ),
            )
        },
        modifier = modifier,
        colors = AssistChipDefaults.assistChipColors(containerColor = containerColor),
    )
}
