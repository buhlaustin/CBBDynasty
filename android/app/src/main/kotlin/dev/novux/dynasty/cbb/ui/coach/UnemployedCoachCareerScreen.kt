package dev.novux.dynasty.cbb.ui.coach

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import dev.novux.dynasty.cbb.R
import dev.novux.dynasty.cbb.core.calendar.CalendarWeekLabel
import dev.novux.dynasty.cbb.core.coach.employment.JobOfferCard
import dev.novux.dynasty.cbb.core.save.LoadedDynasty
import dev.novux.dynasty.cbb.ui.dynasty.DynastyShellChrome
import dev.novux.dynasty.cbb.ui.dynasty.DynastyShellMenuAction
import dev.novux.dynasty.cbb.ui.dynasty.DynastyShellNewsTab
import dev.novux.dynasty.cbb.ui.dynasty.DynastyShellTab
import dev.novux.dynasty.cbb.ui.dynasty.ShallowNewsFeedSnapshot

@Composable
fun UnemployedCoachCareerScreen(
    loaded: LoadedDynasty,
    offers: List<JobOfferCard>,
    newsFeed: ShallowNewsFeedSnapshot,
    onAccept: (JobOfferCard) -> Unit,
    onWait: () -> Unit,
    onExitHome: () -> Unit,
) {
    var selectedTab by remember { mutableStateOf(DynastyShellTab.Dashboard) }
    val weekHeader = CalendarWeekLabel.display(loaded.weekCheckpoint.activePosition)
    val schoolName = loaded.summary.schoolDisplayName?.takeIf { it.isNotBlank() }
        ?: stringResource(R.string.job_center_no_program)

    DynastyShellChrome(
        schoolDisplayName = schoolName,
        phaseWeekLabel = weekHeader,
        headerSubtitle = stringResource(R.string.job_center_shell_header_hint),
        selectedTab = selectedTab,
        onTabSelected = { selectedTab = it },
        menuActions = listOf(DynastyShellMenuAction(R.string.shell_menu_exit_home, onExitHome)),
    ) { padding ->
        when (selectedTab) {
            DynastyShellTab.Dashboard -> JobCenterScreen(
                offers = offers,
                onAccept = onAccept,
                onWait = onWait,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
            )
            DynastyShellTab.News -> DynastyShellNewsTab(feed = newsFeed, contentPadding = padding)
            DynastyShellTab.Calendar,
            DynastyShellTab.Roster,
            -> Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(24.dp),
            ) {
                Text(
                    text = stringResource(R.string.job_center_shell_tab_locked),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}