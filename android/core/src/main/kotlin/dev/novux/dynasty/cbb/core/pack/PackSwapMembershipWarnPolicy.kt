package dev.novux.dynasty.cbb.core.pack

/** Non-blocking snackbar when display pack year ≠ signed dynasty year ([MEGA-GDD-094]). */
object PackSwapMembershipWarnPolicy {
    const val MEGA_GDD_ID: String = "MEGA-GDD-094"

    fun isMismatch(signedMembershipYear: String?, activeMembershipYear: String): Boolean =
        PackMembershipYearPolicy.isMismatch(signedMembershipYear, activeMembershipYear)
}