package dev.novux.dynasty.cbb.core.preseason

/**
 * Checklist order UX — tasks 1–5 may be completed in any order ([planning/preseason.md], [MEGA-FRAME-131]).
 */
object PreseasonTaskOrderPolicy {
    const val FRAME_ID: String = "MEGA-FRAME-131"

    val flexibleOrderTaskIds: Set<PreseasonTaskId> = setOf(
        PreseasonTaskId.IncomingClassSeed,
        PreseasonTaskId.PreseasonWorkouts,
        PreseasonTaskId.CoachUpgrades,
        PreseasonTaskId.RankingsReview,
        PreseasonTaskId.PreseasonReport,
    )
}