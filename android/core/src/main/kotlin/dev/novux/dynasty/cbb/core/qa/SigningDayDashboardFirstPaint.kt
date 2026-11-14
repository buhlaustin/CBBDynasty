package dev.novux.dynasty.cbb.core.qa

import dev.novux.dynasty.cbb.core.signingday.SigningDayConstants

/**
 * Offseason Signing Day dashboard must not flash an empty required panel while progress loads
 * ([MEGA-QA-094], [MEGA-FRAME-105]).
 */
object SigningDayDashboardFirstPaint {
    const val QA_ID: String = "MEGA-QA-094"

    const val FRAME_CROSS_REF: String = "MEGA-FRAME-105"

    val OFFSEASON_WEEK_INDEX: Int = SigningDayConstants.OFFSEASON_WEEK_INDEX

    /** Typical first-paint required cards before hard-sign unlocks. */
    const val SKELETON_PLACEHOLDER_COUNT: Int = 2
}
