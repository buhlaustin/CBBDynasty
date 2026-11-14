package dev.novux.dynasty.cbb.core.qa

import java.nio.file.Paths
import kotlin.io.path.readText
import org.junit.Assert.assertTrue
import org.junit.Test

class DashboardTaskBadgePathTest {
    @Test
    fun requiredAndOptionalPanelsShowKindChip() {
        val root = repoRoot()
        val chip = root.resolve(DashboardTaskBadgePath.CHIP).readText()
        assertTrue(chip.contains(DashboardTaskBadgePath.MEGA_ID))
        assertTrue(root.resolve(DashboardTaskBadgePath.REQUIRED_PANEL).readText().contains("DashboardTaskKindChip"))
        assertTrue(root.resolve(DashboardTaskBadgePath.OPTIONAL_PANEL).readText().contains("DashboardTaskKindChip"))
    }

    private fun repoRoot() =
        generateSequence(Paths.get("").toAbsolutePath()) { it.parent }
            .first { it.resolve("android").resolve("settings.gradle.kts").toFile().exists() }
}
