package dev.novux.dynasty.cbb.ui.dashboard

import androidx.compose.material3.AssistChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import dev.novux.dynasty.cbb.R
import dev.novux.dynasty.cbb.core.dashboard.DashboardTaskKind
import dev.novux.dynasty.cbb.core.qa.DashboardTaskBadgePath

@Composable
fun DashboardTaskKindChip(kind: DashboardTaskKind) {
    @Suppress("unused")
    val qaId = DashboardTaskBadgePath.MEGA_ID // MEGA-FRAME-024
    val label = when (kind) {
        DashboardTaskKind.Required -> stringResource(R.string.dashboard_task_badge_required)
        DashboardTaskKind.Optional -> stringResource(R.string.dashboard_task_badge_optional)
    }
    AssistChip(
        onClick = {},
        enabled = false,
        label = { Text(label, style = MaterialTheme.typography.labelSmall) },
    )
}
