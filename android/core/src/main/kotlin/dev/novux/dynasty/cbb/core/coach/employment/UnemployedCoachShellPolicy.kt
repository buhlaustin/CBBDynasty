package dev.novux.dynasty.cbb.core.coach.employment

/** Unemployed coaches stay on unified career route with job offers in shell chrome ([MEGA-FRAME-144]). */
object UnemployedCoachShellPolicy {
    const val MEGA_FRAME_ID = "MEGA-FRAME-144"

    fun usesUnifiedCareerRoute(status: CoachEmploymentStatus): Boolean =
        status == CoachEmploymentStatus.Unemployed
}