package dev.novux.dynasty.cbb.ui.postgame

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.novux.dynasty.cbb.R
import dev.novux.dynasty.cbb.core.injuries.GameSimInjuryEvent
import dev.novux.dynasty.cbb.core.injuries.MedicalRedshirtPolicy
import dev.novux.dynasty.cbb.core.roster.RosterPlayer

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostgameInjurySheet(
    events: List<GameSimInjuryEvent>,
    players: List<RosterPlayer>,
    countsMedicalRedshirt: Boolean = true,
    medicalEligibility: (String) -> MedicalRedshirtPolicy.Eligibility,
    onDismiss: () -> Unit,
    onApplyMedicalRedshirt: (String) -> Unit,
) {
    if (events.isEmpty()) return
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val nameById = players.associate { it.playerId to it.displayLabel() }
    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = stringResource(R.string.postgame_injury_title),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
            )
            if (!countsMedicalRedshirt) {
                Text(
                    text = stringResource(R.string.postgame_injury_exhibition_note),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            events.forEach { event ->
                val name = nameById[event.playerId] ?: event.playerId
                val player = players.firstOrNull { it.playerId == event.playerId }
                Text(
                    text = stringResource(
                        R.string.postgame_injury_line,
                        name,
                        event.label,
                        event.weeksRemaining,
                    ),
                    style = MaterialTheme.typography.bodyLarge,
                )
                player?.let {
                    Text(
                        text = stringResource(
                            R.string.postgame_injury_rs_games_played,
                            it.rsGamesPlayedSeason,
                        ),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                val eligibility = medicalEligibility(event.playerId)
                if (eligibility.eligible) {
                    OutlinedButton(
                        onClick = { onApplyMedicalRedshirt(event.playerId) },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(stringResource(R.string.postgame_medical_redshirt_apply))
                    }
                } else {
                    val ineligibleCopy = when (eligibility.reason) {
                        "exhibition_game" ->
                            stringResource(R.string.postgame_medical_redshirt_exhibition)
                        "too_many_games_played" ->
                            stringResource(
                                R.string.postgame_medical_redshirt_too_many_games,
                                player?.rsGamesPlayedSeason ?: 0,
                            )
                        else -> stringResource(R.string.postgame_medical_redshirt_ineligible)
                    }
                    Text(
                        text = ineligibleCopy,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            Button(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.postgame_injury_dismiss))
            }
        }
    }
}
