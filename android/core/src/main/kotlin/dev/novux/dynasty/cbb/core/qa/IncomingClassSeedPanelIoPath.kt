package dev.novux.dynasty.cbb.core.qa

/**
 * Preseason incoming-class seed must not run repository work on the UI thread ([MEGA-RECR-161]).
 */
object IncomingClassSeedPanelIoPath {
    const val QA_ID: String = "MEGA-RECR-161"

    const val PANEL_SOURCE: String =
        "android/app/src/main/kotlin/dev/novux/dynasty/cbb/ui/preseason/IncomingClassSeedPanel.kt"

    const val REQUIRED_DISPATCHER: String = "Dispatchers.IO"

    val GUARDED_CALLS: List<String> = listOf(
        "isSeeded",
        "loadHeadline",
        "runSeed",
        "completeTask",
    )
}
