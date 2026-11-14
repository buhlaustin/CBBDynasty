package dev.novux.dynasty.cbb.ui.postgame

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import dev.novux.dynasty.cbb.R
import dev.novux.dynasty.cbb.core.balance.BalanceMedicalRedshirtRules
import dev.novux.dynasty.cbb.core.injuries.MedicalRedshirtPolicy
import dev.novux.dynasty.cbb.core.roster.RosterPlayer

@Composable
fun MedicalRedshirtConfirmDialog(
    player: RosterPlayer,
    medicalRules: BalanceMedicalRedshirtRules,
    onConfirm: () -> Unit,
    onCancel: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onCancel,
        title = {
            Text(
                stringResource(
                    R.string.postgame_medical_redshirt_confirm_title,
                    player.displayLabel(),
                ),
            )
        },
        text = {
            Text(
                stringResource(
                    R.string.postgame_medical_redshirt_confirm_body,
                    MedicalRedshirtPolicy.confirmationDetail(player, medicalRules),
                    player.injuryLabel.orEmpty(),
                ),
                style = MaterialTheme.typography.bodyMedium,
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(stringResource(R.string.postgame_medical_redshirt_confirm_apply))
            }
        },
        dismissButton = {
            TextButton(onClick = onCancel) {
                Text(stringResource(R.string.postgame_medical_redshirt_confirm_cancel))
            }
        },
    )
}
