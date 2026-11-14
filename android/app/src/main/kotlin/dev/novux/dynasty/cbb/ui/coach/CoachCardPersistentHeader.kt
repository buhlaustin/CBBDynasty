package dev.novux.dynasty.cbb.ui.coach

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import dev.novux.dynasty.cbb.R
import dev.novux.dynasty.cbb.core.coach.card.CoachCardPersistentHeaderPolicy

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CoachCardPersistentHeader(
    displayName: String,
    allowEdit: Boolean,
    onBack: () -> Unit,
    onEdit: () -> Unit,
    modifier: Modifier = Modifier,
) {
    @Suppress("unused")
    val qaId = CoachCardPersistentHeaderPolicy.MEGA_ID
    TopAppBar(
        modifier = modifier,
        title = { Text(displayName, maxLines = 1) },
        navigationIcon = {
            TextButton(onClick = onBack) {
                Text(stringResource(R.string.coach_card_back))
            }
        },
        actions = {
            if (allowEdit) {
                TextButton(onClick = onEdit) {
                    Text(stringResource(R.string.coach_card_edit))
                }
            }
        },
    )
}
