package dev.novux.dynasty.cbb.core.shell

/**
 * After the studio beat was shown once (or skipped via setting), do not replay on activity resume
 * ([MEGA-SHELL-085], [planning/app-shell.md#launch-flow]).
 */
object LaunchResumeSkipStudioPolicy {
    const val MEGA_ID: String = "MEGA-SHELL-085"

    fun shouldBypassStudioBeat(skipStudioIntroSetting: Boolean, studioBeatConsumed: Boolean): Boolean =
        skipStudioIntroSetting || studioBeatConsumed
}