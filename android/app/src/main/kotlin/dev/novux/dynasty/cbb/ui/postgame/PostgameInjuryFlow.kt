package dev.novux.dynasty.cbb.ui.postgame

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import dev.novux.dynasty.cbb.core.balance.BalanceMedicalRedshirtRules
import dev.novux.dynasty.cbb.core.injuries.GameSimInjuryEvent
import dev.novux.dynasty.cbb.core.injuries.MedicalRedshirtPolicy
import dev.novux.dynasty.cbb.core.roster.RosterPlayer

/**
 * Shows postgame injury bottom sheet + medical redshirt confirmation when [events] is non-empty.
 */
@Composable
fun PostgameInjuryFlow(
    events: List<GameSimInjuryEvent>,
    players: List<RosterPlayer>,
    rsWeekIndex: Int,
    medicalRules: BalanceMedicalRedshirtRules,
    countsMedicalRedshirt: Boolean = true,
    onDismiss: () -> Unit,
    onApplyMedicalRedshirt: (String) -> Unit,
) {
    if (events.isEmpty()) return
    var confirmPlayer by remember(events) { mutableStateOf<RosterPlayer?>(null) }
    PostgameInjurySheet(
        events = events,
        players = players,
        countsMedicalRedshirt = countsMedicalRedshirt,
        medicalEligibility = { playerId ->
            val player = players.firstOrNull { it.playerId == playerId }
                ?: return@PostgameInjurySheet MedicalRedshirtPolicy.Eligibility(false, "player_not_found")
            MedicalRedshirtPolicy.evaluate(
                player,
                rsWeekIndex,
                medicalRules,
                countsTowardMedicalRedshirt = countsMedicalRedshirt,
            )
        },
        onDismiss = onDismiss,
        onApplyMedicalRedshirt = { playerId ->
            confirmPlayer = players.firstOrNull { it.playerId == playerId }
        },
    )
    confirmPlayer?.let { player ->
        MedicalRedshirtConfirmDialog(
            player = player,
            medicalRules = medicalRules,
            onConfirm = {
                onApplyMedicalRedshirt(player.playerId)
                confirmPlayer = null
            },
            onCancel = { confirmPlayer = null },
        )
    }
}
