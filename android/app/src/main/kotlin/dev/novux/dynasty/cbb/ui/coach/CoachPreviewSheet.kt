package dev.novux.dynasty.cbb.ui.coach

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import java.text.NumberFormat
import java.util.Locale
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.novux.dynasty.cbb.R
import dev.novux.dynasty.cbb.core.coach.card.AssistantCoachCard
import dev.novux.dynasty.cbb.core.coach.card.HeadCoachCard
import dev.novux.dynasty.cbb.core.coach.security.CoachSecuritySnapshot

/**
 * Quick coach info sheet ([MEGA-COACH-018], [MEGA-COACH-188]): text-only preview before full card; no coach portrait ship.
 * Shell entry: [dev.novux.dynasty.cbb.ui.shell.DynastyShellScreen] → [CoachPreviewBottomSheet] → View more.
 */
object CoachPreviewSheet {
    const val QA_ID: String = "MEGA-COACH-018"
    const val JOB_SECURITY_CHIP_QA: String = "MEGA-COACH-188"
    const val VIEW_MORE_ROUTE_QA: String = "MEGA-COACH-189"
}

sealed class CoachPreviewTarget {
    data class HeadCoach(
        val card: HeadCoachCard,
    ) : CoachPreviewTarget()

    data class Assistant(
        val card: AssistantCoachCard,
    ) : CoachPreviewTarget()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CoachPreviewBottomSheet(
    target: CoachPreviewTarget?,
    userCoachSecurity: CoachSecuritySnapshot?,
    sheetState: SheetState,
    onDismiss: () -> Unit,
    onViewMore: (CoachPreviewTarget) -> Unit,
) {
    val current = target ?: return
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
    ) {
        CoachPreviewContent(
            target = current,
            userCoachSecurity = userCoachSecurity,
            onViewMore = { preview ->
                onDismiss()
                onViewMore(preview)
            },
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
        )
    }
}

@Composable
fun CoachPreviewContent(
    target: CoachPreviewTarget,
    userCoachSecurity: CoachSecuritySnapshot?,
    onViewMore: (CoachPreviewTarget) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            stringResource(R.string.coach_preview_title),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        when (target) {
            is CoachPreviewTarget.HeadCoach -> HeadCoachPreviewBody(
                card = target.card,
                userCoachSecurity = userCoachSecurity,
            )
            is CoachPreviewTarget.Assistant -> AssistantCoachPreviewBody(card = target.card)
        }
        Button(
            onClick = { onViewMore(target) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp, bottom = 24.dp),
        ) {
            Text(stringResource(R.string.coach_preview_view_more))
        }
    }
}

@Composable
private fun HeadCoachPreviewBody(
    card: HeadCoachCard,
    userCoachSecurity: CoachSecuritySnapshot?,
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(card.displayName, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Text(
            stringResource(R.string.coach_card_role_head),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary,
        )
        Text(card.schoolDisplayName, style = MaterialTheme.typography.titleLarge)
        Text(
            stringResource(R.string.coach_card_age, card.age),
            style = MaterialTheme.typography.bodyMedium,
        )
        card.almaMaterLabel?.takeIf { it.isNotBlank() }?.let { alma ->
            Text(
                stringResource(R.string.coach_alma_mater, alma),
                style = MaterialTheme.typography.bodyMedium,
            )
        }
        Text(
            stringResource(R.string.coach_card_record, card.seasonRecordLabel),
            style = MaterialTheme.typography.bodyLarge,
        )
        val money = remember { NumberFormat.getCurrencyInstance(Locale.US) }
        Text(
            stringResource(R.string.coach_contract_salary, money.format(card.annualSalaryUsd)),
            style = MaterialTheme.typography.bodyMedium,
        )
        userCoachSecurity?.let { JobSecurityTierChip(it) }
        Text(
            stringResource(
                R.string.coach_preview_headline_ratings,
                card.ratings.offensive,
                card.ratings.defensive,
                card.ratings.recruiting,
            ),
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}

@Composable
private fun AssistantCoachPreviewBody(card: AssistantCoachCard) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(card.displayName, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Text(card.role.label, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
        Text(card.schoolDisplayName, style = MaterialTheme.typography.titleLarge)
        Text(
            stringResource(R.string.coach_card_record, card.seasonRecordLabel),
            style = MaterialTheme.typography.bodyLarge,
        )
        Text(
            stringResource(
                R.string.coach_preview_assistant_ratings,
                card.ratings.recruiting,
                card.ratings.development,
                card.ratings.scouting,
            ),
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun rememberCoachPreviewSheetState(): SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
