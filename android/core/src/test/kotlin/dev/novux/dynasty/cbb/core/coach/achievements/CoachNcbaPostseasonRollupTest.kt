package dev.novux.dynasty.cbb.core.coach.achievements

import org.junit.Assert.assertEquals
import org.junit.Test

class CoachNcbaPostseasonRollupTest {
    @Test
    fun mapsFromHeadCoachAchievements() {
        val achievements = HeadCoachAchievements(
            recordLabel = "20-10",
            winPercentLabel = "66.6%",
            ncbaTourneyRecordLabel = "3-1",
            ncbaTourneyWinPercentLabel = "75.0%",
            nationalTitles = 1,
            finalFours = 2,
            ncbaAppearances = 4,
            confRegTitles = 0,
            confTourneyTitles = 1,
            nciRecordLabel = "0-0",
            nciTitles = 0,
            heritageRecordLabel = "0-0",
            heritageTitles = 0,
        )
        val rollup = CoachNcbaPostseasonRollup.from(achievements)
        assertEquals("3-1", rollup.tourneyRecordLabel)
        assertEquals(1, rollup.nationalTitles)
        assertEquals(2, rollup.finalFours)
        assertEquals(4, rollup.ncbaAppearances)
    }
}
