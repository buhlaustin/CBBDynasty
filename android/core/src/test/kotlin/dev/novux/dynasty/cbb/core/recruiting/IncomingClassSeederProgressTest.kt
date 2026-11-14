package dev.novux.dynasty.cbb.core.recruiting

import dev.novux.dynasty.cbb.core.preseason.IncomingClassSeedRunPhase
import org.junit.Assert.assertEquals
import org.junit.Test

class IncomingClassSeederProgressTest {
    @Test
    fun seed_reportsGenerationPhases() {
        val phases = mutableListOf<IncomingClassSeedRunPhase>()
        IncomingClassSeeder.seed(
            enrollingSeasonIndex = 2,
            hsCount = 100,
            jucoCount = 50,
            seedKey = 42,
            onPhase = phases::add,
        )
        assertEquals(
            listOf(
                IncomingClassSeedRunPhase.RollingHighSchoolPool,
                IncomingClassSeedRunPhase.RollingJucoPool,
                IncomingClassSeedRunPhase.RankingNationalRecruits,
            ),
            phases,
        )
    }
}
