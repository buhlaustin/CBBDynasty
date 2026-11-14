package dev.novux.dynasty.cbb.core.save

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DynastyWizardBootstrapPolicyTest {
    @Test
    fun seasonOneSign_includesNilAnnualCycle() {
        val steps = DynastyWizardBootstrapPolicy.stepsAfterSign(seasonIndex = 1)
        assertTrue(DynastyWizardBootstrapPolicy.Step.NilAnnualCyclePool in steps)
        assertTrue(DynastyWizardBootstrapPolicy.requiresNilCycleAtSign(1))
    }

    @Test
    fun laterSeasonSign_skipsWizardBootstrap() {
        assertEquals(emptyList<DynastyWizardBootstrapPolicy.Step>(), DynastyWizardBootstrapPolicy.stepsAfterSign(2))
        assertFalse(DynastyWizardBootstrapPolicy.requiresNilCycleAtSign(2))
    }
}
