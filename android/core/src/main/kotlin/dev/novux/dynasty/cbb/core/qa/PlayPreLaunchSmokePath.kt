package dev.novux.dynasty.cbb.core.qa

import dev.novux.dynasty.cbb.core.calendar.CalendarPhase

/**
 * Documented smoke milestones for Play pre-launch ([MEGA-QA-119]).
 * Manual emulator path; JVM test asserts ordering only.
 */
object PlayPreLaunchSmokePath {
    data class Milestone(val id: String, val description: String, val phase: CalendarPhase? = null)

    val MILESTONES: List<Milestone> = listOf(
        Milestone("install_debug", "Install debug APK on API 34 x86_64 emulator"),
        Milestone("new_game_signed", "Complete new-game wizard sign", CalendarPhase.Offseason),
        Milestone("in_season_shell", "Enter dynasty shell from load or sign"),
        Milestone(
            "regular_season_week_1",
            "Reach regular season week 1 (or exhibition then RS per schedule)",
            CalendarPhase.RegularSeason,
        ),
    )

    const val TARGET_API_LEVEL: Int = 34
    const val TARGET_AVD_PROFILE: String = "pixel_6"
}
