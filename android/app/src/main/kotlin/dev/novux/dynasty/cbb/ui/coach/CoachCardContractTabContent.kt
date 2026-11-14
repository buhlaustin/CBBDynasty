package dev.novux.dynasty.cbb.ui.coach

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import dev.novux.dynasty.cbb.R
import dev.novux.dynasty.cbb.core.coach.card.AssistantCoachCard
import dev.novux.dynasty.cbb.core.coach.card.HeadCoachCard
import dev.novux.dynasty.cbb.core.coach.contract.HeadCoachContractDetail
import dev.novux.dynasty.cbb.core.coach.contract.HeadCoachExtensionOffer
import dev.novux.dynasty.cbb.core.coach.employment.CoachEmploymentStatus
import dev.novux.dynasty.cbb.core.coach.security.CoachSecuritySnapshot
import dev.novux.dynasty.cbb.core.coach.security.JobSecurityTier
@Composable
fun HeadCoachContractTab(
    card: HeadCoachCard,
    contractDetail: HeadCoachContractDetail,
    userCoachSecurity: CoachSecuritySnapshot?,
    employmentStatus: CoachEmploymentStatus,
    isUserCoach: Boolean = true,
    onViewAdTrustOnStaff: () -> Unit,
    onResignTap: () -> Unit,
    onTerminationTap: () -> Unit,
    onOpenJobCenter: () -> Unit,
    extensionOffer: HeadCoachExtensionOffer? = null,
    onAcceptExtension: () -> Unit = {},
    onDeclineExtension: () -> Unit = {},
) {
    val firedRisk = userCoachSecurity?.jobSecurity?.tier in setOf(
        JobSecurityTier.Critical,
        JobSecurityTier.HotSeat,
    )
    CoachCardContractTabShell(
        header = {
            userCoachSecurity?.let { JobSecurityMeter(it) }
            contractDetail.firingRiskCopy?.let {
                Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
            }
            when {
                extensionOffer != null -> CoachContractExtensionOfferCard(
                    offer = extensionOffer,
                    onAccept = onAcceptExtension,
                    onDecline = onDeclineExtension,
                )
                contractDetail.extensionLikely -> {
                    Text(
                        stringResource(R.string.coach_contract_extension_likely),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
            }
        },
        termsBlock = {
            CoachContractTabTermsBlock(
                annualSalaryUsd = card.annualSalaryUsd,
                buyoutUsd = card.buyoutUsd,
                contractLengthYears = card.contractLengthYears,
                contractYearsLeft = card.contractYearsLeft,
            )
        },
        goalsBlock = {
            CoachContractTabGoalsBlock(contractDetail.performanceGoals.displayLines())
        },
        footer = {
            if (isUserCoach) {
                userCoachSecurity?.let {
                    AdTrustMeter(it)
                    OutlinedButton(onClick = onViewAdTrustOnStaff, modifier = Modifier.fillMaxWidth()) {
                        Text(stringResource(R.string.coach_contract_view_staff_ad_trust))
                    }
                }
            }
            if (!isUserCoach) {
                Text(
                    stringResource(R.string.coach_contract_read_only_opponent),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else if (employmentStatus == CoachEmploymentStatus.Unemployed) {
                Text(
                    stringResource(R.string.coach_contract_unemployed_notice),
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                )
                OutlinedButton(onClick = onOpenJobCenter, modifier = Modifier.fillMaxWidth()) {
                    Text(stringResource(R.string.coach_contract_open_job_center))
                }
            } else {
                if (firedRisk) {
                    OutlinedButton(onClick = onTerminationTap, modifier = Modifier.fillMaxWidth()) {
                        Text(stringResource(R.string.coach_contract_termination_cta))
                    }
                }
                OutlinedButton(onClick = onResignTap, modifier = Modifier.fillMaxWidth()) {
                    Text(stringResource(R.string.coach_contract_resign_cta))
                }
            }
        },
    )
}

@Composable
fun AssistantCoachContractTab(card: AssistantCoachCard) {
    CoachCardContractTabShell(
        termsBlock = {
            if (card.annualSalaryUsd > 0) {
                CoachContractTabTermsBlock(
                    annualSalaryUsd = card.annualSalaryUsd,
                    buyoutUsd = card.buyoutUsd,
                    contractLengthYears = card.contractLengthYears,
                    contractYearsLeft = card.contractYearsLeft,
                )
            } else {
                Text(
                    stringResource(R.string.coach_assistant_contract_note),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        },
        footer = {
            if (card.annualSalaryUsd > 0) {
                Text(
                    stringResource(R.string.coach_assistant_contract_note),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        },
    )
}
