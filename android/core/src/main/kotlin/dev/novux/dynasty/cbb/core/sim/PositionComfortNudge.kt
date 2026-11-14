package dev.novux.dynasty.cbb.core.sim

import dev.novux.dynasty.cbb.core.lineup.LineupSlotPolicy
import dev.novux.dynasty.cbb.core.sim.tuning.SimPositionComfortModifiers

/**
 * Out-of-position comfort nudge — does not reorder players; adjusts rating only
 * ([planning/positions-and-lineup.md#game-slots-vs-listed-position], MEGA-SIM-023).
 */
object PositionComfortNudge {
    fun offensiveRating(
        participant: GameSimParticipant,
        comfort: SimPositionComfortModifiers = SimPositionComfortModifiers(),
    ): Int = (participant.offensiveRating + comfortDelta(participant, comfort)).coerceIn(1, 99)

    fun defensiveRating(
        participant: GameSimParticipant,
        comfort: SimPositionComfortModifiers = SimPositionComfortModifiers(),
    ): Int = (participant.defensiveRating + comfortDelta(participant, comfort)).coerceIn(1, 99)

    private fun comfortDelta(
        participant: GameSimParticipant,
        comfort: SimPositionComfortModifiers,
    ): Int {
        if (participant.listedPosition == participant.slotPlayed) return 0
        return if (LineupSlotPolicy.slotEligible(participant.listedPosition, participant.slotPlayed)) {
            -comfort.eligibleInterchangePenalty
        } else {
            -comfort.ineligibleSlotPenalty
        }
    }
}
