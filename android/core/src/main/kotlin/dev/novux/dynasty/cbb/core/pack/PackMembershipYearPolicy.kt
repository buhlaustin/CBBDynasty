package dev.novux.dynasty.cbb.core.pack

/** Warn when display pack year drifted from dynasty sign — history IDs stay on save ([MEGA-GDD-019]). */
object PackMembershipYearPolicy {
    const val MEGA_GDD_ID = "MEGA-GDD-019"

    fun isMismatch(signedMembershipYear: String?, activeMembershipYear: String): Boolean {
        if (signedMembershipYear.isNullOrBlank()) return false
        return signedMembershipYear != activeMembershipYear
    }
}