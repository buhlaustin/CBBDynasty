package dev.novux.dynasty.cbb.core.qa

/**
 * Release AAB → universal APK sideload parity with debug smoke ([MEGA-QA-134]).
 */
object ReleaseSideloadSmokePath {
    const val QA_ID: String = "MEGA-QA-134"

    /** Same application id for debug and release (sideload parity). */
    const val APPLICATION_ID: String = "com.dynasty.cbb"

    data class ParityStep(val id: String, val description: String)

    /** Subset of [PlayPreLaunchSmokePath] that must pass on release sideload. */
    val PARITY_WITH_DEBUG: List<ParityStep> = listOf(
        ParityStep("build_universal", "Run scripts/build_release_universal_apk.sh"),
        ParityStep("install_sideload", "adb install -r universal.apk on test device"),
        ParityStep("cold_launch", "Cold launch — studio/splash/home (same as MEGA-QA-119 step 2)"),
        ParityStep("new_game_sign", "New game wizard sign (MEGA-QA-119 step 3)"),
        ParityStep("reach_rs", "Reach regular season shell (MEGA-QA-119 step 5)"),
    )
}
