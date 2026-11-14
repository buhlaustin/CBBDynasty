package dev.novux.dynasty.cbb.core.qa

import java.nio.file.Paths
import kotlin.io.path.readText
import org.junit.Assert.assertTrue
import org.junit.Test

class PlayerPromiseAtRiskPathTest {
    @Test
    fun atRiskRefreshPersistenceAndSurfaces() {
        val root = generateSequence(Paths.get("").toAbsolutePath()) { it.parent }
            .first { it.resolve("android").resolve("settings.gradle.kts").toFile().exists() }
        val engine = root.resolve(PlayerPromiseAtRiskPath.ENGINE).readText()
        assertTrue(engine.contains(PlayerPromiseAtRiskPath.MEGA_ID))
        assertTrue(engine.contains("PromiseStatus.AtRisk"))
        assertTrue(root.resolve(PlayerPromiseAtRiskPath.REPO).readText().contains("refreshInSeasonAfterWeek"))
        assertTrue(root.resolve(PlayerPromiseAtRiskPath.DASHBOARD).readText().contains("PromiseAtRisk"))
        val ui = root.resolve(PlayerPromiseAtRiskPath.UI).readText()
        assertTrue(ui.contains(PlayerPromiseAtRiskPath.MEGA_ID))
        assertTrue(ui.contains("PromiseStatus.AtRisk"))
    }
}
