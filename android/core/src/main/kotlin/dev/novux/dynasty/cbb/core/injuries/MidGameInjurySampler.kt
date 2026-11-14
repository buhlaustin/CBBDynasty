package dev.novux.dynasty.cbb.core.injuries

import dev.novux.dynasty.cbb.core.balance.BalanceInjuryOccurrence
import dev.novux.dynasty.cbb.core.calendar.CalendarPhase
import dev.novux.dynasty.cbb.core.fatigue.FatigueEngine
import dev.novux.dynasty.cbb.core.roster.RosterPlayer
import dev.novux.dynasty.cbb.core.sim.GameSimClock
import dev.novux.dynasty.cbb.core.sim.SimFloorMinutesLedger
import kotlin.math.pow
import kotlin.random.Random

/**
 * Per-possession injury sample during live sim — [planning/injuries.md#sim-hook].
 */
object MidGameInjurySampler {
    const val POSSESSIONS_PER_TEAM_ESTIMATE: Double = 70.0

    fun formatLogLine(playerLabel: String, injuryLabel: String): String =
        "INJURY — $playerLabel · $injuryLabel"

    fun gameMinuteElapsed(gameClockSeconds: Int): Int =
        (GameSimClock.REGULATION_SECONDS - gameClockSeconds).coerceAtLeast(0) / 60

    fun tryTeamPossession(
        phase: CalendarPhase,
        floorPlayerIds: List<String>,
        minutesLedger: SimFloorMinutesLedger,
        roster: List<RosterPlayer>,
        alreadyInjuredIds: Set<String>,
        injuriesThisTeam: Int,
        bank: List<InjuryBankRow>,
        tuning: BalanceInjuryOccurrence,
        medicalStaffGrade: Int,
        gameClockSeconds: Int,
        possessionIndex: Int,
        teamSeed: Long,
        random: Random,
    ): GameSimInjuryEvent? {
        if (phase == CalendarPhase.Offseason || phase == CalendarPhase.Preseason) return null
        if (injuriesThisTeam >= tuning.maxInjuriesPerTeamPerGame) return null
        if (floorPlayerIds.isEmpty()) return null
        val phaseMultiplier = when (phase) {
            CalendarPhase.Exhibition -> tuning.exhibitionMultiplier
            else -> 1.0
        }
        val staffFactor = (tuning.medicalStaffGradeBaseline / medicalStaffGrade.coerceAtLeast(1).toDouble())
            .coerceIn(0.75, 1.25)
        val pickId = floorPlayerIds[random.nextInt(floorPlayerIds.size)]
        if (pickId in alreadyInjuredIds) return null
        val player = roster.firstOrNull { it.playerId == pickId } ?: return null
        if (player.injuryActive || player.medicalRedshirtSeason) return null
        val minutes = minutesLedger.floorMinutes(pickId)
        if (minutes <= 0.0) return null
        val minutesWeight = (minutes / 32.0).pow(tuning.minutesWeightExponent).coerceIn(0.05, 2.5)
        val historyFactor = if (player.injuryId != null) 1.15 else 1.0
        val fatigueWeight = FatigueEngine.fatigueInjuryWeight(player.stamina, tuning.fatigueWeightExponent)
        val perPossessionBase = tuning.baseChancePerPlayerGame / POSSESSIONS_PER_TEAM_ESTIMATE
        val pInjury = perPossessionBase * phaseMultiplier * minutesWeight *
            staffFactor * historyFactor * fatigueWeight
        if (random.nextDouble() >= pInjury) return null
        val row = InjuryBankPicker.pickWeighted(
            bank,
            teamSeed + possessionIndex + pickId.hashCode(),
        ) ?: return null
        var weeks = row.rollDurationWeeks(teamSeed + possessionIndex)
        if (row.severity == InjurySeverity.Minor && medicalStaffGrade >= tuning.medicalStaffGradeBaseline) {
            weeks = (weeks - 1).coerceAtLeast(1)
        }
        return GameSimInjuryEvent(
            playerId = pickId,
            injuryId = row.id,
            label = row.label,
            bodyPart = row.bodyPart,
            severity = row.severity,
            weeksRemaining = weeks,
            occurredInGameMinute = gameMinuteElapsed(gameClockSeconds),
        )
    }
}
