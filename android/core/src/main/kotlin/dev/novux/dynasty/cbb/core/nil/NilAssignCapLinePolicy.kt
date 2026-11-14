package dev.novux.dynasty.cbb.core.nil

import dev.novux.dynasty.cbb.core.balance.BalanceNilRules

/** Surfaces the 3× ask cap on recruit / retention flows ([MEGA-GDD-030]). */
object NilAssignCapLinePolicy {
    const val MEGA_ID: String = "MEGA-GDD-030"

    fun maxOfferUsd(nilAskUsd: Int, rules: BalanceNilRules): Long =
        NilOfferEngine.capFromAsk(nilAskUsd, rules)

    fun assignableUsd(
        nilAskUsd: Int,
        rules: BalanceNilRules,
        ledger: NilBudgetLedger,
        priorReservationUsd: Long,
    ): Long = NilOfferEngine.maxAssignableUsd(nilAskUsd, rules, ledger, priorReservationUsd)
}