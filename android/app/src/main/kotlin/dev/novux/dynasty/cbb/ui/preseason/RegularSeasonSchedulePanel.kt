package dev.novux.dynasty.cbb.ui.preseason

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.LinearProgressIndicator
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
import dev.novux.dynasty.cbb.core.preseason.PreseasonTaskId
import dev.novux.dynasty.cbb.core.schedule.PreseasonSchedulePlan
import dev.novux.dynasty.cbb.core.schedule.PreseasonScheduleValidation
import dev.novux.dynasty.cbb.core.schedule.RegularSeasonScheduleBuilderUxPolicy
import dev.novux.dynasty.cbb.core.schedule.RegularSeasonScheduleSolveResult
import dev.novux.dynasty.cbb.core.schedule.ScheduleGameKind
import dev.novux.dynasty.cbb.data.preseason.PreseasonProgressRepository
import dev.novux.dynasty.cbb.data.preseason.PreseasonScheduleRepository
import kotlinx.coroutines.launch

@Composable
fun RegularSeasonSchedulePanel(
    dynastyId: String,
    seasonIndex: Int,
    scheduleRepository: PreseasonScheduleRepository,
    progressRepository: PreseasonProgressRepository,
    onSaved: () -> Unit,
    onValidationChanged: (PreseasonScheduleValidation?) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    var plan by remember { mutableStateOf<PreseasonSchedulePlan?>(null) }
    var builderContext by remember {
        mutableStateOf<RegularSeasonScheduleBuilderUxPolicy.BuilderContext?>(null)
    }
    var solving by remember { mutableStateOf(false) }
    var validationError by remember { mutableStateOf<PreseasonScheduleValidation?>(null) }
    var reviewPlan by remember { mutableStateOf<PreseasonSchedulePlan?>(null) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(dynastyId, seasonIndex) {
        plan = scheduleRepository.loadPlan(dynastyId, seasonIndex)
        builderContext = scheduleRepository.loadRegularSeasonBuilderContext(dynastyId, seasonIndex)
    }

    LaunchedEffect(validationError) {
        onValidationChanged(validationError)
    }

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = stringResource(R.string.preseason_rs_schedule_title),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
        )
        builderContext?.let { ctx ->
            val slots = RegularSeasonScheduleBuilderUxPolicy.previewSlotSummary(ctx)
            Text(
                text = stringResource(
                    R.string.preseason_rs_builder_slots,
                    slots.builderSlots,
                    slots.totalRsCap,
                ),
                style = MaterialTheme.typography.bodyMedium,
            )
            Text(
                text = stringResource(
                    R.string.preseason_rs_builder_breakdown,
                    slots.eventRsGames,
                    slots.conferenceLocked,
                    slots.nonConferenceSlots,
                    ctx.exhibitionCount,
                ),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        validationError?.let { err ->
            Text(
                text = validationErrorMessage(err),
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium,
            )
        }
        if (solving) {
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
        }
        plan?.let { current ->
            Text(
                text = stringResource(
                    R.string.preseason_rs_schedule_summary,
                    current.totalRsGames,
                    current.conferenceGameCount,
                    current.nonConferenceGameCount,
                    current.earlyEventGameCount,
                ),
                style = MaterialTheme.typography.bodyMedium,
            )
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                current.regularSeason
                    .take(12)
                    .forEach { game ->
                        Text(
                            rsGamePreviewLine(game),
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                if (current.regularSeason.size > 12) {
                    Text(stringResource(R.string.preseason_rs_more_conf), style = MaterialTheme.typography.bodySmall)
                }
            }
        }
        OutlinedButton(
            onClick = {
                scope.launch {
                    solving = true
                    validationError = null
                    when (val outcome = scheduleRepository.buildRegularSeasonSchedule(dynastyId, seasonIndex)) {
                        is RegularSeasonScheduleSolveResult.Saved -> {
                            plan = outcome.plan
                            reviewPlan = outcome.plan
                        }
                        is RegularSeasonScheduleSolveResult.Invalid -> validationError = outcome.validation
                        RegularSeasonScheduleSolveResult.MissingContext -> validationError =
                            PreseasonScheduleValidation(false, "MissingContext")
                    }
                    solving = false
                }
            },
            enabled = !solving,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(
                if (solving) {
                    stringResource(R.string.preseason_rs_solving)
                } else if (plan != null) {
                    stringResource(R.string.preseason_rs_rebuild)
                } else {
                    stringResource(R.string.preseason_rs_run_solver)
                },
            )
        }
    }

    reviewPlan?.let { built ->
        RegularSeasonScheduleReviewBottomSheet(
            plan = built,
            onConfirm = {
                scope.launch {
                    progressRepository.completeTask(
                        dynastyId,
                        seasonIndex,
                        PreseasonTaskId.ScheduleRegularSeason,
                    )
                    reviewPlan = null
                    onSaved()
                }
            },
            onDismiss = { reviewPlan = null },
        )
    }
}

@Composable
private fun validationErrorMessage(validation: PreseasonScheduleValidation): String = when (validation.message) {
    "Too many exhibition games" -> stringResource(R.string.preseason_rs_error_exhibitions)
    "Over regular-season cap" -> stringResource(R.string.preseason_rs_error_over_cap)
    "Over builder slot budget" -> stringResource(R.string.preseason_rs_error_builder)
    "Event plus builder exceeds cap" -> stringResource(R.string.preseason_rs_error_event_builder)
    "Double-booked regular-season week" -> stringResource(R.string.preseason_rs_error_double_book)
    "MissingContext" -> stringResource(R.string.preseason_rs_error_missing)
    else -> stringResource(R.string.preseason_rs_error_generic)
}

@Composable
private fun rsGamePreviewLine(game: dev.novux.dynasty.cbb.core.schedule.ScheduledGameStub): String {
    val ha = if (game.homeGame) "H" else "A"
    return when (game.kind) {
        ScheduleGameKind.Conference -> stringResource(R.string.preseason_rs_line_conf, game.opponentDisplayName, ha)
        ScheduleGameKind.NonConference -> stringResource(R.string.preseason_rs_line_nonconf, game.opponentDisplayName, ha)
        ScheduleGameKind.EarlyEvent -> stringResource(R.string.preseason_rs_line_event, game.opponentDisplayName, ha)
        ScheduleGameKind.Exhibition -> stringResource(R.string.preseason_rs_line_exhibition, game.opponentDisplayName, ha)
    }
}
