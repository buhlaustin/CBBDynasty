package dev.novux.dynasty.cbb.core.player.stats

import dev.novux.dynasty.cbb.core.roster.RosterPlayer

/** Who receives an official GP tick when the user team commits a scheduled game ([MEGA-COACH-149]). */
object PlayerSeasonStatFinalizeGate {
    fun playersReceivingGamePlayed(players: List<RosterPlayer>, teamPlayedGame: Boolean): List<RosterPlayer> {
        if (!teamPlayedGame) return emptyList()
        return players.filter { eligibleForOfficialGame(it) }
    }

    private fun eligibleForOfficialGame(player: RosterPlayer): Boolean {
        if (player.medicalRedshirtSeason) return false
        if (player.academicHold) return false
        if (player.gamesSuspendedRemaining > 0) return false
        return player.isStarter || player.depthOrder <= 10
    }
}
