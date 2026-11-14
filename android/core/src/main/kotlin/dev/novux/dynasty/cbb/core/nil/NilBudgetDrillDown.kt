package dev.novux.dynasty.cbb.core.nil

data class NilBudgetLineItem(
    val prospectId: String?,
    val label: String,
    val amountUsd: Long,
)

data class NilBudgetDrillDown(
    val ledger: NilBudgetLedger,
    val committedLines: List<NilBudgetLineItem>,
    val reservedLines: List<NilBudgetLineItem>,
)

/**
 * Committed / reserved breakdown for the hub NIL strip sheet
 * ([planning/recruiting-engine.md#nil-budget-strip-recruiting-hub], [MEGA-RECR-094]).
 */
object NilBudgetDrillDownEngine {
    fun build(
        ledger: NilBudgetLedger,
        committedByProspect: Map<String, Long>,
        pendingOffersUsd: Map<String, Long>,
        scholarshipOfferProspectIds: Set<String>,
        prospectDisplayName: (String) -> String,
    ): NilBudgetDrillDown {
        val committedLines = committedByProspect.entries
            .filter { it.value > 0L }
            .sortedByDescending { it.value }
            .map { (id, usd) ->
                NilBudgetLineItem(
                    prospectId = id,
                    label = prospectDisplayName(id),
                    amountUsd = usd,
                )
            }
        val committedSum = committedLines.sumOf { it.amountUsd }
        val committedExtra = (ledger.nilCommittedUsd - committedSum).coerceAtLeast(0L)
        val allCommitted = if (committedExtra > 0L) {
            committedLines + NilBudgetLineItem(
                prospectId = null,
                label = "Other committed",
                amountUsd = committedExtra,
            )
        } else {
            committedLines
        }
        val reservedLines = pendingOffersUsd.entries
            .filter { (id, usd) -> id in scholarshipOfferProspectIds && usd > 0L }
            .sortedByDescending { it.value }
            .map { (id, usd) ->
                NilBudgetLineItem(
                    prospectId = id,
                    label = prospectDisplayName(id),
                    amountUsd = usd,
                )
            }
        return NilBudgetDrillDown(
            ledger = ledger,
            committedLines = allCommitted,
            reservedLines = reservedLines,
        )
    }
}
