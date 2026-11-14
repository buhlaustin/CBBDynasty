package dev.novux.dynasty.cbb.core.qa

import java.nio.file.Paths
import kotlin.io.path.readText
import org.junit.Assert.assertTrue
import org.junit.Test

class PlayerCardMultiTabPathTest {
    @Test
    fun previewHintsFullTabs() {
        val root = generateSequence(Paths.get("").toAbsolutePath()) { it.parent }
            .first { it.resolve("android").resolve("settings.gradle.kts").toFile().exists() }
        assertTrue(
            root.resolve(
                "android/app/src/main/kotlin/dev/novux/dynasty/cbb/ui/player/PlayerCardSheet.kt",
            ).readText().contains("player_card_multi_tab_hint"),
        )
        assertTrue(
            root.resolve(
                "android/app/src/main/kotlin/dev/novux/dynasty/cbb/ui/player/PlayerCardScreen.kt",
            ).readText().contains("PrimaryTabRow"),
        )
    }
}