package dev.novux.dynasty.cbb.core.recruiting

import dev.novux.dynasty.cbb.core.balance.BalanceNilRules
import dev.novux.dynasty.cbb.core.nil.NilAssignCapLinePolicy
import dev.novux.dynasty.cbb.core.nil.NilBudgetLedger

/** Recommended rows assign NIL through ledger with the 3× ask cap ([MEGA-RECR-033]). */
object RecommendedNilAssignHubPolicy {
    const val MEGA_ID: String = "MEGA-RECR-033"

    fun maxAssignableUsd(
        nilAskUsd: Int,
        rules: BalanceNilRules,
        ledger: NilBudgetLedger,
        priorReservationUsd: Long,
    ): Long = NilAssignCapLinePolicy.assignableUsd(nilAskUsd, rules, ledger, priorReservationUsd)
}