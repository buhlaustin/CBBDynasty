package dev.novux.dynasty.cbb.core.nil

import dev.novux.dynasty.cbb.core.balance.BalanceNilRules
import kotlin.math.roundToLong

/**
 * NIL assign supports partial offers between $0 and the 3× ask cap ([MEGA-RECR-093]).
 */
object RecruitNilPartialOfferPolicy {
    const val MEGA_ID: String = "MEGA-RECR-093"

    fun partialOfferUsd(
        nilAskUsd: Int,
        rules: BalanceNilRules,
        fractionOfCap: Float,
    ): Long {
        val cap = NilAssignCapLinePolicy.maxOfferUsd(nilAskUsd, rules)
        val clamped = fractionOfCap.coerceIn(0f, 1f)
        return (cap * clamped).roundToLong()
    }
}