package dev.novux.dynasty.cbb.core.nil

import dev.novux.dynasty.cbb.core.qa.NilBudgetDrillDownPath
import org.junit.Assert.assertEquals
import org.junit.Test

class NilBudgetDrillDownEngineTest {
    @Test
    fun megaRecr094_reservedOnlyOpenScholarshipOffers() {
        assertEquals(NilBudgetDrillDownPath.RECR_ID, "MEGA-RECR-094")
        val ledger = NilBudgetLedgerCalculator.compute(
            poolAnnualUsd = 5_000_000,
            committedUsd = 1_000_000,
            pendingOffersUsd = mapOf("a" to 200_000L, "b" to 100_000L),
            scholarshipOfferProspectIds = setOf("a"),
        )
        val drill = NilBudgetDrillDownEngine.build(
            ledger = ledger,
            committedByProspect = mapOf("signed-1" to 1_000_000L),
            pendingOffersUsd = mapOf("a" to 200_000L, "b" to 100_000L),
            scholarshipOfferProspectIds = setOf("a"),
            prospectDisplayName = { id -> "Prospect $id" },
        )
        assertEquals(1, drill.reservedLines.size)
        assertEquals(200_000L, drill.reservedLines.single().amountUsd)
        assertEquals("Prospect a", drill.reservedLines.single().label)
        assertEquals(1_000_000L, drill.committedLines.single().amountUsd)
    }
}
