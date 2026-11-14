package dev.novux.dynasty.cbb.core.qa

import java.nio.file.Paths
import kotlin.io.path.readText
import org.junit.Assert.assertTrue
import org.junit.Test

class PlayerCardPersistentHeaderPathTest {
    @Test
    fun persistentHeaderOnFullCardAndPreviewSheet() {
        val root = generateSequence(Paths.get("").toAbsolutePath()) { it.parent }
            .first { it.resolve("android").resolve("settings.gradle.kts").toFile().exists() }
        assertTrue(root.resolve(PlayerCardPersistentHeaderPath.HEADER).readText().contains("PlayerCardPersistentHeader"))
        assertTrue(
            root.resolve("android/core/src/main/kotlin/dev/novux/dynasty/cbb/core/player/card/PlayerCardPersistentHeaderPolicy.kt")
                .readText()
                .contains(PlayerCardPersistentHeaderPath.MEGA_ID),
        )
        assertTrue(
            root.resolve(PlayerCardPersistentHeaderPath.SCREEN).readText()
                .contains("PlayerCardPersistentHeader"),
        )
        assertTrue(
            root.resolve(PlayerCardPersistentHeaderPath.SHEET).readText()
                .contains("PlayerCardPersistentHeader"),
        )
    }
}
