package dev.novux.dynasty.cbb.core.qa

import java.nio.file.Paths
import kotlin.io.path.readText
import org.junit.Assert.assertTrue
import org.junit.Test

class BracketologyNewsPathTest {
    @Test
    fun bracketologyEngineAndFeedWireExist() {
        val root = generateSequence(Paths.get("").toAbsolutePath()) { it.parent }
            .first { it.resolve("android").resolve("settings.gradle.kts").toFile().exists() }
        assertTrue(root.resolve(BracketologyNewsPath.ENGINE).readText().contains(BracketologyNewsPath.MEGA_ID))
        assertTrue(root.resolve(BracketologyNewsPath.POLICY).readText().contains(BracketologyNewsPath.MEGA_ID))
        assertTrue(root.resolve(BracketologyNewsPath.FEED_WIRE).readText().contains("BracketologyNewsEngine"))
        val kind = root.resolve("android/core/src/main/kotlin/dev/novux/dynasty/cbb/core/news/NewsHeadline.kt")
            .readText()
        assertTrue(kind.contains("Bracketology"))
    }
}
