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
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.novux.dynasty.cbb.R
import dev.novux.dynasty.cbb.core.dashboard.DashboardDeepLink
import dev.novux.dynasty.cbb.core.dashboard.DashboardTask
import dev.novux.dynasty.cbb.core.dashboard.DashboardTaskId
import dev.novux.dynasty.cbb.core.recruiting.RecruitingHubTab

@Composable
fun DashboardOptionalTasksPanel(
    tasks: List<DashboardTask>,
    dismissedKeys: Set<String>,
    onDismiss: (String) -> Unit,
    taskDismissKey: (DashboardTask) -> String = { it.id.name },
    onDeepLink: (RecruitingHubTab) -> Unit,
    onStrategyOpen: () -> Unit = {},
    onCoachCardOpen: () -> Unit = {},
    onSummerWorkoutReportOpen: () -> Unit = {},
    onFundraisingWeekNineOpen: () -> Unit = {},
    onReplayStagedGame: () -> Unit = {},
    headingRes: Int? = null,
    modifier: Modifier = Modifier,
) {
    val visible = tasks.filterNot { taskDismissKey(it) in dismissedKeys }
    if (visible.isEmpty()) return
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = stringResource(headingRes ?: R.string.dashboard_optional_heading),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
        )
        visible.forEach { task ->
            DashboardTaskCard(
                task = task,
                onOpen = {
                    when (task.deepLink) {
                        DashboardDeepLink.ReplayProvisionalWeekGame -> onReplayStagedGame()
                        DashboardDeepLink.Strategy,
                        DashboardDeepLink.PlayWeekGame,
                        -> onStrategyOpen()
                        DashboardDeepLink.CoachCard -> onCoachCardOpen()
                        DashboardDeepLink.SummerWorkoutReport -> onSummerWorkoutReportOpen()
                        DashboardDeepLink.FundraisingWeekNine -> onFundraisingWeekNineOpen()
                        else -> onDeepLink(task.deepLink.toHubTab())
                    }
                },
                onDismiss = { onDismiss(taskDismissKey(task)) },
            )
        }
    }
}

@Composable
private fun DashboardTaskCard(
    task: DashboardTask,
    onOpen: () -> Unit,
    onDismiss: () -> Unit,
) {
    val message = taskMessage(task)
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                DashboardTaskKindChip(task.kind)
            }
            Text(text = message, style = MaterialTheme.typography.bodyLarge)
            OutlinedButton(onClick = onOpen, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.dashboard_task_open))
            }
            TextButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.dashboard_task_dismiss))
            }
        }
    }
}

