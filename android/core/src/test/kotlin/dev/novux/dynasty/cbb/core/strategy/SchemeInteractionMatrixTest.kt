package dev.novux.dynasty.cbb.core.strategy

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SchemeInteractionMatrixTest {
    @Test
    fun defaultMatrixCoversAll7x7Presets() {
        val keys = SchemeInteractionMatrix.defaultMatrix.keys
        assertEquals(49, keys.size)
        OffensiveSchemePreset.entries.forEach { off ->
            DefensiveSchemePreset.entries.forEach { def ->
                assertTrue("$off vs $def", keys.contains(off to def))
            }
        }
    }

    @Test
    fun tuningJsonDeclares49Cells() {
        val stream = checkNotNull(javaClass.classLoader.getResourceAsStream("sim/scheme-interaction-matrix.json"))
        val raw = stream.bufferedReader().readText()
        assertEquals(49, Regex("\"offense\"").findAll(raw).count())
    }
}
