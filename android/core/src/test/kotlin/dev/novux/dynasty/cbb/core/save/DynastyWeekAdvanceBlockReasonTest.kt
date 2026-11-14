package dev.novux.dynasty.cbb.core.save

import org.junit.Assert.assertSame
import org.junit.Test

class DynastyWeekAdvanceBlockReasonTest {
    @Test
    fun disciplinePending_isStableEnum() {
        assertSame(DynastyWeekAdvanceBlockReason.DisciplinePending, DynastyWeekAdvanceBlockReason.valueOf("DisciplinePending"))
    }
}
