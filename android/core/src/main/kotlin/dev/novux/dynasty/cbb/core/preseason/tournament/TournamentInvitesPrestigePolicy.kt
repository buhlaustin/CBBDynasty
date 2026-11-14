package dev.novux.dynasty.cbb.core.preseason.tournament

/** Prestige-driven invite counts ([planning/preseason.md#6--tournament-invites], [MEGA-FRAME-074]). */
enum class TournamentInvitesPrestigeTier {
    Low,
    Mid,
    High,
}

object TournamentInvitesPrestigePolicy {
    const val MEGA_FRAME_ID: String = "MEGA-FRAME-074"

    const val LOW_PRESTIGE_MAX: Int = 29
    const val MID_PRESTIGE_MAX: Int = 54

    const val HIGH_INVITE_COUNT_MIN: Int = 2
    const val HIGH_INVITE_COUNT_MAX: Int = 3

    fun tier(programPrestige: Int): TournamentInvitesPrestigeTier = when {
        programPrestige <= LOW_PRESTIGE_MAX -> TournamentInvitesPrestigeTier.Low
        programPrestige <= MID_PRESTIGE_MAX -> TournamentInvitesPrestigeTier.Mid
        else -> TournamentInvitesPrestigeTier.High
    }

    fun offersMultipleInvites(programPrestige: Int): Boolean =
        tier(programPrestige) == TournamentInvitesPrestigeTier.High
}
