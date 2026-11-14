package dev.novux.dynasty.cbb.ui.recruiting

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.novux.dynasty.cbb.R
import dev.novux.dynasty.cbb.core.player.loyalty.CoachPromise
import dev.novux.dynasty.cbb.core.player.loyalty.CoachPromiseType
import dev.novux.dynasty.cbb.core.recruiting.RecruitOfferPromisePolicy

@Suppress("unused")
private val recruitPromisePickerQaAnchor: String = RecruitOfferPromisePolicy.QA_ID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecruitPromisePickerSheet(
    prospectDisplayName: String,
    onDismiss: () -> Unit,
    onConfirm: (CoachPromiseType) -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var selected by remember { mutableStateOf(RecruitOfferPromisePolicy.OFFERABLE_TYPES.first()) }
    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                stringResource(R.string.recruiting_promise_picker_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                stringResource(R.string.recruiting_promise_picker_subtitle, prospectDisplayName),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                RecruitOfferPromisePolicy.OFFERABLE_TYPES.forEach { type ->
                    val label = CoachPromise(
                        type = type,
                        stayTopN = CoachPromise.DEFAULT_STAY_TOP_N,
                    ).displayLabel()
                    RowChoice(
                        label = label,
                        selected = selected == type,
                        onSelect = { selected = type },
                    )
                }
            }
            TextButton(
                onClick = { onConfirm(selected) },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(stringResource(R.string.recruiting_promise_picker_confirm))
            }
        }
    }
}

@Composable
private fun RowChoice(
    label: String,
    selected: Boolean,
    onSelect: () -> Unit,
) {
    androidx.compose.foundation.layout.Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onSelect)
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        RadioButton(selected = selected, onClick = onSelect)
        Text(label, style = MaterialTheme.typography.bodyLarge)
    }
}
