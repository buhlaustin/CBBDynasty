package dev.novux.dynasty.cbb.core.coach.contract

import dev.novux.dynasty.cbb.core.coach.card.HeadCoachCard
import dev.novux.dynasty.cbb.core.coach.security.CoachSecuritySnapshot
import dev.novux.dynasty.cbb.core.coach.security.JobSecurityTier
import kotlinx.serialization.Serializable

/** Incoming AD extension proposal ([MEGA-COACH-121]). */
@Serializable
data class HeadCoachExtensionOffer(
    val offerId: String,
    val additionalYears: Int,
    val proposedContractLengthYears: Int,
    val proposedYearsLeft: Int,
    val proposedAnnualSalaryUsd: Long,
)

object HeadCoachExtensionOfferEngine {
    const val MEGA_ID = "MEGA-COACH-121"

    fun incoming(
        card: HeadCoachCard,
        security: CoachSecuritySnapshot?,
        dismissedForSeasonIndex: Int?,
        currentSeasonIndex: Int,
    ): HeadCoachExtensionOffer? {
        if (dismissedForSeasonIndex == currentSeasonIndex) return null
        val trust = security?.adTrust?.score ?: return null
        val tier = security?.jobSecurity?.tier ?: return null
        if (trust < 70 || tier != JobSecurityTier.Secure) return null
        val additionalYears = 2
        val salaryBump = (card.annualSalaryUsd * 108L + 50L) / 100L
        return HeadCoachExtensionOffer(
            offerId = "hc-ext-${card.dynastyId}",
            additionalYears = additionalYears,
            proposedContractLengthYears = card.contractLengthYears + additionalYears,
            proposedYearsLeft = card.contractYearsLeft + additionalYears,
            proposedAnnualSalaryUsd = salaryBump.coerceAtLeast(card.annualSalaryUsd),
        )
    }
}
