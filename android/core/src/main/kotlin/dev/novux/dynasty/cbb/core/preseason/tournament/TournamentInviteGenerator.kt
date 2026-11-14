package dev.novux.dynasty.cbb.core.preseason.tournament

import kotlin.random.Random

object TournamentInviteGenerator {
    fun invites(programPrestige: Int, seed: Long): List<EarlySeasonEvent> {
        val random = Random(seed)
        return when (TournamentInvitesPrestigePolicy.tier(programPrestige)) {
            TournamentInvitesPrestigeTier.Low -> emptyList()
            TournamentInvitesPrestigeTier.Mid -> {
                if (random.nextBoolean()) {
                    listOf(EarlySeasonEventCatalog.all[random.nextInt(EarlySeasonEventCatalog.all.size)])
                } else {
                    emptyList()
                }
            }
            TournamentInvitesPrestigeTier.High -> {
                val take = TournamentInvitesPrestigePolicy.HIGH_INVITE_COUNT_MIN +
                    random.nextInt(
                        TournamentInvitesPrestigePolicy.HIGH_INVITE_COUNT_MAX -
                            TournamentInvitesPrestigePolicy.HIGH_INVITE_COUNT_MIN + 1,
                    )
                EarlySeasonEventCatalog.all.shuffled(random).take(take)
            }
        }
    }
}
