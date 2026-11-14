package dev.novux.dynasty.cbb.ui.newgame

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.novux.dynasty.cbb.R
import dev.novux.dynasty.cbb.core.save.NewGameContractNilPreviewPolicy
import dev.novux.dynasty.cbb.core.save.NewGameDraft
import dev.novux.dynasty.cbb.core.save.NewGameSchoolSummaryPreview
import java.text.NumberFormat
import java.util.Locale

@Composable
fun SignContractStepContent(
    draft: NewGameDraft,
    schoolDisplayName: String,
    contractNilPreview: NewGameSchoolSummaryPreview?,
    signError: SignContractError?,
    signingInProgress: Boolean,
    onSign: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        Text(
            text = stringResource(R.string.new_sign_school_label, schoolDisplayName),
            style = MaterialTheme.typography.bodyLarge,
        )
        Text(
            text = stringResource(R.string.new_sign_coach_label, draft.coach.displayName()),
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(top = 8.dp),
        )
        contractNilPreview?.let { preview ->
            val currency = remember { NumberFormat.getCurrencyInstance(Locale.US) }
            val nilUsd = NewGameContractNilPreviewPolicy.annualNilUsd(preview)
            Text(
                text = stringResource(R.string.new_sign_nil_preview, currency.format(nilUsd)),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 12.dp),
            )
        }
        Text(
            text = draft.coach.displayName(),
            style = MaterialTheme.typography.headlineMedium.copy(
                fontSize = 32.sp,
                fontStyle = FontStyle.Italic,
                fontWeight = FontWeight.SemiBold,
            ),
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 24.dp),
        )
        signError?.let {
            Text(
                text = signErrorMessage(it),
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(bottom = 8.dp),
            )
        }
        Button(
            onClick = onSign,
            enabled = !signingInProgress,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(stringResource(R.string.new_sign_cta))
        }
    }
}

enum class SignContractError {
    IncompleteWizard,
    SlotUnavailable,
}

@Composable
private fun signErrorMessage(error: SignContractError): String = when (error) {
    SignContractError.IncompleteWizard -> stringResource(R.string.new_sign_err_incomplete)
    SignContractError.SlotUnavailable -> stringResource(R.string.new_no_empty_slot)
}
