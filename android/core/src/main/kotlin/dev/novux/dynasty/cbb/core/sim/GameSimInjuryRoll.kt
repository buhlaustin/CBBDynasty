package dev.novux.dynasty.cbb.core.sim

import dev.novux.dynasty.cbb.core.balance.BalanceInjuryOccurrence
import dev.novux.dynasty.cbb.core.calendar.CalendarPhase
import dev.novux.dynasty.cbb.core.injuries.GameSimInjuryEvent
import dev.novux.dynasty.cbb.core.injuries.InjuryBankRow
import dev.novux.dynasty.cbb.core.injuries.InjuryOccurrenceEngine
import dev.novux.dynasty.cbb.core.injuries.InjuryRosterOps
import dev.novux.dynasty.cbb.core.injuries.InjuryApplyContext
import dev.novux.dynasty.cbb.core.roster.RosterPlayer

/**
 * Post-game injury rolls — [planning/injuries.md#sim-hook].
 */
data class GameSimInjuryRollInput(
    val phase: CalendarPhase,
    val bank: List<InjuryBankRow>,
    val occurrence: BalanceInjuryOccurrence,
    val medicalStaffGrade: Int,
    val homeRosterPlayers: List<RosterPlayer>?,
    val awayRosterPlayers: List<RosterPlayer>?,
    val injuryApplyContext: InjuryApplyContext? = null,
)

object GameSimInjuryRoll {
    fun attach(
        input: GameSimInput,
        result: GameSimResult,
        rollInput: GameSimInjuryRollInput,
    ): GameSimResult {
        if (!result.isComplete) return result
        val midHomeIds = result.homeInjuries.map { it.playerId }.toSet()
        val midAwayIds = result.awayInjuries.map { it.playerId }.toSet()
        val homeMinutes = floorMinutesFromResult(result.homeFloorSecondsByPlayer, input.homeRoster, result.homeBox.possessions)
        val awayMinutes = floorMinutesFromResult(result.awayFloorSecondsByPlayer, input.awayRoster, result.awayBox.possessions)
        val postHome = rollInput.homeRosterPlayers?.let { roster ->
            InjuryOccurrenceEngine.rollAfterGame(
                phase = rollInput.phase,
                minutesByPlayerId = homeMinutes,
                roster = roster.filter { it.playerId !in midHomeIds },
                bank = rollInput.bank,
                tuning = rollInput.occurrence,
                medicalStaffGrade = rollInput.medicalStaffGrade,
                gameSeed = input.seed xor input.home.teamId.hashCode().toLong(),
                existingInjuryCount = result.homeInjuries.size,
            ).injuries
        }.orEmpty()
        val postAway = rollInput.awayRosterPlayers?.let { roster ->
            InjuryOccurrenceEngine.rollAfterGame(
                phase = rollInput.phase,
                minutesByPlayerId = awayMinutes,
                roster = roster.filter { it.playerId !in midAwayIds },
                bank = rollInput.bank,
                tuning = rollInput.occurrence,
                medicalStaffGrade = rollInput.medicalStaffGrade,
                gameSeed = input.seed xor input.away.teamId.hashCode().toLong(),
                existingInjuryCount = result.awayInjuries.size,
            ).injuries
        }.orEmpty()
        return result.copy(
            homeInjuries = result.homeInjuries + postHome,
            awayInjuries = result.awayInjuries + postAway,
        )
    }

    private fun floorMinutesFromResult(
        floorSeconds: Map<String, Int>,
        roster: GameSimRosterInput?,
        possessions: Int,
    ): Map<String, Double> {
        if (floorSeconds.isNotEmpty()) {
            return floorSeconds.mapValues { (_, sec) -> sec / 60.0 }
        }
        return estimateMinutes(roster, possessions)
    }

    fun applyToRoster(
        players: List<RosterPlayer>,
        events: List<GameSimInjuryEvent>,
        context: InjuryApplyContext,
    ): List<RosterPlayer> = InjuryRosterOps.applyGameInjuries(players, events, context)

    private fun estimateMinutes(roster: GameSimRosterInput?, possessions: Int): Map<String, Double> {
        if (roster == null) return emptyMap()
        val scale = possessions.toDouble() / 70.0.coerceAtLeast(1.0)
        return roster.participants
            .groupBy { it.playerId }
            .mapValues { (_, parts) -> parts.sumOf { it.targetMinutes } * scale }
    }

}
