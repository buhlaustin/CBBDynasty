package dev.novux.dynasty.cbb.core.qa

/**
 * Advance-week confirmation must not seed recruits or build fast-sim bundles ([MEGA-QA-008]).
 *
 * Heavy work is deferred until the user confirms and [advanceDynastyWeek] runs.
 */
object SimWeekAdvancePreDialogPath {
    const val QA_ID: String = "MEGA-QA-008"

    const val FRAME_CROSS_REF: String = "MEGA-FRAME-093"

    const val REPOSITORY_SOURCE: String =
        "android/app/src/main/kotlin/dev/novux/dynasty/cbb/data/save/SaveSlotRepository.kt"

    const val PRE_DIALOG_PARAM: String = "preDialogOnly"

    const val LOAD_POOL_CALL: String = "loadPool"

    const val ENSURE_POOL_CALL: String = "ensurePool"

    const val FAST_SIM_FACTORY_CALL: String = "buildFastSimBundle"
}
