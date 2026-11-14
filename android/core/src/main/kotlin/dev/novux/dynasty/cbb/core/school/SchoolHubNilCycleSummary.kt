package dev.novux.dynasty.cbb.core.school

import dev.novux.dynasty.cbb.core.nil.NilBudgetLedger

/** Read-only NIL pool snapshot for user school hub ([MEGA-GDD-114]). */
data class SchoolHubNilCycleSummary(
    val poolAnnualUsd: Long,
    val remainingUsd: Long,
    val committedUsd: Long,
)

object SchoolHubNilCycleSummaryPolicy {
    const val MEGA_ID: String = "MEGA-GDD-114"

    fun from(poolAnnualUsd: Long, ledger: NilBudgetLedger): SchoolHubNilCycleSummary =
        SchoolHubNilCycleSummary(
            poolAnnualUsd = poolAnnualUsd,
            remainingUsd = ledger.nilRemainingUsd,
            committedUsd = ledger.nilCommittedUsd,
        )
}