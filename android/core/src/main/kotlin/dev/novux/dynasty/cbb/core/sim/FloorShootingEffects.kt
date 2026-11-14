package dev.novux.dynasty.cbb.core.sim

import dev.novux.dynasty.cbb.core.strategy.ResolvedSchemeEffects

/** Blends roster shooting profile into possession scheme effects ([MEGA-SIM-169]). */
object FloorShootingEffects {
    fun merge(
        base: ResolvedSchemeEffects,
        floorParticipants: List<GameSimParticipant>,
    ): ResolvedSchemeEffects {
        if (floorParticipants.isEmpty()) return base
        val tendency = SlotPlayedFloorRating.meanThreePointTendency(floorParticipants)
        val threeNudge = (tendency - 50) / 500.0
        val makeNudge = (tendency - 50) / 800.0
        return base.copy(
            threePointAttemptRate = (base.threePointAttemptRate + threeNudge).coerceIn(0.12, 0.62),
            makeRateAdjust = base.makeRateAdjust + makeNudge,
        )
    }
}
