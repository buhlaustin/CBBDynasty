package dev.novux.dynasty.cbb.ui.preseason

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.AssistChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.novux.dynasty.cbb.R
import dev.novux.dynasty.cbb.core.dashboard.DashboardTask

/** Quick jumps to checklist rows — not a second full task card list ([MEGA-FRAME-087]). */
@Composable
fun PreseasonRequiredTaskChips(
    tasks: List<DashboardTask>,
    onOpenTask: (DashboardTask) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (tasks.isEmpty()) return
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            text = stringResource(R.string.preseason_required_jump_heading),
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            tasks.forEach { task ->
                val title = preseasonChipLabel(task.stringFormatArgs.firstOrNull())
                AssistChip(
                    onClick = { onOpenTask(task) },
                    label = { Text(title) },
                )
            }
        }
    }
}

@Composable
private fun preseasonChipLabel(taskKey: String?): String {
    val id = taskKey?.let { runCatching { dev.novux.dynasty.cbb.core.preseason.PreseasonTaskId.valueOf(it) }.getOrNull() }
        ?: return taskKey.orEmpty()
    return when (id) {
        dev.novux.dynasty.cbb.core.preseason.PreseasonTaskId.IncomingClassSeed ->
            stringResource(R.string.preseason_task_incoming_class)
        dev.novux.dynasty.cbb.core.preseason.PreseasonTaskId.PreseasonWorkouts ->
            stringResource(R.string.preseason_task_workouts)
        dev.novux.dynasty.cbb.core.preseason.PreseasonTaskId.CoachUpgrades ->
            stringResource(R.string.preseason_task_coach_upgrades)
        dev.novux.dynasty.cbb.core.preseason.PreseasonTaskId.RankingsReview ->
            stringResource(R.string.preseason_task_rankings)
        dev.novux.dynasty.cbb.core.preseason.PreseasonTaskId.PreseasonReport ->
            stringResource(R.string.preseason_task_report)
        dev.novux.dynasty.cbb.core.preseason.PreseasonTaskId.TournamentInvites ->
            stringResource(R.string.preseason_task_tournament_invites)
        dev.novux.dynasty.cbb.core.preseason.PreseasonTaskId.ScheduleExhibitions ->
            stringResource(R.string.preseason_task_exhibitions)
        dev.novux.dynasty.cbb.core.preseason.PreseasonTaskId.ScheduleRegularSeason ->
            stringResource(R.string.preseason_task_regular_schedule)
    }
}
