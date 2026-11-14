package dev.novux.dynasty.cbb.ui.player

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.novux.dynasty.cbb.R
import dev.novux.dynasty.cbb.core.player.DynastyPlayer
import dev.novux.dynasty.cbb.core.player.history.PlayerPriorSeasonSnapshot
import dev.novux.dynasty.cbb.core.player.games.PlayerGameLogEntry
import dev.novux.dynasty.cbb.core.player.stats.PlayerStatsSummary
import dev.novux.dynasty.cbb.core.school.SchoolHubOverview
import dev.novux.dynasty.cbb.data.player.PlayerStatsRepository
import dev.novux.dynasty.cbb.ui.coach.CoachOverviewTeamCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayerCardScreen(
    player: DynastyPlayer,
    schoolDisplayName: String,
    seasonRecordLabel: String,
    seasonIndex: Int,
    playerStatsRepository: PlayerStatsRepository,
    allowEdit: Boolean,
    schoolHubOverview: SchoolHubOverview? = null,
    onOpenSchoolHub: (() -> Unit)? = null,
    onBack: () -> Unit,
    nationalTop60RankByPlayerId: Map<String, Int> = emptyMap(),
    modifier: Modifier = Modifier,
) {
    var selectedTab by remember { mutableStateOf(PlayerCardTab.Overview) }
    var showEditHint by remember { mutableStateOf(false) }
    var boxScoreEntry by remember { mutableStateOf<PlayerGameLogEntry?>(null) }
    val tabs = PlayerCardTab.ORDER

    Column(modifier = modifier.fillMaxSize()) {
        PlayerCardPersistentHeader(
            displayName = player.identity.displayName,
            allowEdit = allowEdit,
            onBack = onBack,
            onEdit = { showEditHint = true },
        )
        PlayerCardIdentityStrip(player = player)
        PrimaryTabRow(selectedTabIndex = tabs.indexOf(selectedTab)) {
            tabs.forEach { tab ->
                Tab(
                    selected = tab == selectedTab,
                    onClick = { selectedTab = tab },
                    text = { Text(playerTabLabel(tab)) },
                )
            }
        }
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            when (selectedTab) {
                PlayerCardTab.Overview -> PlayerCardOverviewTab(
                    player = player,
                    schoolDisplayName = schoolDisplayName,
                    seasonRecordLabel = seasonRecordLabel,
                    schoolHubOverview = schoolHubOverview,
                    onOpenSchoolHub = onOpenSchoolHub,
                    nationalTop60RankByPlayerId = nationalTop60RankByPlayerId,
                )
                PlayerCardTab.Ratings -> PlayerCardRatingsTabContent(player = player)
                PlayerCardTab.Stats -> PlayerCardStatsTab(
                    player = player,
                    dynastyId = player.dynastyId,
                    seasonIndex = seasonIndex,
                    playerStatsRepository = playerStatsRepository,
                )
                PlayerCardTab.Games -> PlayerCardGamesTab(
                    player = player,
                    dynastyId = player.dynastyId,
                    seasonIndex = seasonIndex,
                    playerStatsRepository = playerStatsRepository,
                    onOpenBoxScore = { boxScoreEntry = it },
                )
                PlayerCardTab.History -> PlayerCardHistoryTab(
                    player = player,
                    dynastyId = player.dynastyId,
                    seasonIndex = seasonIndex,
                    schoolDisplayName = schoolDisplayName,
                    playerStatsRepository = playerStatsRepository,
                )
            }
        }
    }

    PlayerGameBoxScoreSheet(
        player = player,
        entry = boxScoreEntry,
        onDismiss = { boxScoreEntry = null },
    )

    if (showEditHint) {
        AlertDialog(
            onDismissRequest = { showEditHint = false },
            title = { Text(stringResource(R.string.player_card_edit)) },
            text = { Text(stringResource(R.string.player_card_edit_hint)) },
            confirmButton = {
                TextButton(onClick = { showEditHint = false }) {
                    Text(stringResource(R.string.player_card_edit_ack))
                }
            },
        )
    }
}

@Composable
private fun playerTabLabel(tab: PlayerCardTab): String = when (tab) {
    PlayerCardTab.Overview -> stringResource(R.string.player_card_overview)
    PlayerCardTab.Ratings -> stringResource(R.string.player_card_ratings_tab)
    PlayerCardTab.Stats -> stringResource(R.string.player_card_stats_tab)
    PlayerCardTab.Games -> stringResource(R.string.player_card_games_tab)
    PlayerCardTab.History -> stringResource(R.string.player_card_history_tab)
}

