package dev.novux.dynasty.cbb.core.recruiting

/**
 * When a NIL offer is on the row, sign-likelihood badge tracks live interest (tops at full ask)
 * ([MEGA-RECR-097]).
 */
object RecruitNilInterestBadgeRefreshPolicy {
    const val MEGA_ID: String = "MEGA-RECR-097"

    fun badgeScoreForRow(
        adjustedBaselineScore: Double,
        liveInterest: Int,
        nilOfferUsd: Int?,
    ): Double = if (nilOfferUsd != null && nilOfferUsd > 0) {
        liveInterest.toDouble()
    } else {
        adjustedBaselineScore
    }

}