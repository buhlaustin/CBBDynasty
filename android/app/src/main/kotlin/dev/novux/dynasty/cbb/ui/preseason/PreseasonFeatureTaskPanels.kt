package dev.novux.dynasty.cbb.ui.preseason

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.novux.dynasty.cbb.R
import dev.novux.dynasty.cbb.core.offseason.weekseven.SummerWorkoutReport
import dev.novux.dynasty.cbb.core.offseason.weekseven.SummerWorkoutSimulator
import dev.novux.dynasty.cbb.core.preseason.PreseasonTaskId
import dev.novux.dynasty.cbb.data.coach.CoachSkillPointRepository
import dev.novux.dynasty.cbb.data.preseason.PreseasonProgressRepository
import dev.novux.dynasty.cbb.data.roster.DynastyRosterRepository
import kotlinx.coroutines.launch

@Composable
fun PreseasonWorkoutsPanel(
    dynastyId: String,
    seasonIndex: Int,
    rosterRepository: DynastyRosterRepository,
    progressRepository: PreseasonProgressRepository,
    onComplete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var report by remember { mutableStateOf<SummerWorkoutReport?>(null) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(dynastyId, seasonIndex) {
        val players = rosterRepository.listPlayers(dynastyId)
        report = SummerWorkoutSimulator.simulate(
            players = players,
            dynastySeed = dynastyId.hashCode().toLong() + seasonIndex,
        )
    }

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = stringResource(R.string.preseason_task_workouts),
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
        )
        Text(
            text = stringResource(R.string.preseason_workouts_body),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = stringResource(R.string.preseason_workouts_redshirt_hint),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        report?.let { row ->
            Text(
                text = stringResource(
                    R.string.summer_workouts_summary,
                    row.coachTrainingGrade,
                    row.teamAverageGain,
                    row.lines.size,
                ),
                style = MaterialTheme.typography.bodySmall,
            )
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                row.lines.take(8).forEach { line ->
                    Text(
                        text = stringResource(
                            R.string.summer_workouts_line,
                            line.gainPoints,
                            line.displayLabel,
                        ),
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }
        }
        OutlinedButton(
            onClick = {
                scope.launch {
                    progressRepository.completeTask(dynastyId, seasonIndex, PreseasonTaskId.PreseasonWorkouts)
                    onComplete()
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = report != null,
        ) {
            Text(stringResource(R.string.preseason_workouts_confirm))
        }
    }
}

@Composable
fun PreseasonCoachUpgradesPanel(
    dynastyId: String,
    seasonIndex: Int,
    coachSkillPointRepository: CoachSkillPointRepository,
    progressRepository: PreseasonProgressRepository,
    onOpenStrategy: () -> Unit,
    onComplete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var bank by remember { mutableStateOf(0) }
    var strategyOpened by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(dynastyId) {
        bank = coachSkillPointRepository.loadState(dynastyId).unspentBank
    }

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = stringResource(R.string.preseason_task_coach_upgrades),
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
        )
        Text(
            text = stringResource(R.string.preseason_coach_upgrades_body, bank),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        OutlinedButton(
            onClick = {
                strategyOpened = true
                onOpenStrategy()
            },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(stringResource(R.string.preseason_coach_open_strategy))
        }
        if (!strategyOpened) {
            Text(
                text = stringResource(R.string.preseason_coach_upgrades_strategy_required),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        OutlinedButton(
            onClick = {
                scope.launch {
                    progressRepository.completeTask(dynastyId, seasonIndex, PreseasonTaskId.CoachUpgrades)
                    onComplete()
                }
            },
            enabled = strategyOpened,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(stringResource(R.string.preseason_coach_bank_season))
        }
    }
}

@Composable
fun PreseasonRankingsReviewPanel(
    onOpenReviewSheet: () -> Unit,
    onOpenRankingsHub: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = stringResource(R.string.preseason_task_rankings),
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
        )
        Text(
            text = stringResource(R.string.preseason_rankings_review_body),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = stringResource(R.string.preseason_rankings_no_shortcut_hint),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        OutlinedButton(onClick = onOpenReviewSheet, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(R.string.preseason_rankings_open_review))
        }
        OutlinedButton(onClick = onOpenRankingsHub, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(R.string.preseason_rankings_open_hub))
        }
    }
}
