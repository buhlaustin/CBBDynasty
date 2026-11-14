package dev.novux.dynasty.cbb.core.qa

import dev.novux.dynasty.cbb.core.calendar.CalendarPhase

/**
 * Physical-device product gate ([MEGA-QA-127]): mid-tier Android 13+ with full
 * new-game flow and four consecutive regular-season sim weeks on device.
 */
object PhysicalDeviceSmokePath {
    /** Android 13 (API 33) minimum per Play mid-tier matrix. */
    const val MIN_ANDROID_API: Int = 33

    /** Typical retail smoke device (RAM). */
    const val MIN_RAM_GB: Int = 6

    /** Sim week advances in RS after reaching in-season shell. */
    const val REGULAR_SEASON_SIM_WEEKS: Int = 4

    const val DEVICE_PROFILE_LABEL: String = "Mid-tier retail · ${MIN_RAM_GB} GB RAM · Android 13+"

    data class Step(val id: String, val action: String)

    val MANUAL_STEPS: List<Step> = listOf(
        Step("install_debug", "Install debug APK via USB or internal testing track"),
        Step("new_game", "New game → complete wizard → sign dynasty"),
        Step("enter_rs", "Reach regular season week 1 in dynasty shell"),
        Step(
            "sim_four_weeks",
            "Confirm and sim week four times; no crash, phase stays coherent",
        ),
        Step("verify_save", "Force-stop app, relaunch, load same slot — still RS, week ≥ 5"),
    )

    val FINAL_PHASE: CalendarPhase = CalendarPhase.RegularSeason
}
