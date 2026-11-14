package dev.novux.dynasty.cbb.core.coach.contract

import dev.novux.dynasty.cbb.core.coach.CoachArchetype
import kotlin.math.roundToLong

/** Computes head coach annual salary at signing or roster build ([MEGA-COACH-118]). */
object HeadCoachContractSalary {
    private const val BASE_SALARY_USD = 450_000L

    fun annualUsd(archetype: CoachArchetype, programPrestige: Int): Long {
        val prestigeMultiplier = 0.6 + (programPrestige.coerceIn(1, 99) / 99.0) * 1.4
        val archetypeBonus = when (archetype.name) {
            "Recruiter" -> 1.05
            "SchemeManager" -> 1.08
            "ProgramBuilder" -> 1.1
            else -> 1.0
        }
        return (BASE_SALARY_USD * prestigeMultiplier * archetypeBonus).roundToLong()
    }
}
