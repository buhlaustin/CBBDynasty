package dev.novux.dynasty.cbb.core.coach.achievements

import org.junit.Assert.assertEquals
import org.junit.Test

class HeadCoachAchievementsSeasonFallbackTest {
    @Test
    fun fromSeasonRecordLabelParsesWinPct() {
        val snap = HeadCoachAchievements.fromSeasonRecordLabel("18-6")
        assertEquals("18-6", snap.recordLabel)
        assertEquals("75.0%", snap.winPercentLabel)
    }
}
