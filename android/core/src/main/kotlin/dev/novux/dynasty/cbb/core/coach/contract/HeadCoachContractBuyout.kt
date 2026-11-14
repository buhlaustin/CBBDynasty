package dev.novux.dynasty.cbb.core.coach.contract

import kotlin.math.roundToLong

object HeadCoachContractBuyout {
    private const val REMAINING_YEARS_FACTOR = 0.65

    fun usd(annualSalaryUsd: Long, contractYearsLeft: Int): Long =
        (annualSalaryUsd * contractYearsLeft.coerceAtLeast(1) * REMAINING_YEARS_FACTOR).roundToLong()
}
