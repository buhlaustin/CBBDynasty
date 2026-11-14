package dev.novux.dynasty.cbb.core.qa

import java.nio.file.Paths
import kotlin.io.path.readText
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RecruitingHubTabSetPathTest {
    @Test
    fun hubExposesFullTabSet() {
        assertEquals("MEGA-RECR-037", RecruitingHubTabSetPath.RECR_ID)
        val root = generateSequence(Paths.get("").toAbsolutePath()) { it.parent }
            .first { it.resolve("android").resolve("settings.gradle.kts").toFile().exists() }
        val hub = root.resolve(RecruitingHubTabSetPath.HUB_SCREEN).readText()
        assertTrue(hub.contains(RecruitingHubTabSetPath.SCROLLABLE_ROW))
        assertTrue(hub.contains("RecruitingHubTab.BoardTargets"))
        assertTrue(hub.contains("RecruitingHubTab.Committed"))
        assertTrue(hub.contains("RecruitingHubTab.Lost"))
        assertTrue(hub.contains("RecruitingHubTab.HighSchool"))
        assertTrue(hub.contains("RecruitingHubTab.JuniorCollege"))
        val policy = root.resolve(RecruitingHubTabSetPath.TAB_POLICY).readText()
        assertTrue(policy.contains(RecruitingHubTabSetPath.RECR_ID))
    }
}
