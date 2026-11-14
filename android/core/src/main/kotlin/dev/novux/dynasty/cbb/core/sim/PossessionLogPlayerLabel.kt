package dev.novux.dynasty.cbb.core.sim

/**
 * Jersey # + display name on scorer / assister / rebounder in live possession text.
 */
object PossessionLogPlayerLabel {
    const val QA_ID: String = "MEGA-SIM-097"

    fun format(
        credits: PossessionPlayerCredits,
        offenseRoster: GameSimRosterInput?,
        defenseRoster: GameSimRosterInput?,
        before: TeamBoxScore,
        after: TeamBoxScore,
        change: PossessionChange,
        blockedShot: Boolean,
    ): String? {
        val fgm = after.fieldGoalsMade - before.fieldGoalsMade
        val fga = after.fieldGoalsAttempted - before.fieldGoalsAttempted
        val threeMade = after.threePointsMade - before.threePointsMade
        val ftAtt = after.freeThrowsAttempted - before.freeThrowsAttempted
        val parts = mutableListOf<String>()
        if (fgm > 0 && credits.scorerId != null) {
            val scorer = label(offenseRoster, credits.scorerId) ?: return null
            val make = if (threeMade > 0) "$scorer 3PT make" else "$scorer 2PT make"
            val line = credits.assistId?.let { astId ->
                val ast = label(offenseRoster, astId)
                if (ast != null) "$make (ast $ast)" else make
            } ?: make
            parts += line
        } else if (fga > 0 && fgm == 0 && !blockedShot && credits.rebounderId != null) {
            val rebounder = when (change) {
                PossessionChange.OffenseRetainsOffensiveRebound ->
                    label(offenseRoster, credits.rebounderId)
                PossessionChange.DefenseTakesBall ->
                    label(defenseRoster, credits.rebounderId)
            }
            rebounder?.let { parts += "rebound $it" }
        }
        if (parts.isEmpty() && ftAtt > 0 && credits.freeThrowShooterId != null) {
            label(offenseRoster, credits.freeThrowShooterId)?.let { parts += "FT $it" }
        }
        return parts.takeIf { it.isNotEmpty() }?.joinToString(" | ")
    }

    fun label(roster: GameSimRosterInput?, playerId: String): String? {
        val participant = roster?.participants?.firstOrNull { it.playerId == playerId } ?: return null
        val number = if (participant.jerseyNumber > 0) {
            participant.jerseyNumber
        } else {
            SimJerseyNumber.fromPlayerId(playerId)
        }
        val name = participant.logDisplayName.ifBlank { playerId }
        return "#$number $name"
    }
}
