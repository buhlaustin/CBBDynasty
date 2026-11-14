package dev.novux.dynasty.cbb.core.coach.contract

import dev.novux.dynasty.cbb.core.coach.card.HeadCoachCard
import dev.novux.dynasty.cbb.core.coach.security.CoachSecuritySnapshot
import dev.novux.dynasty.cbb.core.coach.security.JobSecurityTier
import kotlin.math.roundToLong

data class HeadCoachContractDetail(
    val annualSalaryUsd: Long,
    val buyoutUsd: Long,
    val performanceGoals: HeadCoachPerformanceGoals,
    val extensionLikely: Boolean,
    val firingRiskCopy: String?,
)

object HeadCoachContractEngine {
    fun derive(
        card: HeadCoachCard,
        security: CoachSecuritySnapshot?,
        programPrestige: Int = 50,
    ): HeadCoachContractDetail {
        val annualSalaryUsd = card.annualSalaryUsd
        val buyoutUsd = card.buyoutUsd
        val goals = HeadCoachPerformanceGoalsEngine.build(programPrestige, card.schoolDisplayName)
        val tier = security?.jobSecurity?.tier
        val trust = security?.adTrust?.score ?: 50
        val extensionLikely = trust >= 70 && tier == JobSecurityTier.Secure
        val firingRiskCopy = when (tier) {
            JobSecurityTier.Critical, JobSecurityTier.HotSeat ->
                "Your seat is hot. Hit performance goals before the AD reviews your deal."
            JobSecurityTier.Watch ->
                "The athletic department is monitoring results this season."
            else -> null
        }
        return HeadCoachContractDetail(
            annualSalaryUsd = annualSalaryUsd,
            buyoutUsd = buyoutUsd,
            performanceGoals = goals,
            extensionLikely = extensionLikely,
            firingRiskCopy = firingRiskCopy,
        )
    }
}
