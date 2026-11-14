package dev.novux.dynasty.cbb.core.sim

import dev.novux.dynasty.cbb.core.lineup.RotationMinutesAllocator

/** User-facing copy for halftime rotation emphasis minute nudges (MEGA-SIM-143). */
object HalftimeRotationBandHint {
    fun minuteBandSummary(emphasis: HalftimeRotationEmphasis): String = when (emphasis) {
        HalftimeRotationEmphasis.Neutral ->
            "Second-half target minutes match pregame rotation bands (${RotationMinutesAllocator.TEAM_MINUTES_REGULATION} team total)."
        HalftimeRotationEmphasis.RideStarters ->
            "Starters +4 min target, bench −2 min (rebalanced to ${RotationMinutesAllocator.TEAM_MINUTES_REGULATION} team minutes)."
        HalftimeRotationEmphasis.ExpandBench ->
            "Starters −3 min, bench +3 min (rebalanced to ${RotationMinutesAllocator.TEAM_MINUTES_REGULATION} team minutes)."
    }
}
