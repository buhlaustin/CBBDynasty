package dev.novux.dynasty.cbb.core.qa

import java.nio.file.Paths
import kotlin.io.path.readText
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RecruitingPerProspectSpendPathTest {
    @Test
    fun economyRepositoryRecordsPerTargetPointSpend() {
        assertEquals("MEGA-QA-068", RecruitingPerProspectSpendPath.QA_ID)
        val root = generateSequence(Paths.get("").toAbsolutePath()) { it.parent }
            .first { it.resolve("android").resolve("settings.gradle.kts").toFile().exists() }
        val economy = root.resolve(RecruitingPerProspectSpendPath.ECONOMY_REPOSITORY).readText()
        val store = root.resolve(RecruitingPerProspectSpendPath.INTEREST_SPEND_REPOSITORY).readText()
        assertTrue(
            economy.contains(RecruitingPerProspectSpendPath.RECORD_METHOD),
        )
        assertTrue(
            economy.contains(RecruitingPerProspectSpendPath.PERSIST_METHOD) ||
                economy.contains("interestSpendRepository.${RecruitingPerProspectSpendPath.PERSIST_METHOD}"),
        )
        assertTrue(store.contains(RecruitingPerProspectSpendPath.PERSIST_METHOD))
        assertTrue(store.contains("point_spend_"))
    }
}
