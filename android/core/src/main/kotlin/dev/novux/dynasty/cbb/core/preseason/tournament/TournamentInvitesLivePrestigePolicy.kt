package dev.novux.dynasty.cbb.core.preseason.tournament

/**
 * Tournament invite generation must use live user program prestige, not the recruiting economy default
 * ([MEGA-GDD-104]).
 */
object TournamentInvitesLivePrestigePolicy {
    const val MEGA_ID: String = "MEGA-GDD-104"
}