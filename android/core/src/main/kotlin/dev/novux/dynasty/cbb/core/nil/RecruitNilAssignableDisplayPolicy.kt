package dev.novux.dynasty.cbb.core.nil

import dev.novux.dynasty.cbb.core.balance.BalanceNilRules

/** Shows budget-limited assignable NIL alongside the 3× ask cap ([MEGA-RECR-096]). */
object RecruitNilAssignableDisplayPolicy {
    const val MEGA_ID: String = "MEGA-RECR-096"

    fun assignableUsd(
        nilAskUsd: Int,
        rules: BalanceNilRules,
        ledger: NilBudgetLedger,
        priorReservationUsd: Long,
    ): Long = NilOfferEngine.maxAssignableUsd(nilAskUsd, rules, ledger, priorReservationUsd)
}