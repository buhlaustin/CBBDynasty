package dev.novux.dynasty.cbb.core.qa

import java.nio.file.Paths
import kotlin.io.path.readText
import org.junit.Assert.assertTrue
import org.junit.Test

class CoachNcbaPostseasonRollupPathTest {
    @Test
    fun ncbaPostseasonRollupOnOverviewAchievements() {
        val root = generateSequence(Paths.get("").toAbsolutePath()) { it.parent }
            .first { it.resolve("android").resolve("settings.gradle.kts").toFile().exists() }
        assertTrue(root.resolve(CoachNcbaPostseasonRollupPath.ROLLUP).readText().contains(CoachNcbaPostseasonRollupPath.MEGA_ID))
        assertTrue(root.resolve(CoachNcbaPostseasonRollupPath.UI).readText().contains("CoachOverviewNcbaPostseasonBlock"))
        assertTrue(
            root.resolve(CoachNcbaPostseasonRollupPath.ACHIEVEMENTS_CARD).readText()
                .contains("CoachOverviewNcbaPostseasonBlock"),
        )
    }
}
