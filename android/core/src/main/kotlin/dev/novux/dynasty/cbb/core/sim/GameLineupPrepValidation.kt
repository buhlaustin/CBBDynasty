package dev.novux.dynasty.cbb.core.sim

import dev.novux.dynasty.cbb.core.lineup.DefaultStarterAlgorithm
import dev.novux.dynasty.cbb.core.lineup.LineupSlotPolicy
import dev.novux.dynasty.cbb.core.lineup.RotationMinutesAllocator
import dev.novux.dynasty.cbb.core.roster.PlayerPosition
import dev.novux.dynasty.cbb.core.roster.RosterPlayer

/** Sim entry gate — [planning/positions-and-lineup.md], MEGA-SIM-165. */
data class GameLineupPrepValidation(
    val canStartSim: Boolean,
    val missingSlots: List<PlayerPosition> = emptyList(),
    val illegalAssignments: List<IllegalSlotAssignment> = emptyList(),
    val unknownOrIneligiblePlayerIds: List<String> = emptyList(),
) {
    data class IllegalSlotAssignment(
        val slot: PlayerPosition,
        val playerId: String,
        val listedPosition: PlayerPosition,
    )
}

object GameLineupPrepValidator {
    fun validate(
        players: List<RosterPlayer>,
        userStarters: Map<PlayerPosition, String>? = null,
    ): GameLineupPrepValidation {
        val eligible = players.filter { RotationMinutesAllocator.isLineupEligible(it) }
        val byId = eligible.associateBy { it.playerId }
        val starters = if (userStarters.isNullOrEmpty()) {
            DefaultStarterAlgorithm.pickStarters(players)
        } else {
            userStarters
        }
        val missingSlots = PlayerPosition.STARTER_ORDER.filter { starters[it].isNullOrBlank() }
        val illegal = mutableListOf<GameLineupPrepValidation.IllegalSlotAssignment>()
        val badIds = mutableSetOf<String>()
        starters.forEach { (slot, id) ->
            if (id.isBlank()) return@forEach
            val player = byId[id]
            if (player == null) {
                badIds += id
                return@forEach
            }
            if (!LineupSlotPolicy.slotEligible(player.position, slot)) {
                illegal += GameLineupPrepValidation.IllegalSlotAssignment(
                    slot = slot,
                    playerId = id,
                    listedPosition = player.position,
                )
            }
        }
        val canStart = missingSlots.isEmpty() &&
            illegal.isEmpty() &&
            badIds.isEmpty() &&
            PlayerPosition.STARTER_ORDER.all { !starters[it].isNullOrBlank() }
        return GameLineupPrepValidation(
            canStartSim = canStart,
            missingSlots = missingSlots,
            illegalAssignments = illegal,
            unknownOrIneligiblePlayerIds = badIds.toList(),
        )
    }
}
