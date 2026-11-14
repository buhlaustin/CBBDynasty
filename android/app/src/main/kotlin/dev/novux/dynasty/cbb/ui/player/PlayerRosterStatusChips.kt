package dev.novux.dynasty.cbb.ui.player

import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AssistChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import dev.novux.dynasty.cbb.R
import dev.novux.dynasty.cbb.core.player.DynastyPlayer
import dev.novux.dynasty.cbb.core.player.card.PlayerRosterStatusCardPolicy

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PlayerRosterStatusChips(
    player: DynastyPlayer,
    modifier: Modifier = Modifier,
) {
    @Suppress("unused")
    val qaId = PlayerRosterStatusCardPolicy.MEGA_ID
    val chips = PlayerRosterStatusCardPolicy.visibleChips(player)
    if (chips.isEmpty()) return
    FlowRow(
        modifier = modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        chips.forEach { chip ->
            val labelRes = when (chip) {
                PlayerRosterStatusCardPolicy.ChipKind.TransferPortal ->
                    R.string.player_status_transfer_portal
                PlayerRosterStatusCardPolicy.ChipKind.DeclaredForDraft ->
                    R.string.player_status_declared_draft
                PlayerRosterStatusCardPolicy.ChipKind.ProDayComplete ->
                    R.string.player_status_pro_day_complete
                PlayerRosterStatusCardPolicy.ChipKind.DraftStock ->
                    R.string.player_status_draft_stock
            }
            AssistChip(
                onClick = {},
                label = {
                    Text(
                        when (chip) {
                            PlayerRosterStatusCardPolicy.ChipKind.DraftStock ->
                                stringResource(labelRes, player.role.draftStockPoints)
                            else -> stringResource(labelRes)
                        },
                        style = MaterialTheme.typography.labelMedium,
                    )
                },
            )
        }
    }
}