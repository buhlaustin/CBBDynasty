package dev.novux.dynasty.cbb.ui.offseason

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.novux.dynasty.cbb.R
import dev.novux.dynasty.cbb.core.offseason.weekseven.SummerWorkoutReport
import dev.novux.dynasty.cbb.data.offseason.OffseasonSummerWorkoutRepository

@Composable
fun SummerWorkoutReportPanel(
    dynastyId: String,
    seasonIndex: Int,
    summerWorkoutRepository: OffseasonSummerWorkoutRepository,
    modifier: Modifier = Modifier,
    report: SummerWorkoutReport? = null,
    onReportLoaded: (SummerWorkoutReport) -> Unit = {},
    onOpenFullReport: (() -> Unit)? = null,
) {
    var loadedReport by remember { mutableStateOf(report) }

    LaunchedEffect(dynastyId, seasonIndex, report) {
        if (report != null) {
            loadedReport = report
        } else {
            loadedReport = summerWorkoutRepository.ensureSimulated(dynastyId, seasonIndex)
        }
        loadedReport?.let(onReportLoaded)
    }

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = stringResource(R.string.summer_workouts_title),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
        )
        Text(
            text = stringResource(R.string.summer_workouts_subtitle),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        loadedReport?.let { data ->
            Text(
                text = stringResource(
                    R.string.summer_workouts_summary,
                    data.coachTrainingGrade,
                    data.teamAverageGain,
                    data.lines.size,
                ),
                style = MaterialTheme.typography.bodyLarge,
            )
            OutlinedButton(
                onClick = { onOpenFullReport?.invoke() },
                modifier = Modifier.fillMaxWidth(),
                enabled = onOpenFullReport != null,
            ) {
                Text(stringResource(R.string.dashboard_task_open))
            }
        }
    }
}

@Composable
fun SummerWorkoutReportContent(
    report: SummerWorkoutReport,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = stringResource(
                R.string.summer_workouts_summary,
                report.coachTrainingGrade,
                report.teamAverageGain,
                report.lines.size,
            ),
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium,
        )
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(report.lines, key = { it.playerId }) { line ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(line.displayLabel, fontWeight = FontWeight.Medium)
                        Text(
                            stringResource(
                                R.string.summer_workouts_line,
                                line.gainPoints,
                                line.summary,
                            ),
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SummerWorkoutReportBottomSheet(
    report: SummerWorkoutReport,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Column(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .verticalScroll(rememberScrollState()),
        ) {
            Text(
                text = stringResource(R.string.summer_workouts_sheet_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(bottom = 8.dp),
            )
            SummerWorkoutReportContent(report = report)
        }
    }
}
