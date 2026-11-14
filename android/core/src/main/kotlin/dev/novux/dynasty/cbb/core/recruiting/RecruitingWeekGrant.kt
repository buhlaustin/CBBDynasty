package dev.novux.dynasty.cbb.core.recruiting

import dev.novux.dynasty.cbb.core.academics.AcademicsRecruitingBudgetEngine
import dev.novux.dynasty.cbb.core.balance.BalanceRecruitingEconomy
import dev.novux.dynasty.cbb.core.calendar.CalendarPhase
import kotlin.math.roundToInt

data class RecruitingCycleLedger(
    val cycleSeasonIndex: Int,
    val programPrestige: Int,
    val weeklyPoolBase: Int,
    val pointsAvailable: Int,
    val unspentAtLastWeekEnd: Int = 0,
    val cycleClosed: Boolean = false,
    val programAcademicsGrade: Int? = null,
    val academicsBudgetMultiplier: Double = 1.0,
) {
    fun withPointsAvailable(next: Int): RecruitingCycleLedger = copy(pointsAvailable = next.coerceAtLeast(0))
}

data class WeekGrantBreakdown(
    val weeklyBase: Int,
    val crunchMultiplier: Double,
    val grantedThisWeek: Int,
    val carryoverFromPrior: Int,
    val newBalance: Int,
)

/**
 * Year-long economy — 50% carryover, offseason W2–5 crunch ([planning/recruiting-engine.md]).
 */
object RecruitingWeekGrant {
    fun openCycle(
        economy: BalanceRecruitingEconomy,
        cycleSeasonIndex: Int,
        programPrestige: Int,
        programAcademicsGrade: Int = AcademicsRecruitingBudgetEngine.BASELINE_GRADE,
    ): RecruitingCycleLedger {
        val tierBase = RecruitingPrestigePool.weeklyPoolPoints(economy, programPrestige)
        val multiplier = AcademicsRecruitingBudgetEngine.budgetMultiplier(programAcademicsGrade)
        val base = AcademicsRecruitingBudgetEngine.effectiveWeeklyPool(tierBase, programAcademicsGrade)
        return RecruitingCycleLedger(
            cycleSeasonIndex = cycleSeasonIndex,
            programPrestige = programPrestige,
            weeklyPoolBase = base,
            pointsAvailable = base,
            programAcademicsGrade = programAcademicsGrade,
            academicsBudgetMultiplier = multiplier,
        )
    }

    fun applyWeekAdvanceGrant(
        economy: BalanceRecruitingEconomy,
        ledger: RecruitingCycleLedger,
        phase: CalendarPhase,
        offseasonWeekIndex: Int?,
        pointsSpentThisWeek: Int,
    ): Pair<RecruitingCycleLedger, WeekGrantBreakdown> {
        if (RecruitingClosedCycleWeekGrantPolicy.skipsWeeklyGrant(ledger)) {
            val breakdown = WeekGrantBreakdown(
                weeklyBase = ledger.weeklyPoolBase,
                crunchMultiplier = 1.0,
                grantedThisWeek = 0,
                carryoverFromPrior = 0,
                newBalance = ledger.pointsAvailable,
            )
            return ledger to breakdown
        }
        val unspent = (ledger.pointsAvailable - pointsSpentThisWeek).coerceAtLeast(0)
        val carryover = (unspent * economy.weeklyCarryoverFraction).roundToInt()
        val multiplier = signingCrunchMultiplier(economy, phase, offseasonWeekIndex)
        val granted = (ledger.weeklyPoolBase * multiplier).roundToInt()
        val newBalance = granted + carryover
        val next = ledger.copy(
            pointsAvailable = newBalance,
            unspentAtLastWeekEnd = unspent,
        )
        val breakdown = WeekGrantBreakdown(
            weeklyBase = ledger.weeklyPoolBase,
            crunchMultiplier = multiplier,
            grantedThisWeek = granted,
            carryoverFromPrior = carryover,
            newBalance = newBalance,
        )
        return next to breakdown
    }

    fun signingCrunchMultiplier(
        economy: BalanceRecruitingEconomy,
        phase: CalendarPhase,
        offseasonWeekIndex: Int?,
    ): Double {
        if (phase != CalendarPhase.Offseason || offseasonWeekIndex == null) return 1.0
        return if (offseasonWeekIndex in economy.signingCrunchOffseasonWeeks) {
            economy.signingCrunchMultiplier
        } else {
            1.0
        }
    }

    /** Next cycle pool after End of Season prestige recap. */
    fun previewNextCycleWeeklyPool(
        economy: BalanceRecruitingEconomy,
        postRecapPrestige: Int,
        programAcademicsGrade: Int = AcademicsRecruitingBudgetEngine.BASELINE_GRADE,
    ): Int {
        val tierBase = RecruitingPrestigePool.weeklyPoolPoints(economy, postRecapPrestige)
        return AcademicsRecruitingBudgetEngine.effectiveWeeklyPool(tierBase, programAcademicsGrade)
    }
}
