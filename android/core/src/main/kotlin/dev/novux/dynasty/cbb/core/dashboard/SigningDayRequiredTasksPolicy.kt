package dev.novux.dynasty.cbb.core.dashboard

import dev.novux.dynasty.cbb.core.signingday.SigningDayProgress

/**
 * Signing Day shell requires rankings view, position confirm, then hard sign
 * ([MEGA-RECR-046], [SigningDayRequiredDashboardTasks]).
 */
object SigningDayRequiredTasksPolicy {
    const val MEGA_ID: String = "MEGA-RECR-046"

    fun hasOpenRequiredTasks(progress: SigningDayProgress): Boolean =
        SigningDayRequiredDashboardTasks.build(progress).isNotEmpty()
}