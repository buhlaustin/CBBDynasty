package dev.novux.dynasty.cbb.core.league

import dev.novux.dynasty.cbb.core.pack.ConferenceDisplayOverlay
import dev.novux.dynasty.cbb.core.pack.TeamDisplayOverlay

/**
 * Builds RS schedules for every D-I program in the active pack ([planning/game-design.md#world-scale--league-simulation], [MEGA-NOTES-047]).
 */
object LeagueScheduleGenerator {
    const val MEGA_ID: String = "MEGA-NOTES-047"

    fun generate(
        teams: List<TeamDisplayOverlay>,
        conferences: Map<String, ConferenceDisplayOverlay>,
        seasonIndex: Int,
        dynastySeed: Long,
    ): LeagueScheduleSeason =
        LeagueScheduleSymmetricEngine.generate(teams, conferences, seasonIndex, dynastySeed)
}
