package dev.novux.dynasty.cbb.core.recruiting

import org.junit.Assert.assertEquals
import org.junit.Test

class RecruitingProspectPointSpendLedgerTest {
    @Test
    fun dmTwoProspects_keepsSeparateTotals() {
        var ledger = emptyMap<String, Int>()
        ledger = RecruitingProspectPointSpendLedger.record(ledger, "prospect-a", 50)
        ledger = RecruitingProspectPointSpendLedger.record(ledger, "prospect-b", 50)
        assertEquals(50, RecruitingProspectPointSpendLedger.spentOn(ledger, "prospect-a"))
        assertEquals(50, RecruitingProspectPointSpendLedger.spentOn(ledger, "prospect-b"))
        assertEquals(100, RecruitingProspectPointSpendLedger.totalSpent(ledger))
    }

    @Test
    fun codecRoundTrip() {
        val map = mapOf("a" to 50, "b" to 125)
        val raw = RecruitingProspectPointSpendLedger.encode(map)
        assertEquals(map, RecruitingProspectPointSpendLedger.decode(raw))
    }
}