@Composable
private fun PlayerCardOverviewTab(
    player: DynastyPlayer,
    schoolDisplayName: String,
    seasonRecordLabel: String,
    schoolHubOverview: SchoolHubOverview?,
    onOpenSchoolHub: (() -> Unit)?,
    nationalTop60RankByPlayerId: Map<String, Int> = emptyMap(),
) {
    CoachOverviewTeamCard(
        schoolDisplayName = schoolDisplayName,
        seasonRecordLabel = seasonRecordLabel,
        schoolHubOverview = schoolHubOverview,
        onOpenSchoolHub = onOpenSchoolHub,
    )
    PlayerCardOverviewContent(
        player = player,
        nationalTop60RankByPlayerId = nationalTop60RankByPlayerId,
    )
}

@Composable
private fun PlayerCardStatsTab(
    player: DynastyPlayer,
    dynastyId: String,
    seasonIndex: Int,
    playerStatsRepository: PlayerStatsRepository,
) {
    var summary by remember(player.playerId) { mutableStateOf<PlayerStatsSummary?>(null) }
    LaunchedEffect(player.playerId, player.role.rsGamesPlayedSeason, seasonIndex) {
        summary = playerStatsRepository.summaryFor(dynastyId, seasonIndex, player)
    }
    Text(
        stringResource(R.string.player_card_stats_tab),
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold,
    )
    Text(
        stringResource(R.string.player_card_rs_games_played, player.role.rsGamesPlayedSeason),
        style = MaterialTheme.typography.bodyLarge,
    )
    summary?.let { PlayerStatsFourCards(summary = it) }
        ?: Text(
            stringResource(R.string.player_card_stats_loading),
            style = MaterialTheme.typography.bodyMedium,
        )
}

@Composable
private fun PlayerCardGamesTab(
    player: DynastyPlayer,
    dynastyId: String,
    seasonIndex: Int,
    playerStatsRepository: PlayerStatsRepository,
    onOpenBoxScore: (PlayerGameLogEntry) -> Unit,
) {
    var entries by remember(player.playerId) { mutableStateOf<List<PlayerGameLogEntry>?>(null) }
    LaunchedEffect(player.playerId, player.role.rsGamesPlayedSeason, seasonIndex) {
        entries = playerStatsRepository.gameLogFor(dynastyId, seasonIndex, player)
    }
    Text(
        stringResource(R.string.player_card_games_tab),
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold,
    )
    val log = entries
    when {
        log == null -> Text(
            stringResource(R.string.player_card_stats_loading),
            style = MaterialTheme.typography.bodyMedium,
        )
        log.isEmpty() -> Text(
            stringResource(R.string.player_card_games_empty),
            style = MaterialTheme.typography.bodyMedium,
        )
        else -> PlayerGameLogPanel(entries = log, onEntryClick = onOpenBoxScore)
    }
}

@Composable
private fun PlayerCardHistoryTab(
    player: DynastyPlayer,
    dynastyId: String,
    seasonIndex: Int,
    schoolDisplayName: String,
    playerStatsRepository: PlayerStatsRepository,
) {
    var priorSeasons by remember(player.playerId) { mutableStateOf<List<PlayerPriorSeasonSnapshot>?>(null) }
    LaunchedEffect(player.playerId, seasonIndex) {
        priorSeasons = playerStatsRepository.priorSeasonsForPlayer(
            dynastyId = dynastyId,
            playerId = player.playerId,
            currentSeasonIndex = seasonIndex,
            currentSchoolDisplayName = schoolDisplayName,
        )
    }
    Text(
        stringResource(R.string.player_card_history_tab),
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold,
    )
    Text(
        stringResource(
            R.string.player_card_history_eligibility,
            player.eligibility.yearsUsed,
            player.eligibility.yearsRemaining,
        ),
        style = MaterialTheme.typography.bodyMedium,
    )
    PlayerPromiseHistoryPanel(player = player, currentSeasonIndex = seasonIndex)
    when (val seasons = priorSeasons) {
        null -> Text(
            stringResource(R.string.player_card_stats_loading),
            style = MaterialTheme.typography.bodyMedium,
        )
        else -> if (seasons.isEmpty()) {
            Text(
                stringResource(R.string.player_card_history_pipeline),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        } else {
            PlayerPriorSeasonList(
                seasons = seasons,
                currentSchoolDisplayName = schoolDisplayName,
            )
        }
    }
}
