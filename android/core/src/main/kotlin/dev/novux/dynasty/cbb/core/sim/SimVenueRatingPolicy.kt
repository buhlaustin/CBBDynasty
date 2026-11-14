package dev.novux.dynasty.cbb.core.sim

import dev.novux.dynasty.cbb.core.sim.tuning.SimVenueModifiers
import kotlin.math.roundToInt

object SimVenueRatingPolicy {
    fun offensiveRating(base: Int, offenseIsHome: Boolean, homeVenue: SimHomeVenueType, mods: SimVenueModifiers): Int =
        (base * multiplierForTeam(offenseIsHome, homeVenue, mods)).roundToInt().coerceAtLeast(1)

    fun defensiveRating(base: Int, defenseIsHome: Boolean, homeVenue: SimHomeVenueType, mods: SimVenueModifiers): Int =
        (base * multiplierForTeam(defenseIsHome, homeVenue, mods)).roundToInt().coerceAtLeast(1)

  /** Final two minutes at home — light crowd nudge for the home side ([MEGA-SIM-118] partial). */
    fun lateGameHomeCrowdOffenseBoost(
        offenseIsHome: Boolean,
        gameClockSeconds: Int,
        homeVenue: SimHomeVenueType,
    ): Int = when {
        offenseIsHome && homeVenue == SimHomeVenueType.Home && gameClockSeconds <= 120 -> 1
        else -> 0
    }

    private fun multiplierForTeam(
        teamIsHome: Boolean,
        homeVenue: SimHomeVenueType,
        mods: SimVenueModifiers,
    ): Double = if (teamIsHome) {
        when (homeVenue) {
            SimHomeVenueType.Home -> mods.home
            SimHomeVenueType.Neutral -> mods.neutral
            SimHomeVenueType.Road -> mods.road
        }
    } else {
        when (homeVenue) {
            SimHomeVenueType.Home -> mods.road
            SimHomeVenueType.Neutral -> mods.neutral
            SimHomeVenueType.Road -> mods.home
        }
    }
}
