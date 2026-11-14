package dev.novux.dynasty.cbb.ui.dynasty

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Article
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.People
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.novux.dynasty.cbb.R
import kotlinx.coroutines.launch

data class DynastyShellMenuAction(
    val labelRes: Int,
    val onClick: () -> Unit,
)

/** Shell chrome drawer ([ShellHamburgerDrawerPolicy], [MEGA-SHELL-099]). */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DynastyShellChrome(
    schoolDisplayName: String,
    phaseWeekLabel: String,
    selectedTab: DynastyShellTab,
    onTabSelected: (DynastyShellTab) -> Unit,
    menuActions: List<DynastyShellMenuAction>,
    modifier: Modifier = Modifier,
    headerSubtitle: String? = null,
    content: @Composable (PaddingValues) -> Unit,
) {
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    ModalNavigationDrawer(
        modifier = modifier,
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 20.dp)) {
                    Text(
                        text = stringResource(R.string.shell_drawer_hub_heading),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        text = schoolDisplayName,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 12.dp),
                    )
                    HorizontalDivider(modifier = Modifier.padding(bottom = 8.dp))
                }
                menuActions.forEach { action ->
                    NavigationDrawerItem(
                        label = { Text(stringResource(action.labelRes)) },
                        selected = false,
                        onClick = {
                            scope.launch { drawerState.close() }
                            action.onClick()
                        },
                    )
                }
            }
        },
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    navigationIcon = {
                        IconButton(
                            onClick = { scope.launch { drawerState.open() } },
                        ) {
                            Icon(Icons.Filled.Menu, contentDescription = stringResource(R.string.shell_menu_open))
                        }
                    },
                    title = {
                        Column {
                            Text(
                                text = stringResource(R.string.shell_dynasty_school, schoolDisplayName),
                                style = MaterialTheme.typography.titleMedium,
                                maxLines = 1,
                            )
                            Text(
                                text = phaseWeekLabel,
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.primary,
                                maxLines = 1,
                            )
                            headerSubtitle?.let { subtitle ->
                                Text(
                                    text = subtitle,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 2,
                                )
                            }
                        }
                    },
                )
            },
            bottomBar = {
                NavigationBar(modifier = Modifier.fillMaxWidth()) {
                    NavigationBarItem(
                        selected = selectedTab == DynastyShellTab.Roster,
                        onClick = { onTabSelected(DynastyShellTab.Roster) },
                        icon = { Icon(Icons.Filled.People, null) },
                        label = { Text(stringResource(R.string.shell_tab_roster)) },
                    )
                    NavigationBarItem(
                        selected = selectedTab == DynastyShellTab.Calendar,
                        onClick = { onTabSelected(DynastyShellTab.Calendar) },
                        icon = { Icon(Icons.Filled.CalendarMonth, null) },
                        label = { Text(stringResource(R.string.shell_tab_calendar)) },
                    )
                    NavigationBarItem(
                        selected = selectedTab == DynastyShellTab.Dashboard,
                        onClick = { onTabSelected(DynastyShellTab.Dashboard) },
                        icon = { Icon(Icons.Filled.Home, null) },
                        label = { Text(stringResource(R.string.shell_tab_dashboard)) },
                    )
                    NavigationBarItem(
                        selected = selectedTab == DynastyShellTab.News,
                        onClick = { onTabSelected(DynastyShellTab.News) },
                        icon = { Icon(Icons.Filled.Article, null) },
                        label = { Text(stringResource(R.string.shell_tab_news)) },
                    )
                }
            },
            content = content,
        )
    }
}
