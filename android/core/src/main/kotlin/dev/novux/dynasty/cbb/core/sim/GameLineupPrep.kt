package dev.novux.dynasty.cbb.core.sim

import dev.novux.dynasty.cbb.core.lineup.DefaultStarterAlgorithm
import dev.novux.dynasty.cbb.core.lineup.LineupSlotPolicy
import dev.novux.dynasty.cbb.core.lineup.RotationMinutesAllocator
import dev.novux.dynasty.cbb.core.lineup.RotationMinutesBalancer
import dev.novux.dynasty.cbb.core.fatigue.FatigueEngine
import dev.novux.dynasty.cbb.core.roster.PlayerPosition
import dev.novux.dynasty.cbb.core.roster.RosterPlayer

data class GameSimParticipant(
    val playerId: String,
    val slotPlayed: PlayerPosition,
    val listedPosition: PlayerPosition,
    val offensiveRating: Int,
    val defensiveRating: Int,
    val targetMinutes: Int,
    val logDisplayName: String = "",
    val jerseyNumber: Int = 0,
    val startingStamina: Int = FatigueEngine.FRESH,
    val heightInches: Int = 76,
    val weightLbs: Int = 200,
    val shooting: GameSimShootingProfile = GameSimShootingProfile(),
)

data class GameSimRosterInput(
    val teamId: String,
    val participants: List<GameSimParticipant>,
) {
    fun weightedOffensive(): Int = weighted(participants) { it.offensiveRating }
    fun weightedDefensive(): Int = weighted(participants) { it.defensiveRating }

    private fun weighted(items: List<GameSimParticipant>, pick: (GameSimParticipant) -> Int): Int {
        val totalMin = items.sumOf { it.targetMinutes }
        if (totalMin <= 0) return 60
        val sum = items.sumOf { pick(it) * it.targetMinutes }
        return (sum / totalMin).coerceIn(1, 99)
    }
}

/**
 * Builds sim participants from dynasty roster — starters, interchange validation, minutes.
 */
object GameLineupPrep {
    fun build(
        teamId: String,
        players: List<RosterPlayer>,
        userStarters: Map<PlayerPosition, String>? = null,
        userRotationMinutes: Map<String, Int>? = null,
    ): GameSimRosterInput {
        val validation = GameLineupPrepValidator.validate(players, userStarters)
        require(validation.canStartSim) {
            "Cannot build sim roster: missing=${validation.missingSlots}, illegal=${validation.illegalAssignments}"
        }
        val eligible = players.filter { RotationMinutesAllocator.isLineupEligible(it) }
        val starters = resolveStarters(eligible, userStarters)
        val minutes = RotationMinutesBalancer.resolveForSim(players, starters, userRotationMinutes)
        val byId = eligible.associateBy { it.playerId }
        val starterSlotsPerPlayer = starters.entries.groupBy({ it.value }, { it.key })
        val participants = buildList {
            starters.forEach { (slot, id) ->
                val player = byId[id] ?: return@forEach
                val totalMin = minutes[id] ?: RotationMinutesAllocator.STARTER_MINUTES
                val slotsForPlayer = starterSlotsPerPlayer[id].orEmpty()
                val slotMinutes = when {
                    slotsForPlayer.size <= 1 -> totalMin
                    else -> {
                        val index = slotsForPlayer.indexOf(slot).coerceAtLeast(0)
                        val base = totalMin / slotsForPlayer.size
                        val remainder = totalMin % slotsForPlayer.size
                        base + if (index < remainder) 1 else 0
                    }
                }
                add(
                    GameSimParticipant(
                        playerId = id,
                        slotPlayed = slot,
                        listedPosition = player.position,
                        offensiveRating = player.off,
                        defensiveRating = player.def,
                        targetMinutes = slotMinutes,
                        logDisplayName = player.displayLabel(),
                        jerseyNumber = SimJerseyNumber.fromPlayerId(id),
                        startingStamina = player.stamina,
                        heightInches = player.heightInches,
                        weightLbs = player.weightLbs,
                        shooting = GameSimShootingProfile(
                            threePointTendency = player.off.coerceIn(1, 99),
                        ),
                    ),
                )
            }
            minutes.forEach { (id, min) ->
                if (id in starters.values) return@forEach
                val player = byId[id] ?: return@forEach
                add(
                    GameSimParticipant(
                        playerId = id,
                        slotPlayed = player.position,
                        listedPosition = player.position,
                        offensiveRating = player.off,
                        defensiveRating = player.def,
                        targetMinutes = min,
                        logDisplayName = player.displayLabel(),
                        jerseyNumber = SimJerseyNumber.fromPlayerId(id),
                        startingStamina = player.stamina,
                        heightInches = player.heightInches,
                        weightLbs = player.weightLbs,
                        shooting = GameSimShootingProfile(
                            threePointTendency = player.off.coerceIn(1, 99),
                        ),
                    ),
                )
            }
        }
        val totalMinutes = participants.sumOf { it.targetMinutes }
        require(totalMinutes <= RotationMinutesAllocator.TEAM_MINUTES_REGULATION + 5) {
            "Rotation minutes $totalMinutes exceed regulation cap"
        }
        return GameSimRosterInput(teamId = teamId, participants = participants)
    }

    fun resolveStarters(
        players: List<RosterPlayer>,
        userStarters: Map<PlayerPosition, String>?,
    ): Map<PlayerPosition, String> {
        if (userStarters.isNullOrEmpty()) {
            return DefaultStarterAlgorithm.pickStarters(players)
        }
        val byId = players.associateBy { it.playerId }
        userStarters.forEach { (slot, id) ->
            val player = byId[id] ?: error("Unknown starter $id")
            require(LineupSlotPolicy.slotEligible(player.position, slot)) {
                "Illegal slot $slot for listed ${player.position}"
            }
            require(RotationMinutesAllocator.isLineupEligible(player)) {
                "Ineligible starter $id"
            }
        }
        return userStarters
    }
}
