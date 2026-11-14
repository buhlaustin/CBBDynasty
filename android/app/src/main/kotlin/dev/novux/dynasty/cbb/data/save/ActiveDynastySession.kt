package dev.novux.dynasty.cbb.data.save

import dev.novux.dynasty.cbb.core.save.LoadedDynasty

/** In-memory active dynasty until in-game shell exists (Phase 4+). */
object ActiveDynastySession {
    @Volatile
    var current: LoadedDynasty? = null
        private set

    /** Incremented when an overlay hub pops back to the in-career shell ([MEGA-FRAME-139]). */
    @Volatile
    var shellDashboardTabRestoreNonce: Int = 0
        private set

    private val listeners = mutableListOf<() -> Unit>()

    fun addListener(listener: () -> Unit): () -> Unit {
        listeners.add(listener)
        return { listeners.remove(listener) }
    }

    fun requestShellDashboardTabOnHubReturn() {
        shellDashboardTabRestoreNonce++
        listeners.toList().forEach { it() }
    }

    fun set(loaded: LoadedDynasty) {
        current = loaded
        listeners.toList().forEach { it() }
    }

    fun clear() {
        current = null
        listeners.toList().forEach { it() }
    }
}
