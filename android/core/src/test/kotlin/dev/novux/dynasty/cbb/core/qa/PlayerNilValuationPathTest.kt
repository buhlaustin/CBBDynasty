package dev.novux.dynasty.cbb.core.qa

import dev.novux.dynasty.cbb.core.player.nil.PlayerNilValuationPolicy
import java.nio.file.Paths
import kotlin.io.path.readText
import org.junit.Assert.assertTrue
import org.junit.Test

class PlayerNilValuationPathTest {
    @Test
    fun nilValuationOnPlayerOverviewContentAndPreviewSheet() {
        val root = generateSequence(Paths.get("").toAbsolutePath()) { it.parent }
            .first { it.resolve("android").resolve("settings.gradle.kts").toFile().exists() }
        val sheet = root.resolve("android/app/src/main/kotlin/dev/novux/dynasty/cbb/ui/player/PlayerCardSheet.kt")
            .readText()
        val line = root.resolve("android/app/src/main/kotlin/dev/novux/dynasty/cbb/ui/player/PlayerNilValuationLine.kt")
            .readText()
        assertTrue(sheet.contains(PlayerNilValuationPolicy.OVERVIEW_COMPOSABLE))
        assertTrue(line.contains(PlayerNilValuationPolicy.QA_ID))
        assertTrue(line.contains("nilUsdPerYear"))
    }
}
