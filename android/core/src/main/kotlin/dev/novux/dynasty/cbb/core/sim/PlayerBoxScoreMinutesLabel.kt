package dev.novux.dynasty.cbb.core.sim

import dev.novux.dynasty.cbb.core.injuries.GameSimInjuryEvent

/** Game log minutes column — [planning/injuries.md], MEGA-SIM-132. */
object PlayerBoxScoreMinutesLabel {
    fun format(
        line: PlayerBoxScore,
        midGameInjuryPlayerIds: Set<String>,
    ): String {
        val midInjury = line.playerId in midGameInjuryPlayerIds
        return when {
            midInjury && line.minutesSeconds <= 0 -> "DNP-INJ"
            line.minutesSeconds <= 0 -> "0"
            else -> line.minutesPlayed().toString()
        }
    }

    fun midGameInjuredIds(events: List<GameSimInjuryEvent>): Set<String> =
        events.filter { it.occurredInGameMinute != null }.map { it.playerId }.toSet()
}
