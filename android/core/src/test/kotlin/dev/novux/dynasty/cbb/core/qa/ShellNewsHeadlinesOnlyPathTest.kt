package dev.novux.dynasty.cbb.core.qa

import java.nio.file.Paths
import kotlin.io.path.readText
import org.junit.Assert.assertTrue
import org.junit.Test

class ShellNewsHeadlinesOnlyPathTest {
    @Test
    fun newsTabDocumentsHeadlinesOnly() {
        val root = generateSequence(Paths.get("").toAbsolutePath()) { it.parent }
            .first { it.resolve("android").resolve("settings.gradle.kts").toFile().exists() }
        val tab = root.resolve(
            "android/app/src/main/kotlin/dev/novux/dynasty/cbb/ui/dynasty/DynastyShellTabContent.kt",
        ).readText()
        assertTrue(tab.contains("DynastyShellNewsHeadlinesPolicy"))
        assertTrue(tab.contains("shell_news_headlines_only_hint"))
    }
}