package dev.novux.dynasty.cbb.ui.player

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.novux.dynasty.cbb.R
import dev.novux.dynasty.cbb.core.player.DynastyPlayer
import dev.novux.dynasty.cbb.core.player.PlayerCardMultiTabPolicy
import dev.novux.dynasty.cbb.core.player.scout.ScoutDepth
import dev.novux.dynasty.cbb.ui.portrait.PlayerPortraitSlot

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayerCardPreviewSheet(
    player: DynastyPlayer?,
    onDismiss: () -> Unit,
    onViewMore: (DynastyPlayer) -> Unit,
    allowEdit: Boolean = true,
    nationalTop60RankByPlayerId: Map<String, Int> = emptyMap(),
) {
    if (player == null) return
    var showEditHint by remember(player.playerId) { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Column(
            modifier = Modifier
                .fillMaxWidth(),
        ) {
            PlayerCardPersistentHeader(
                displayName = player.identity.displayName,
                allowEdit = allowEdit,
                onBack = onDismiss,
                onEdit = { showEditHint = true },
            )
            Column(modifier = Modifier.padding(horizontal = 20.dp)) {
            Text(
                stringResource(R.string.player_card_preview_title),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            PlayerPortraitSlot(
                displayName = player.identity.displayName,
                avatarPresetId = player.appearance.avatarPresetId,
                playerId = player.playerId,
            )
            PlayerCardOverviewContent(
                player = player,
                modifier = Modifier.padding(bottom = 8.dp),
                compact = true,
                nationalTop60RankByPlayerId = nationalTop60RankByPlayerId,
            )
            Text(
                text = stringResource(
                    R.string.player_card_multi_tab_hint,
                    PlayerCardMultiTabPolicy.tabCount,
                ),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 8.dp),
            )
            Button(
                onClick = {
                    onDismiss()
                    onViewMore(player)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
            ) {
                Text(stringResource(R.string.coach_preview_view_more))
            }
            }
        }
    }
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

/** @deprecated Use [PlayerCardPreviewSheet]; kept as alias for callers during migration. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayerCardSheet(
    player: DynastyPlayer?,
    onDismiss: () -> Unit,
) = PlayerCardPreviewSheet(player = player, onDismiss = onDismiss, onViewMore = { })

@Composable
fun PlayerCardOverviewContent(
    player: DynastyPlayer,
    modifier: Modifier = Modifier,
    compact: Boolean = false,
    nationalTop60RankByPlayerId: Map<String, Int> = emptyMap(),
) {
    val report = player.scoutReport(ScoutDepth.Light)
    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text(
            text = player.identity.displayName,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
        )
        PlayerCardIdentityStripChips(player = player)
        PlayerRosterStatusChips(player = player)
        PlayerEligibilityStatusLines(player = player)
        Text(
            text = player.archetype.displayName,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        PlayerLoyaltyChips(player)
        HorizontalDivider()
        Text(
            text = stringResource(R.string.player_card_overview),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
        )
        Text(
            text = stringResource(
                R.string.player_card_hometown,
                player.identity.hometownCity,
                player.identity.hometownState,
            ),
            style = MaterialTheme.typography.bodyMedium,
        )
        Text(
            text = stringResource(
                R.string.player_card_measurements,
                player.identity.heightInches / 12,
                player.identity.heightInches % 12,
                player.identity.weightLbs,
            ),
            style = MaterialTheme.typography.bodyMedium,
        )
        PlayerNilValuationLine(player = player, compact = compact)
        PlayerDraftProjectionLine(
            player = player,
            compact = compact,
            nationalTop60RankByPlayerId = nationalTop60RankByPlayerId,
        )
        if (!compact) {
            HorizontalDivider()
            Text(
                text = stringResource(R.string.player_card_ratings_tab),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
        }
        if (compact) {
            Text(
                text = stringResource(
                    R.string.player_card_headline_ratings,
                    player.ratings.ovr,
                    player.ratings.off,
                    player.ratings.def,
                ),
                style = MaterialTheme.typography.bodyLarge,
            )
        } else {
            PlayerRatingAttributesPanel(ratings = player.ratings)
            PlayerLoyaltyRatingsRow(player)
        }
        Text(
            text = stringResource(R.string.player_card_scout_ovr, report.overall),
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}
