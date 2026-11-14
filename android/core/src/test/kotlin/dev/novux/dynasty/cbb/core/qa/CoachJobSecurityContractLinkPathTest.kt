package dev.novux.dynasty.cbb.core.qa

import java.nio.file.Paths
import kotlin.io.path.readText
import org.junit.Assert.assertTrue
import org.junit.Test

class CoachJobSecurityContractLinkPathTest {
    @Test
    fun overviewJobSecurityNavigatesToContractTab() {
        val root = generateSequence(Paths.get("").toAbsolutePath()) { it.parent }
            .first { it.resolve("android").resolve("settings.gradle.kts").toFile().exists() }
        val meter = root.resolve(CoachJobSecurityContractLinkPath.METER).readText()
        assertTrue(meter.contains(CoachJobSecurityContractLinkPath.NAV_PARAM))
        assertTrue(meter.contains("clickable"))
        val screen = root.resolve(CoachJobSecurityContractLinkPath.HEAD_SCREEN).readText()
        assertTrue(screen.contains("CoachCardTab.Contract"))
        assertTrue(screen.contains("onOpenContractTab"))
    }
}
