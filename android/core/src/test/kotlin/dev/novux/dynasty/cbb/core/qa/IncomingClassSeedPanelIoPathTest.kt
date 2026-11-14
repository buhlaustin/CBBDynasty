package dev.novux.dynasty.cbb.core.qa

import java.nio.file.Paths
import kotlin.io.path.readText
import org.junit.Assert.assertTrue
import org.junit.Test

class IncomingClassSeedPanelIoPathTest {
    @Test
    fun panelWrapsSeedRepositoryCallsInIoDispatcher() {
        val root = generateSequence(Paths.get("").toAbsolutePath()) { it.parent }
            .first { it.resolve("android").resolve("settings.gradle.kts").toFile().exists() }
        val source = root.resolve(IncomingClassSeedPanelIoPath.PANEL_SOURCE).readText()
        IncomingClassSeedPanelIoPath.GUARDED_CALLS.forEach { call ->
            assertTrue(
                "Expected `$call` guarded by withContext(${IncomingClassSeedPanelIoPath.REQUIRED_DISPATCHER})",
                source.contains("withContext(${IncomingClassSeedPanelIoPath.REQUIRED_DISPATCHER})") &&
                    source.contains(call),
            )
        }
        assertTrue(source.contains("try") && source.contains("finally"))
    }
}
