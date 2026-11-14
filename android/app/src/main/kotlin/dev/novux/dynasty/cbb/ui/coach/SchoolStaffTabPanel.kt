package dev.novux.dynasty.cbb.ui.coach

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.novux.dynasty.cbb.R
import dev.novux.dynasty.cbb.core.coach.card.CoachCardPolicy
import dev.novux.dynasty.cbb.core.coach.security.CoachSecuritySnapshot
import dev.novux.dynasty.cbb.core.coach.staff.SchoolStaffRoster
import dev.novux.dynasty.cbb.core.coach.staff.StaffFourthAssistantSlotPolicy
import dev.novux.dynasty.cbb.core.coach.staff.StaffKind
import dev.novux.dynasty.cbb.ui.portrait.CoachPortraitSlot

@Composable
fun SchoolStaffTabPanel(
    roster: SchoolStaffRoster,
    adTrust: CoachSecuritySnapshot? = null,
    onHeadCoachClick: () -> Unit,
    onAssistantClick: (String) -> Unit,
    userCanManage: Boolean = false,
    onManageStaff: (StaffManageTarget) -> Unit = {},
    programPrestige: Int? = null,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        adTrust?.let { AdTrustMeter(it) }
        Text(
            stringResource(R.string.staff_tab_title),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
        )
        Text(
            stringResource(
                if (userCanManage) R.string.staff_tab_hint_manage else R.string.staff_tab_hint,
            ),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        StaffRow(
            title = roster.headCoach.displayName,
            subtitle = stringResource(R.string.coach_card_role_head),
            meta = roster.headCoach.seasonRecordLabel,
            avatarPresetId = roster.headCoach.avatarPresetId,
            clickable = true,
            onClick = onHeadCoachClick,
        )
        Text(
            stringResource(R.string.staff_assistants_header),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Medium,
        )
        programPrestige?.let { prestige ->
            @Suppress("unused")
            val qaSlots = StaffFourthAssistantSlotPolicy.MEGA_ID
            Text(
                stringResource(
                    R.string.staff_assistant_slot_policy,
                    StaffFourthAssistantSlotPolicy.maxAssistantSlots(prestige),
                ),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            if (StaffFourthAssistantSlotPolicy.showFourthSlotPlaceholder(prestige, roster.assistants.size)) {
                Text(
                    stringResource(R.string.staff_fourth_slot_open),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                )
            } else if (!StaffFourthAssistantSlotPolicy.fourthSlotUnlocked(prestige)) {
                Text(
                    stringResource(
                        R.string.staff_fourth_slot_locked,
                        StaffFourthAssistantSlotPolicy.FOURTH_SLOT_PRESTIGE_THRESHOLD,
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        roster.assistants.forEach { assistant ->
            StaffRow(
                title = assistant.displayName,
                subtitle = assistant.role.label,
                meta = stringResource(
                    R.string.staff_rating_chip,
                    assistant.ratings.recruiting,
                ),
                avatarPresetId = assistant.avatarPresetId,
                clickable = CoachCardPolicy.eligibleForCoachCard(StaffKind.CoachTrack),
                onClick = { onAssistantClick(assistant.staffId) },
                manageLabel = if (userCanManage) stringResource(R.string.staff_manage_cta) else null,
                onManage = {
                    onManageStaff(
                        StaffManageTarget(
                            staffId = assistant.staffId,
                            displayName = assistant.displayName,
                            isAssistant = true,
                        ),
                    )
                },
            )
        }
        Text(
            stringResource(R.string.staff_support_header),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Medium,
        )
        roster.supportStaff.forEach { member ->
            StaffRow(
                title = member.displayName,
                subtitle = member.role.label,
                meta = stringResource(R.string.staff_rating_chip, member.contributionRating),
                clickable = false,
                onClick = {},
                manageLabel = if (userCanManage) stringResource(R.string.staff_manage_cta) else null,
                onManage = {
                    onManageStaff(
                        StaffManageTarget(
                            staffId = member.staffId,
                            displayName = member.displayName,
                            isAssistant = false,
                        ),
                    )
                },
            )
        }
        Text(
            stringResource(R.string.staff_support_no_card),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun StaffRow(
    title: String,
    subtitle: String,
    meta: String,
    avatarPresetId: String = "",
    clickable: Boolean,
    onClick: () -> Unit,
    manageLabel: String? = null,
    onManage: () -> Unit = {},
) {
    val colors = if (clickable) {
        CardDefaults.cardColors()
    } else {
        CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (clickable) Modifier.clickable(onClick = onClick) else Modifier),
        colors = colors,
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            if (clickable) {
                CoachPortraitSlot(displayName = title, avatarPresetId = avatarPresetId)
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.Medium)
                Text(subtitle, style = MaterialTheme.typography.bodySmall)
                Text(meta, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
            }
            manageLabel?.let { label ->
                OutlinedButton(onClick = onManage) {
                    Text(label)
                }
            }
        }
    }
}
