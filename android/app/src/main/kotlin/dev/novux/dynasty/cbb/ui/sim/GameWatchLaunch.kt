package dev.novux.dynasty.cbb.ui.sim

import dev.novux.dynasty.cbb.core.roster.PlayerPosition
import dev.novux.dynasty.cbb.core.roster.RosterPlayer
import dev.novux.dynasty.cbb.core.sim.GameSimInjuryRollInput
import dev.novux.dynasty.cbb.core.sim.GameSimRosterInput
import dev.novux.dynasty.cbb.core.strategy.DefensiveSchemePreset
import dev.novux.dynasty.cbb.core.strategy.GameplanInput
import dev.novux.dynasty.cbb.core.strategy.OffensiveSchemePreset

import dev.novux.dynasty.cbb.core.sim.SimHomeVenueType

/** In-memory launch payload for watch sim (same dynasty session). */
object GameWatchLaunch {
    var homeGameplan: GameplanInput = GameplanInput()
    var homeName: String = "Home"
    var awayName: String = "Away"
    var homeSimRoster: GameSimRosterInput? = null
    var homeRosterPlayers: List<RosterPlayer> = emptyList()
    var homeStarters: Map<PlayerPosition, String> = emptyMap()
    var rotationMinutes: Map<String, Int> = emptyMap()
    var homeOffensiveScheme: OffensiveSchemePreset = OffensiveSchemePreset.Balanced
    var homeDefensiveScheme: DefensiveSchemePreset = DefensiveSchemePreset.ManToMan
    var injuryRoll: GameSimInjuryRollInput? = null
    var isScrimmage: Boolean = false
    var userTeamIsHome: Boolean = true
    var homeVenue: SimHomeVenueType = SimHomeVenueType.Home
    var userOffensiveRating: Int = 78
    var userDefensiveRating: Int = 74
    var awayOffensiveRating: Int = 76
    var awayDefensiveRating: Int = 75
    var simSeed: Long? = null
    var activeGameId: String? = null
}
