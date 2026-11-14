package dev.novux.dynasty.cbb.core.strategy

import dev.novux.dynasty.cbb.core.lineup.LineupSlotPolicy
import dev.novux.dynasty.cbb.core.lineup.RotationMinutesAllocator
import dev.novux.dynasty.cbb.core.roster.PlayerPosition
import dev.novux.dynasty.cbb.core.roster.RosterPlayer

/**
 * Maps opponent lineup slots (PG–C) to your defenders — [planning/strategy-schemes.md#gameplan-fields-locked-for-planning].
 */
object DefensiveAssignmentPolicy {
    /** Opponent slots in standard scouting order. */
    val OPPONENT_SLOTS: List<PlayerPosition> = PlayerPosition.STARTER_ORDER

    fun defaultAssignments(
        starters: Map<PlayerPosition, String>,
    ): Map<PlayerPosition, String> =
        OPPONENT_SLOTS.mapNotNull { oppSlot ->
            starters[oppSlot]?.let { oppSlot to it }
        }.toMap()

    fun eligibleDefenders(
        players: List<RosterPlayer>,
        opponentSlot: PlayerPosition,
    ): List<RosterPlayer> =
        players
            .filter { RotationMinutesAllocator.isLineupEligible(it) }
            .filter { LineupSlotPolicy.slotEligible(it.position, opponentSlot) }
            .sortedByDescending { it.def }

    fun cycleAssignment(
        players: List<RosterPlayer>,
        opponentSlot: PlayerPosition,
        currentDefenderId: String?,
    ): String? {
        val options = eligibleDefenders(players, opponentSlot)
        if (options.isEmpty()) return null
        val index = options.indexOfFirst { it.playerId == currentDefenderId }.let {
            if (it < 0) 0 else (it + 1) % options.size
        }
        return options[index].playerId
    }

    fun resolve(
        gameplan: GameplanInput,
        starters: Map<PlayerPosition, String>,
    ): Map<PlayerPosition, String> {
        val defaults = defaultAssignments(starters)
        if (gameplan.defensiveAssignments.isEmpty()) return defaults
        return OPPONENT_SLOTS.associateWith { slot ->
            gameplan.defensiveAssignments[slot]
                ?: defaults[slot]
                ?: defaults.values.firstOrNull()
                .orEmpty()
        }.filterValues { it.isNotEmpty() }
    }
}
