package dev.novux.dynasty.cbb.core.sim

/** Read-only opponent first-half line for halftime coach UI ([MEGA-SIM-126]). */
data class HalftimeScoutingSnapshot(
    val opponentName: String,
    val userPoints: Int,
    val opponentPoints: Int,
    val opponentBox: TeamBoxScore,
    val opponentOffensiveRating: Int,
    val opponentDefensiveRating: Int,
) {
    companion object {
        fun fromHalftimePartial(
            userIsHome: Boolean,
            homeName: String,
            awayName: String,
            partial: GameSimResult,
            homeRating: GameSimTeamInput,
            awayRating: GameSimTeamInput,
        ): HalftimeScoutingSnapshot {
            val oppBox = if (userIsHome) partial.awayBox else partial.homeBox
            val oppInput = if (userIsHome) awayRating else homeRating
            return HalftimeScoutingSnapshot(
                opponentName = if (userIsHome) awayName else homeName,
                userPoints = if (userIsHome) partial.homeScore else partial.awayScore,
                opponentPoints = if (userIsHome) partial.awayScore else partial.homeScore,
                opponentBox = oppBox,
                opponentOffensiveRating = oppInput.offensiveRating,
                opponentDefensiveRating = oppInput.defensiveRating,
            )
        }
    }
}
