package dev.novux.dynasty.cbb.core.qa

/**
 * Coach quick-preview sheet before full card ([MEGA-COACH-018]).
 */
object CoachPreviewSheetPath {
    const val QA_ID: String = "MEGA-COACH-018"

    const val SHEET_SOURCE: String =
        "android/app/src/main/kotlin/dev/novux/dynasty/cbb/ui/coach/CoachPreviewSheet.kt"

    const val SHELL_SOURCE: String =
        "android/app/src/main/kotlin/dev/novux/dynasty/cbb/ui/shell/DynastyShellScreen.kt"

    const val PREVIEW_ENTRY_SYMBOL: String = "openHeadCoachPreview"

    const val FULL_CARD_SYMBOL: String = "openHeadCoachFull"

    const val FORBIDDEN_IN_PREVIEW: String = "CoachPortraitSlot"
}
