package dev.novux.dynasty.cbb.core.sim

import dev.novux.dynasty.cbb.core.lineup.RotationMinutesAllocator
import dev.novux.dynasty.cbb.core.roster.PlayerPosition
import dev.novux.dynasty.cbb.core.strategy.DefensiveSchemePreset
import dev.novux.dynasty.cbb.core.strategy.GameplanInput
import dev.novux.dynasty.cbb.core.strategy.OffensiveSchemePreset

/** Second-half minutes tilt — [planning/strategy-schemes.md#halftime-adjustments]. */
enum class HalftimeRotationEmphasis {
    RideStarters,
    Neutral,
    ExpandBench,
}

data class HalftimeCoachAdjustments(
    val gameplan: GameplanInput,
    val offensiveScheme: OffensiveSchemePreset,
    val defensiveScheme: DefensiveSchemePreset,
    val starters: Map<PlayerPosition, String>,
    val rotationEmphasis: HalftimeRotationEmphasis = HalftimeRotationEmphasis.Neutral,
)

object HalftimeRosterAdjust {
    fun applyRotationEmphasis(
        roster: GameSimRosterInput,
        emphasis: HalftimeRotationEmphasis,
    ): GameSimRosterInput {
        if (emphasis == HalftimeRotationEmphasis.Neutral) return roster
        val starterTarget = RotationMinutesAllocator.STARTER_MINUTES
        val adjusted = roster.participants.map { p ->
            val delta = when (emphasis) {
                HalftimeRotationEmphasis.RideStarters ->
                    if (p.targetMinutes >= starterTarget - 2) 4 else -2
                HalftimeRotationEmphasis.ExpandBench ->
                    if (p.targetMinutes >= starterTarget - 2) -3 else 3
                HalftimeRotationEmphasis.Neutral -> 0
            }
            p.copy(targetMinutes = (p.targetMinutes + delta).coerceAtLeast(4))
        }
        val total = adjusted.sumOf { it.targetMinutes }
        val cap = RotationMinutesAllocator.TEAM_MINUTES_REGULATION
        val normalized = if (total == cap) {
            adjusted
        } else {
            rebalanceToCap(adjusted, cap)
        }
        return roster.copy(participants = normalized)
    }

    private fun rebalanceToCap(
        participants: List<GameSimParticipant>,
        cap: Int,
    ): List<GameSimParticipant> {
        val mutable = participants.map { it.targetMinutes }.toMutableList()
        var total = mutable.sum()
        var index = 0
        while (total > cap && mutable.any { it > 4 }) {
            val i = index % mutable.size
            if (mutable[i] > 4) {
                mutable[i]--
                total--
            }
            index++
        }
        while (total < cap) {
            val i = index % mutable.size
            mutable[i]++
            total++
            index++
        }
        return participants.mapIndexed { i, p -> p.copy(targetMinutes = mutable[i]) }
    }
}
