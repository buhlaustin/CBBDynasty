package dev.novux.dynasty.cbb.core.recruiting

/** Post-spend player feedback when relationship meters move ([MEGA-RECR-091]). */
object RecruitSpendFeedbackPolicy {
    const val MEGA_ID: String = "MEGA-RECR-091"

    fun shouldShowInterestGainConfirmation(interestDeltaApplied: Int): Boolean =
        interestDeltaApplied > 0
}