@Composable
private fun taskMessage(task: DashboardTask): String = when (task.id) {
    DashboardTaskId.SpendRecruitingPoints -> stringResource(
        R.string.dashboard_task_spend_points,
        task.formatArgs.firstOrNull() ?: 0,
    )
    DashboardTaskId.RecruitingWatchlistOffer -> stringResource(
        R.string.dashboard_task_offer_recruit,
        task.stringFormatArgs.firstOrNull().orEmpty(),
    )
    DashboardTaskId.RecruitingWatchlistReEngage -> stringResource(
        R.string.dashboard_task_reengage_recruit,
        task.stringFormatArgs.firstOrNull().orEmpty(),
    )
    DashboardTaskId.RecruitingTop60Movement -> stringResource(
        R.string.dashboard_task_review_top60_recruit,
        task.stringFormatArgs.firstOrNull().orEmpty(),
        task.formatArgs.firstOrNull() ?: 0,
    )
    DashboardTaskId.RecruitingRivalCommit -> stringResource(
        R.string.dashboard_task_rival_commit_respond,
        task.stringFormatArgs.firstOrNull().orEmpty(),
        task.stringFormatArgs.getOrNull(1).orEmpty(),
    )
    DashboardTaskId.PortalWindow -> stringResource(
        R.string.dashboard_task_portal_window,
        task.formatArgs.firstOrNull() ?: 2,
    )
    DashboardTaskId.PortalTop40Published -> stringResource(R.string.dashboard_task_portal_top40)
    DashboardTaskId.PortalPipelineEntrant -> stringResource(
        R.string.dashboard_task_portal_pipeline_entrant,
        task.stringFormatArgs.firstOrNull().orEmpty(),
        task.stringFormatArgs.getOrNull(1).orEmpty(),
    )
    DashboardTaskId.SigningCrunch -> stringResource(
        R.string.dashboard_task_signing_crunch,
        task.formatArgs.firstOrNull() ?: 2,
    )
    DashboardTaskId.OpenScholarships -> stringResource(
        R.string.dashboard_task_open_scholarships,
        task.formatArgs.firstOrNull() ?: 0,
    )
    DashboardTaskId.ReviewInjury -> stringResource(
        R.string.dashboard_task_review_injury,
        task.stringFormatArgs.firstOrNull().orEmpty(),
        task.formatArgs.firstOrNull() ?: 0,
    )
    DashboardTaskId.MedicalRedshirtCandidate -> stringResource(
        R.string.dashboard_task_medical_redshirt,
        task.stringFormatArgs.firstOrNull().orEmpty(),
    )
    DashboardTaskId.SigningDayViewRankings,
    DashboardTaskId.SigningDayEditPositions,
    DashboardTaskId.SigningDayHardSign,
    -> ""
    DashboardTaskId.PlayScheduledGame -> stringResource(
        R.string.dashboard_task_play_game,
        task.stringFormatArgs.firstOrNull().orEmpty(),
        if (task.formatArgs.firstOrNull() == 1) {
            stringResource(R.string.dynasty_shell_home)
        } else {
            stringResource(R.string.dynasty_shell_away)
        },
    )
    DashboardTaskId.ReplayStagedWeekGame -> stringResource(
        R.string.dashboard_task_replay_staged_game,
        task.stringFormatArgs.firstOrNull().orEmpty(),
        if (task.formatArgs.firstOrNull() == 1) {
            stringResource(R.string.dynasty_shell_home)
        } else {
            stringResource(R.string.dynasty_shell_away)
        },
    )
    DashboardTaskId.CoachSundaySkillPoints -> stringResource(
        R.string.dashboard_task_coach_sunday_points,
        task.formatArgs.firstOrNull() ?: 0,
    )
    DashboardTaskId.PreseasonRequired -> ""
    DashboardTaskId.TeamDisciplinePending -> stringResource(
        R.string.dashboard_task_team_discipline,
        task.stringFormatArgs.firstOrNull().orEmpty(),
    )
    DashboardTaskId.FundraisingGala,
    DashboardTaskId.FundraisingAlumniGame,
    DashboardTaskId.FundraisingDonorDrive,
    -> stringResource(
        R.string.dashboard_task_fundraising_event,
        task.stringFormatArgs.firstOrNull().orEmpty(),
    )
    DashboardTaskId.FundraisingNamingRights -> stringResource(R.string.dashboard_task_fundraising_naming)
    DashboardTaskId.FundraisingFacilityUpgrades -> stringResource(
        R.string.dashboard_task_fundraising_facilities,
        task.formatArgs.firstOrNull() ?: 0,
    )
    DashboardTaskId.SummerWorkoutReportReview -> stringResource(
        R.string.dashboard_task_summer_workout_review,
        task.formatArgs.getOrNull(0) ?: 0,
        task.formatArgs.getOrNull(1) ?: 0,
    )
    DashboardTaskId.PromiseAtRisk -> stringResource(
        R.string.dashboard_task_promise_at_risk,
        task.stringFormatArgs.firstOrNull().orEmpty(),
        task.stringFormatArgs.getOrNull(1).orEmpty(),
    )
}

private fun DashboardDeepLink.toHubTab(): RecruitingHubTab = when (this) {
    DashboardDeepLink.RecruitingRecommended -> RecruitingHubTab.Recommended
    DashboardDeepLink.RecruitingPortal -> RecruitingHubTab.TransferPortal
    DashboardDeepLink.RecruitingHsJuco -> RecruitingHubTab.AllHsJuco
    DashboardDeepLink.Strategy,
    DashboardDeepLink.PlayWeekGame,
    DashboardDeepLink.ReplayProvisionalWeekGame,
    DashboardDeepLink.SigningDayRankings,
    DashboardDeepLink.SigningDayPositions,
    DashboardDeepLink.PreseasonChecklist,
    DashboardDeepLink.PreseasonTaskIncomingClassSeed,
    DashboardDeepLink.PreseasonTaskWorkouts,
    DashboardDeepLink.PreseasonTaskCoachUpgrades,
    DashboardDeepLink.PreseasonTaskRankingsReview,
    DashboardDeepLink.PreseasonTaskReport,
    DashboardDeepLink.PreseasonTaskTournamentInvites,
    DashboardDeepLink.PreseasonTaskScheduleExhibitions,
    DashboardDeepLink.PreseasonTaskScheduleRegularSeason,
    DashboardDeepLink.RankingsHub,
    DashboardDeepLink.PlayersLeavingRoster,
    DashboardDeepLink.PlayersLeavingTransfer,
    DashboardDeepLink.PlayersLeavingProDays,
    DashboardDeepLink.CoachCard,
    DashboardDeepLink.DisciplineSheet,
    DashboardDeepLink.SummerWorkoutReport,
    DashboardDeepLink.FundraisingWeekNine,
    -> RecruitingHubTab.Recommended
}
