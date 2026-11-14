package dev.novux.dynasty.cbb.ui.awards

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.novux.dynasty.cbb.R
import dev.novux.dynasty.cbb.core.awards.AwardHonorLine
import dev.novux.dynasty.cbb.core.awards.AwardsDemoCatalog
import dev.novux.dynasty.cbb.core.awards.AwardsHubPolicy
import dev.novux.dynasty.cbb.core.awards.AwardsWeeklyCadencePolicy
import dev.novux.dynasty.cbb.core.awards.ConferenceWeeklyAwardsPolicy
import dev.novux.dynasty.cbb.core.awards.YearlyAwardsSeasonArchive
import dev.novux.dynasty.cbb.core.calendar.CalendarPosition
import dev.novux.dynasty.cbb.core.calendar.SeasonMembershipYear

private enum class AwardsHubRoute { Landing, Yearly, Weekly }

private enum class YearlyPrimaryTab { Ncba, Conference }

private enum class YearlyNcbaSubTab { Overview, Mvp, ByPosition, AllAmerica, Coaches }

private enum class YearlyConferenceSubTab { Overview, Player, AllConference, Coach }

data class ConferenceOption(val id: String, val displayName: String)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AwardsHubScreen(
    seasonIndex: Int,
    position: CalendarPosition,
    userConferenceId: String,
    userConferenceName: String,
    conferences: List<ConferenceOption>,
    currentSeasonArchive: YearlyAwardsSeasonArchive? = null,
    onBack: () -> Unit,
) {
    var route by remember { mutableStateOf(AwardsHubRoute.Landing) }
    when (route) {
        AwardsHubRoute.Landing -> AwardsLanding(
            onBack = onBack,
            onYearly = { route = AwardsHubRoute.Yearly },
            onWeekly = { route = AwardsHubRoute.Weekly },
        )
        AwardsHubRoute.Yearly -> AwardsYearlyScreen(
            seasonIndex = seasonIndex,
            position = position,
            userConferenceId = userConferenceId,
            userConferenceName = userConferenceName,
            conferences = conferences,
            currentSeasonArchive = currentSeasonArchive,
            onBack = { route = AwardsHubRoute.Landing },
        )
        AwardsHubRoute.Weekly -> AwardsWeeklyScreen(
            seasonIndex = seasonIndex,
            position = position,
            userConferenceName = userConferenceName,
            onBack = { route = AwardsHubRoute.Landing },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AwardsLanding(onBack: () -> Unit, onYearly: () -> Unit, onWeekly: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(title = { Text(stringResource(R.string.awards_hub_title)) })
        OutlinedButton(onClick = onBack, modifier = Modifier.padding(horizontal = 16.dp)) {
            Text(stringResource(R.string.dynasty_shell_back))
        }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = stringResource(R.string.awards_hub_shell_access_hint),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Button(onClick = onYearly, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.awards_hub_yearly_button))
            }
            Button(onClick = onWeekly, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.awards_hub_weekly_button))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AwardsYearlyScreen(
    seasonIndex: Int,
    position: CalendarPosition,
    userConferenceId: String,
    userConferenceName: String,
    conferences: List<ConferenceOption>,
    currentSeasonArchive: YearlyAwardsSeasonArchive? = null,
    onBack: () -> Unit,
) {
    var primaryTab by remember { mutableIntStateOf(YearlyPrimaryTab.Ncba.ordinal) }
    var nbaSub by remember { mutableIntStateOf(YearlyNcbaSubTab.Overview.ordinal) }
    var confSub by remember { mutableIntStateOf(YearlyConferenceSubTab.Overview.ordinal) }
    var selectedConfId by remember(userConferenceId) { mutableStateOf(userConferenceId) }
    var confMenuExpanded by remember { mutableStateOf(false) }
    val seasonLabel = SeasonMembershipYear.label(seasonIndex)
    val priorLabel = SeasonMembershipYear.label((seasonIndex - 1).coerceAtLeast(1))
    var seasonChip by remember { mutableIntStateOf(0) }
    val viewingCurrent = seasonChip == 0
    val locked = viewingCurrent && AwardsHubPolicy.yearlyCurrentSeasonLocked(position.phase)
    val displayLabel = if (viewingCurrent) seasonLabel else priorLabel

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(title = { Text(stringResource(R.string.awards_yearly_title)) })
        OutlinedButton(onClick = onBack, modifier = Modifier.padding(horizontal = 16.dp)) {
            Text(stringResource(R.string.awards_hub_back_landing))
        }
        ScrollableTabRow(selectedTabIndex = seasonChip) {
            Tab(selected = seasonChip == 0, onClick = { seasonChip = 0 }, text = { Text(seasonLabel) })
            if (seasonIndex > 1) {
                Tab(selected = seasonChip == 1, onClick = { seasonChip = 1 }, text = { Text(priorLabel) })
            }
        }
        if (locked) {
            Text(
                stringResource(R.string.awards_yearly_locked),
                modifier = Modifier.padding(16.dp),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.primary,
            )
        }
        ScrollableTabRow(selectedTabIndex = primaryTab) {
            YearlyPrimaryTab.entries.forEachIndexed { index, tab ->
                Tab(
                    selected = primaryTab == index,
                    onClick = { primaryTab = index },
                    text = {
                        Text(
                            when (tab) {
                                YearlyPrimaryTab.Ncba -> stringResource(R.string.awards_tab_ncba)
                                YearlyPrimaryTab.Conference -> stringResource(R.string.awards_tab_conference)
                            },
                        )
                    },
                )
            }
        }
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            when (YearlyPrimaryTab.entries[primaryTab]) {
                YearlyPrimaryTab.Ncba -> {
                    ScrollableTabRow(selectedTabIndex = nbaSub) {
                        YearlyNcbaSubTab.entries.forEachIndexed { index, tab ->
                            Tab(
                                selected = nbaSub == index,
                                onClick = { nbaSub = index },
                                text = { Text(nbaSubLabel(tab)) },
                            )
                        }
                    }
                    if (!locked) {
                        val useArchive = viewingCurrent && currentSeasonArchive != null
                        honorLines(
                            when (YearlyNcbaSubTab.entries[nbaSub]) {
                                YearlyNcbaSubTab.Overview -> if (useArchive) {
                                    currentSeasonArchive!!.ncbaOverview
                                } else {
                                    AwardsDemoCatalog.ncbaOverview(displayLabel)
                                }
                                YearlyNcbaSubTab.Mvp -> if (useArchive) {
                                    currentSeasonArchive!!.ncbaMvp
                                } else {
                                    AwardsDemoCatalog.ncbaOverview(displayLabel).take(1)
                                }
                                YearlyNcbaSubTab.ByPosition -> if (useArchive) {
                                    currentSeasonArchive!!.ncbaByPosition["PG"].orEmpty()
                                } else {
                                    AwardsDemoCatalog.ncbaPositionPoy("PG")
                                }
                                YearlyNcbaSubTab.AllAmerica -> emptyList()
                                YearlyNcbaSubTab.Coaches -> if (useArchive) {
                                    currentSeasonArchive!!.ncbaCoaches
                                } else {
                                    AwardsDemoCatalog.ncbaOverview(displayLabel).drop(1)
                                }
                            },
                        )
                    }
                }
                YearlyPrimaryTab.Conference -> {
                    val sorted = remember(conferences) {
                        conferences.sortedBy { it.displayName }
                    }
                    val selectedName = sorted.firstOrNull { it.id == selectedConfId }?.displayName
                        ?: userConferenceName
                    ExposedDropdownMenuBox(
                        expanded = confMenuExpanded,
                        onExpandedChange = { confMenuExpanded = it },
                    ) {
                        OutlinedTextField(
                            value = selectedName,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text(stringResource(R.string.awards_conference_picker)) },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = confMenuExpanded) },
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth(),
                        )
                        ExposedDropdownMenu(
                            expanded = confMenuExpanded,
                            onDismissRequest = { confMenuExpanded = false },
                        ) {
                            sorted.forEach { option ->
                                DropdownMenuItem(
                                    text = { Text(option.displayName) },
                                    onClick = {
                                        selectedConfId = option.id
                                        confMenuExpanded = false
                                    },
                                )
                            }
                        }
                    }
                    ScrollableTabRow(selectedTabIndex = confSub) {
                        YearlyConferenceSubTab.entries.forEachIndexed { index, tab ->
                            Tab(
                                selected = confSub == index,
                                onClick = { confSub = index },
                                text = { Text(confSubLabel(tab)) },
                            )
                        }
                    }
                    if (!locked) {
                        val confLines = if (viewingCurrent && currentSeasonArchive != null) {
                            currentSeasonArchive.conferenceAwards[selectedConfId]
                                ?: currentSeasonArchive.conferenceAwards[userConferenceId]
                                .orEmpty()
                        } else {
                            AwardsDemoCatalog.conferenceOverview(selectedName)
                        }
                        honorLines(confLines)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AwardsWeeklyScreen(
    seasonIndex: Int,
    position: CalendarPosition,
    userConferenceName: String,
    onBack: () -> Unit,
) {
    val maxWeek = position.weekIndex.coerceIn(1, 32)
    var week by remember(position) { mutableIntStateOf(maxWeek) }
    val live = AwardsWeeklyCadencePolicy.weeklyAwardsLive(position.phase)

    Column(modifier = Modifier.fillMaxSize()) {
        @Suppress("unused")
        val qaCadence = AwardsWeeklyCadencePolicy.MEGA_ID
        TopAppBar(title = { Text(stringResource(R.string.awards_weekly_title)) })
        OutlinedButton(onClick = onBack, modifier = Modifier.padding(horizontal = 16.dp)) {
            Text(stringResource(R.string.awards_hub_back_landing))
        }
        if (!live) {
            Text(
                stringResource(R.string.awards_weekly_offseason),
                modifier = Modifier.padding(16.dp),
                style = MaterialTheme.typography.bodyMedium,
            )
        } else {
            Text(
                stringResource(R.string.awards_weekly_cadence_hint),
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            if (ConferenceWeeklyAwardsPolicy.honorsDuringConferencePlay(position.phase, position.weekIndex)) {
                Text(
                    stringResource(R.string.awards_weekly_conference_hint, userConferenceName),
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            ScrollableTabRow(selectedTabIndex = (week - 1).coerceAtLeast(0)) {
                (1..maxWeek.coerceAtMost(32)).forEach { w ->
                    Tab(
                        selected = week == w,
                        onClick = { week = w },
                        text = { Text(stringResource(R.string.awards_week_chip, w)) },
                    )
                }
            }
            Column(modifier = Modifier.padding(16.dp)) {
                honorLines(listOf(AwardsDemoCatalog.weeklyPotw(week, userConferenceName)))
            }
        }
    }
}

@Composable
private fun honorLines(lines: List<AwardHonorLine>) {
    if (lines.isEmpty()) {
        Text(
            stringResource(R.string.awards_no_rows),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        return
    }
    lines.forEach { line -> HonorCard(line) }
}

@Composable
private fun HonorCard(line: AwardHonorLine) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(line.awardLabel, fontWeight = FontWeight.SemiBold)
            Text(stringResource(R.string.awards_winner_line, line.winnerName, line.schoolName))
            line.runnerUpName?.let {
                Text(
                    stringResource(R.string.awards_runner_up_line, it),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun nbaSubLabel(tab: YearlyNcbaSubTab): String = when (tab) {
    YearlyNcbaSubTab.Overview -> stringResource(R.string.awards_sub_overview)
    YearlyNcbaSubTab.Mvp -> stringResource(R.string.awards_sub_mvp)
    YearlyNcbaSubTab.ByPosition -> stringResource(R.string.awards_sub_by_position)
    YearlyNcbaSubTab.AllAmerica -> stringResource(R.string.awards_sub_all_america)
    YearlyNcbaSubTab.Coaches -> stringResource(R.string.awards_sub_coaches)
}

@Composable
private fun confSubLabel(tab: YearlyConferenceSubTab): String = when (tab) {
    YearlyConferenceSubTab.Overview -> stringResource(R.string.awards_sub_overview)
    YearlyConferenceSubTab.Player -> stringResource(R.string.awards_sub_player)
    YearlyConferenceSubTab.AllConference -> stringResource(R.string.awards_sub_all_conference)
    YearlyConferenceSubTab.Coach -> stringResource(R.string.awards_sub_coach)
}
