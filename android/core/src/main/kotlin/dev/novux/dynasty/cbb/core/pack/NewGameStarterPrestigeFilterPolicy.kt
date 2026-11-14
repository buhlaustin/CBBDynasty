package dev.novux.dynasty.cbb.core.pack

/**
 * Default school picker shows programs at prestige ≤40 until unlock-all ([MEGA-RECR-075]).
 */
object NewGameStarterPrestigeFilterPolicy {
    const val MEGA_ID: String = "MEGA-RECR-075"

    fun isVisible(programPrestige: Int, unlockAll: Boolean): Boolean =
        WizardProgramPrestige.isSelectable(programPrestige, unlockAll)
}