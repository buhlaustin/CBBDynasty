package dev.novux.dynasty.cbb.core.injuries

import dev.novux.dynasty.cbb.core.balance.BalanceInjuryBankEntry
import dev.novux.dynasty.cbb.core.balance.BalanceInjuryOccurrence
import dev.novux.dynasty.cbb.core.calendar.CalendarPhase
import dev.novux.dynasty.cbb.core.fatigue.FatigueEngine
import dev.novux.dynasty.cbb.core.roster.RosterPlayer
import kotlin.math.pow

/**
 * Random injury rolls at game end — [planning/injuries.md#occurrence].
 */
object InjuryOccurrenceEngine {
    fun bankFromBalance(entries: List<BalanceInjuryBankEntry>): List<InjuryBankRow> =
        entries.mapNotNull { InjuryBankRow.fromBalance(it) }

    fun rollAfterGame(
        phase: CalendarPhase,
        minutesByPlayerId: Map<String, Double>,
        roster: List<RosterPlayer>,
        bank: List<InjuryBankRow>,
        tuning: BalanceInjuryOccurrence,
        medicalStaffGrade: Int,
        gameSeed: Long,
        existingInjuryCount: Int = 0,
    ): GameSimInjuryResult {
        if (phase == CalendarPhase.Offseason || phase == CalendarPhase.Preseason) {
            return GameSimInjuryResult()
        }
        val phaseMultiplier = when (phase) {
            CalendarPhase.Exhibition -> tuning.exhibitionMultiplier
            else -> 1.0
        }
        val staffFactor = (tuning.medicalStaffGradeBaseline / medicalStaffGrade.coerceAtLeast(1).toDouble())
            .coerceIn(0.75, 1.25)
        val candidates = roster.filter { player ->
            (minutesByPlayerId[player.playerId] ?: 0.0) > 0.0 && !player.injuryActive && !player.medicalRedshirtSeason
        }
        val events = mutableListOf<GameSimInjuryEvent>()
        var injuryCount = existingInjuryCount
        candidates.forEachIndexed { index, player ->
            if (injuryCount >= tuning.maxInjuriesPerTeamPerGame) return@forEachIndexed
            val minutes = minutesByPlayerId[player.playerId] ?: 0.0
            val minutesWeight = (minutes / 32.0).pow(tuning.minutesWeightExponent).coerceIn(0.05, 2.5)
            val historyFactor = if (player.injuryId != null) 1.15 else 1.0
            val fatigueWeight = FatigueEngine.fatigueInjuryWeight(player.stamina, tuning.fatigueWeightExponent)
            val pInjury = tuning.baseChancePerPlayerGame * phaseMultiplier * minutesWeight *
                staffFactor * historyFactor * fatigueWeight
            val roll = ((gameSeed + index * 997 + player.playerId.hashCode()) % 10_000) / 10_000.0
            if (roll >= pInjury) return@forEachIndexed
            val row = InjuryBankPicker.pickWeighted(
                bank,
                gameSeed + player.playerId.hashCode(),
            ) ?: return@forEachIndexed
            var weeks = row.rollDurationWeeks(gameSeed + index)
            if (row.severity == InjurySeverity.Minor && medicalStaffGrade >= tuning.medicalStaffGradeBaseline) {
                weeks = (weeks - 1).coerceAtLeast(1)
            }
            events += GameSimInjuryEvent(
                playerId = player.playerId,
                injuryId = row.id,
                label = row.label,
                bodyPart = row.bodyPart,
                severity = row.severity,
                weeksRemaining = weeks,
            )
            injuryCount++
        }
        return GameSimInjuryResult(events)
    }
}
