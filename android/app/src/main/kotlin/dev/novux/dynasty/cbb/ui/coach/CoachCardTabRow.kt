package dev.novux.dynasty.cbb.ui.coach

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import dev.novux.dynasty.cbb.core.qa.CoachCardScrollableTabsPath

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CoachCardTabRow(
    tabs: List<CoachCardTab>,
    selectedTab: CoachCardTab,
    onTabSelected: (CoachCardTab) -> Unit,
) {
    @Suppress("unused")
    val qaId = CoachCardScrollableTabsPath.MEGA_ID
    val index = tabs.indexOf(selectedTab).coerceAtLeast(0)
    ScrollableTabRow(selectedTabIndex = index) {
        tabs.forEach { tab ->
            Tab(
                selected = tab == selectedTab,
                onClick = { onTabSelected(tab) },
                text = { Text(coachTabLabel(tab)) },
            )
        }
    }
}
