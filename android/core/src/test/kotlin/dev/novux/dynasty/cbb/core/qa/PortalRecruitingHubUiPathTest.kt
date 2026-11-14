package dev.novux.dynasty.cbb.core.qa

import java.nio.file.Paths
import kotlin.io.path.readText
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PortalRecruitingHubUiPathTest {
    @Test
    fun recruitingHubUsesPortalTabBody() {
        val root = generateSequence(Paths.get("").toAbsolutePath()) { it.parent }
            .first { it.resolve("android").resolve("settings.gradle.kts").toFile().exists() }
        assertTrue(root.resolve(PortalRecruitingHubUiPath.HUB).readText().contains(PortalRecruitingHubUiPath.MEGA_ID))
        val hub = root.resolve(PortalRecruitingHubUiPath.HUB_SCREEN).readText()
        assertTrue(hub.contains("PortalRecruitingTabBody"))
        val tabBody = root.resolve(PortalRecruitingHubUiPath.TAB_BODY).readText()
        assertTrue(tabBody.contains("PortalTop40Section"))
        assertFalse(hub.contains("recruiting_placeholder_portal"))
    }
}