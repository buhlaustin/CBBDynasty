package dev.novux.dynasty.cbb.core.shell

/**
 * Title splash CTA unlocks after pack overlay index and save slot index are ready
 * ([MEGA-SHELL-011], [planning/app-shell.md#splash-loading-screen]).
 */
object SplashReadyGatePolicy {
    const val MEGA_ID: String = "MEGA-SHELL-011"

    fun isReady(packTeamCount: Int, saveSlotsInitialized: Boolean): Boolean =
        packTeamCount > 0 && saveSlotsInitialized
}