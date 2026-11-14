package dev.novux.dynasty.cbb.core.league

import dev.novux.dynasty.cbb.core.balance.BalanceInjuryOccurrence
import dev.novux.dynasty.cbb.core.calendar.CalendarPhase
import dev.novux.dynasty.cbb.core.injuries.InjuryBankRow
import dev.novux.dynasty.cbb.core.sim.tuning.SimTuning

/** Possession sim + injury bank for AI league batch weeks ([planning/injuries.md], MEGA-SIM-163/176). */
data class LeagueBatchSimParams(
    val tuning: SimTuning,
    val programPrestigeByTeamId: Map<String, Int>,
    val phase: CalendarPhase,
    val injuryBank: List<InjuryBankRow>,
    val injuryOccurrence: BalanceInjuryOccurrence,
    val medicalStaffGrade: Int = 70,
) {
    fun prestigeFor(teamId: String): Int = programPrestigeByTeamId[teamId] ?: 55
}
