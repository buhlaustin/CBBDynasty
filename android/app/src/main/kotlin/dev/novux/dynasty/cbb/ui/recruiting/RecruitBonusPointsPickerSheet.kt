package dev.novux.dynasty.cbb.ui.recruiting

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import dev.novux.dynasty.cbb.R
import dev.novux.dynasty.cbb.core.recruiting.RecruitingBonusPointsRemainderPolicy

@Suppress("unused")
private val recruitBonusPointsPickerQaAnchor: String = RecruitingBonusPointsRemainderPolicy.QA_ID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecruitBonusPointsPickerSheet(
    prospectDisplayName: String,
    pointsAvailable: Int,
    onDismiss: () -> Unit,
    onConfirm: (Int) -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var amountText by remember(pointsAvailable) { mutableStateOf("") }
    val parsedAmount = remember(amountText, pointsAvailable) {
        RecruitingBonusPointsRemainderPolicy.parseUserInput(amountText, pointsAvailable)
    }
    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                stringResource(R.string.recruiting_bonus_picker_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                stringResource(R.string.recruiting_bonus_picker_subtitle, prospectDisplayName, pointsAvailable),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            OutlinedTextField(
                value = amountText,
                onValueChange = { next -> amountText = next.filter { it.isDigit() }.take(6) },
                modifier = Modifier.fillMaxWidth(),
                label = { Text(stringResource(R.string.recruiting_bonus_picker_field_label)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
            )
            OutlinedButton(
                onClick = { amountText = pointsAvailable.toString() },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(stringResource(R.string.recruiting_bonus_picker_spend_all, pointsAvailable))
            }
            TextButton(
                onClick = { parsedAmount?.let(onConfirm) },
                enabled = parsedAmount != null,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(stringResource(R.string.recruiting_bonus_picker_confirm))
            }
        }
    }
}
