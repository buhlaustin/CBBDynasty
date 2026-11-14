package dev.novux.dynasty.cbb.core.coach.security

/** HC contract + job-security surface for carousel season ([MEGA-GDD-034]). */
object HeadCoachCarouselContractPolicy {
    const val MEGA_ID: String = "MEGA-GDD-034"

    fun showJobSecurityFactorDetailsForUserCoach(isUserCoach: Boolean): Boolean = isUserCoach
}