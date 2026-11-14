package dev.novux.dynasty.cbb.core.qa

/**
 * In-season dashboard must load pending discipline on a dedicated effect, not behind shell hydration
 * ([MEGA-QA-093], [MEGA-FRAME-032]).
 */
object DisciplineDashboardFirstPaint {
    const val QA_ID: String = "MEGA-QA-093"
    const val FRAME_CROSS_REF: String = "MEGA-FRAME-032"

    const val SHELL_DEDICATED_EFFECT_MARKER: String = "disciplinePendingLoadEffect"

    const val SHELL_HYDRATION_EFFECT_MARKER: String = "shellDashboardHydrationEffect"
}
