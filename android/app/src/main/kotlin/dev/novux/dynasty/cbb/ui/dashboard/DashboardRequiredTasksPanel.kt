package dev.novux.dynasty.cbb.ui.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.novux.dynasty.cbb.R
import dev.novux.dynasty.cbb.core.dashboard.DashboardDeepLink
import dev.novux.dynasty.cbb.core.dashboard.DashboardTask
import dev.novux.dynasty.cbb.core.dashboard.DashboardTaskId
import dev.novux.dynasty.cbb.core.preseason.PreseasonTaskId

@Composable
fun DashboardRequiredTasksPanel(
    tasks: List<DashboardTask>,
    onOpenSigningDay: (DashboardDeepLink) -> Unit,
    onOpenDiscipline: () -> Unit = {},
    onOpenSeasonReviewRecap: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    if (tasks.isEmpty()) return
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = stringResource(R.string.dashboard_required_heading),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
        )
        tasks.forEach { task ->
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        DashboardTaskKindChip(task.kind)
                    }
                    Text(text = requiredTaskMessage(task), style = MaterialTheme.typography.bodyLarge)
                    OutlinedButton(
                        onClick = {
                            when (task.deepLink) {
                                DashboardDeepLink.DisciplineSheet -> onOpenDiscipline()
                                DashboardDeepLink.SeasonReviewRecap -> onOpenSeasonReviewRecap()
                                else -> onOpenSigningDay(task.deepLink)
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(stringResource(R.string.dashboard_task_open))
                    }
                }
            }
        }
    }
}

@Composable
private fun requiredTaskMessage(task: DashboardTask): String = when (task.id) {
    DashboardTaskId.SigningDayShellLauncher -> stringResource(R.string.dashboard_task_signing_day_shell_launcher)
    DashboardTaskId.SigningDayViewRankings -> stringResource(R.string.dashboard_task_signing_rankings)
    DashboardTaskId.SigningDayEditPositions -> stringResource(R.string.dashboard_task_signing_positions)
    DashboardTaskId.SigningDayHardSign -> stringResource(R.string.dashboard_task_signing_hard_sign)
    DashboardTaskId.TeamDisciplinePending -> stringResource(
        R.string.dashboard_task_team_discipline,
        task.stringFormatArgs.firstOrNull().orEmpty(),
    )
    DashboardTaskId.PreseasonRequired -> preseasonDashboardTitle(task.stringFormatArgs.firstOrNull())
    DashboardTaskId.CarouselLeagueSim -> stringResource(R.string.carousel_background_hint)
    DashboardTaskId.CarouselUserApproval -> task.stringFormatArgs.getOrNull(1)
        ?: stringResource(R.string.dashboard_task_carousel_approval)
    DashboardTaskId.WeekOneProDay -> stringResource(
        R.string.dashboard_task_week_one_pro_day,
        task.stringFormatArgs.firstOrNull().orEmpty(),
    )
    DashboardTaskId.WeekOneTransferRetention -> stringResource(
        R.string.dashboard_task_week_one_transfer,
        task.stringFormatArgs.firstOrNull().orEmpty(),
    )
    DashboardTaskId.SeasonReviewAcknowledge ->
        stringResource(R.string.dashboard_task_season_review_acknowledge)
    DashboardTaskId.OffseasonRosterLock -> stringResource(
        R.string.dashboard_task_offseason_roster_lock,
        task.stringFormatArgs.getOrNull(0).orEmpty(),
        task.stringFormatArgs.getOrNull(1).orEmpty(),
    )
    DashboardTaskId.OffseasonConferenceOffer -> stringResource(
        R.string.dashboard_task_offseason_conference_offer,
        task.stringFormatArgs.firstOrNull().orEmpty(),
    )
    else -> task.id.name
}

@Composable
private fun preseasonDashboardTitle(taskKey: String?): String {
    val id = taskKey?.let { runCatching { PreseasonTaskId.valueOf(it) }.getOrNull() }
        ?: return taskKey.orEmpty()
    return when (id) {
        PreseasonTaskId.IncomingClassSeed -> stringResource(R.string.preseason_task_incoming_class)
        PreseasonTaskId.PreseasonWorkouts -> stringResource(R.string.preseason_task_workouts)
        PreseasonTaskId.CoachUpgrades -> stringResource(R.string.preseason_task_coach_upgrades)
        PreseasonTaskId.RankingsReview -> stringResource(R.string.preseason_task_rankings)
        PreseasonTaskId.PreseasonReport -> stringResource(R.string.preseason_task_report)
        PreseasonTaskId.TournamentInvites -> stringResource(R.string.preseason_task_tournament_invites)
        PreseasonTaskId.ScheduleExhibitions -> stringResource(R.string.preseason_task_exhibitions)
        PreseasonTaskId.ScheduleRegularSeason -> stringResource(R.string.preseason_task_regular_schedule)
    }
}
