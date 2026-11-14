package dev.novux.dynasty.cbb.ui.preseason

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.novux.dynasty.cbb.R
import dev.novux.dynasty.cbb.core.schedule.PreseasonSchedulePlan
import dev.novux.dynasty.cbb.core.schedule.RegularSeasonScheduleBuilderUxPolicy
import dev.novux.dynasty.cbb.core.schedule.ScheduleGameKind
import dev.novux.dynasty.cbb.core.schedule.ScheduledGameStub

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegularSeasonScheduleReviewBottomSheet(
    plan: PreseasonSchedulePlan,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val weeks = RegularSeasonScheduleBuilderUxPolicy.gamesGroupedByWeek(plan)
    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = stringResource(R.string.preseason_rs_review_title),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = stringResource(
                    R.string.preseason_rs_schedule_summary,
                    plan.totalRsGames,
                    plan.conferenceGameCount,
                    plan.nonConferenceGameCount,
                    plan.earlyEventGameCount,
                ),
                style = MaterialTheme.typography.bodyMedium,
            )
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 400.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(weeks.entries.toList(), key = { it.key }) { (week, games) ->
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = stringResource(R.string.preseason_rs_review_week, week),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                        )
                        games.forEach { game ->
                            Text(
                                text = gameLine(game),
                                style = MaterialTheme.typography.bodySmall,
                            )
                        }
                    }
                }
            }
            Button(onClick = onConfirm, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.preseason_rs_review_confirm))
            }
        }
    }
}

@Composable
private fun gameLine(game: ScheduledGameStub): String {
    val venue = if (game.homeGame) {
        stringResource(R.string.preseason_exhibitions_home)
    } else {
        stringResource(R.string.preseason_exhibitions_away)
    }
    val kind = when (game.kind) {
        ScheduleGameKind.Conference -> stringResource(R.string.preseason_rs_kind_conf)
        ScheduleGameKind.NonConference -> stringResource(R.string.preseason_rs_kind_nonconf)
        ScheduleGameKind.EarlyEvent -> stringResource(R.string.preseason_rs_kind_event)
        ScheduleGameKind.Exhibition -> stringResource(R.string.preseason_rs_kind_exhibition)
    }
    return stringResource(R.string.preseason_rs_review_line, kind, game.opponentDisplayName, venue)
}
