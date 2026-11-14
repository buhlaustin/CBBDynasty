package dev.novux.dynasty.cbb.core.qa

import dev.novux.dynasty.cbb.core.recruiting.EliteHiddenOnRecommendedPolicy
import dev.novux.dynasty.cbb.core.recruiting.InfluenceBoardSurfacePolicy
import dev.novux.dynasty.cbb.core.recruiting.OfficialVisitWindowPolicy
import dev.novux.dynasty.cbb.core.recruiting.RecommendedNilAssignHubPolicy
import dev.novux.dynasty.cbb.core.recruiting.SignLikelihoodSeededPoolPolicy
import java.nio.file.Paths
import kotlin.io.path.readText
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RecruitingRecommendedBatchPathTest {
    @Test
    fun hubDocumentsRecommendedPolicies() {
        assertEquals("MEGA-RECR-032", EliteHiddenOnRecommendedPolicy.MEGA_ID)
        assertEquals("MEGA-RECR-030", InfluenceBoardSurfacePolicy.MEGA_ID)
        assertEquals("MEGA-RECR-033", RecommendedNilAssignHubPolicy.MEGA_ID)
        assertEquals("MEGA-RECR-040", OfficialVisitWindowPolicy.MEGA_ID)
        assertEquals("MEGA-RECR-028", SignLikelihoodSeededPoolPolicy.MEGA_ID)
        val hub = repoRoot().resolve(
            "android/app/src/main/kotlin/dev/novux/dynasty/cbb/ui/recruiting/RecruitingHubScreen.kt",
        ).readText()
        assertTrue(hub.contains("RecommendedRecruitList.build"))
        assertTrue(hub.contains("recruiting_recommended_elite_hide_hint"))
        assertTrue(hub.contains("recruiting_influence_board_hint"))
        assertTrue(hub.contains("recruiting_nil_recommended_assign_hint"))
        assertTrue(hub.contains("recruiting_official_visit_window_hint"))
        assertTrue(hub.contains("recruiting_influence_row"))
    }

    private fun repoRoot() =
        generateSequence(Paths.get("").toAbsolutePath()) { it.parent }
            .first { it.resolve("android").resolve("settings.gradle.kts").toFile().exists() }
}