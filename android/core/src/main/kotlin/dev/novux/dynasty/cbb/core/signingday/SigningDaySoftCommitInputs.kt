package dev.novux.dynasty.cbb.core.signingday

import dev.novux.dynasty.cbb.core.balance.BalanceRecruitingRecommended
import dev.novux.dynasty.cbb.core.recruiting.SchoolRecruitingContext

/** Dynasty-scoped inputs for influence-board soft commits ([MEGA-RECR-029]). */
data class SigningDaySoftCommitInputs(
    val userSchool: SchoolRecruitingContext,
    val userProgramId: String,
    val recommended: BalanceRecruitingRecommended,
    val spendDeltaByProspect: Map<String, Int> = emptyMap(),
)
