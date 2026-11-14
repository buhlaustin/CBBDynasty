package dev.novux.dynasty.cbb.core.qa

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PlaySignedAabIntegrityPathTest {
    @Test
    fun automatedChecks_coverBundleAndApkSigning() {
        val ids = PlaySignedAabIntegrityPath.AUTOMATED_CHECKS.map { it.id }
        assertTrue(ids.contains("bundletool_validate"))
        assertTrue(ids.contains("jarsigner_verify"))
        assertEquals(PlaySignedAabIntegrityPath.QA_ID, "MEGA-QA-137")
    }
}
