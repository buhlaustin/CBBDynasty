package dev.novux.dynasty.cbb.ui.coach

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.novux.dynasty.cbb.R

data class StaffManageTarget(
    val staffId: String,
    val displayName: String,
    val isAssistant: Boolean,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun rememberStaffManageSheetState(): SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StaffManageBottomSheet(
    target: StaffManageTarget?,
    sheetState: SheetState,
    onDismiss: () -> Unit,
    onFire: (StaffManageTarget) -> Unit,
    onHireFromPool: (StaffManageTarget) -> Unit,
    onPoach: (StaffManageTarget) -> Unit,
) {
    val current = target ?: return
    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Column(
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                stringResource(R.string.staff_manage_title),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                stringResource(R.string.staff_manage_for, current.displayName),
                style = MaterialTheme.typography.bodyLarge,
            )
            Button(
                onClick = {
                    onDismiss()
                    onHireFromPool(current)
                },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(stringResource(R.string.staff_manage_hire_pool))
            }
            if (current.isAssistant) {
                OutlinedButton(
                    onClick = {
                        onDismiss()
                        onPoach(current)
                    },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(stringResource(R.string.staff_manage_poach))
                }
            }
            OutlinedButton(
                onClick = {
                    onDismiss()
                    onFire(current)
                },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(stringResource(R.string.staff_manage_fire))
            }
        }
    }
}
