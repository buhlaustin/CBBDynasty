package dev.novux.dynasty.cbb.core.shell

/** In-career shell uses a modal navigation drawer for hub links ([MEGA-SHELL-099]). */
object ShellHamburgerDrawerPolicy {
    const val MEGA_ID: String = "MEGA-SHELL-099"

    /** Canonical chrome is ModalNavigationDrawer, not a dropdown menu. */
    fun usesModalNavigationDrawer(): Boolean = true
}