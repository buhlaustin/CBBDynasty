package dev.novux.dynasty.cbb.core.recruiting

/**
 * Per-target recruiting **point** spend for the active cycle ([MEGA-QA-068], [MEGA-RECR-022]).
 *
 * Distinct from cycle-wide [RecruitingCycleLedger.pointsAvailable] and from interest-only
 * [RecruitInterestSpendDelta] attribution.
 */
object RecruitingProspectPointSpendLedger {
    const val QA_ID: String = "MEGA-QA-068"

    fun record(current: Map<String, Int>, prospectId: String, pointsCharged: Int): Map<String, Int> {
        if (pointsCharged <= 0) return current
        val next = current.toMutableMap()
        next[prospectId] = (next[prospectId] ?: 0) + pointsCharged
        return next
    }

    fun spentOn(ledger: Map<String, Int>, prospectId: String): Int = ledger[prospectId] ?: 0

    fun totalSpent(ledger: Map<String, Int>): Int = ledger.values.sum()

    fun encode(ledger: Map<String, Int>): String =
        ledger.entries.joinToString(";") { "${it.key}:${it.value}" }

    fun decode(raw: String): Map<String, Int> =
        if (raw.isBlank()) {
            emptyMap()
        } else {
            raw.split(";").mapNotNull { token ->
                val idx = token.indexOf(':')
                if (idx <= 0) return@mapNotNull null
                val amount = token.substring(idx + 1).toIntOrNull() ?: return@mapNotNull null
                token.substring(0, idx) to amount
            }.toMap()
        }
}
