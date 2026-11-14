package dev.novux.dynasty.cbb.core.qa

import java.nio.file.Paths
import kotlin.io.path.readText
import org.junit.Assert.assertTrue
import org.junit.Test

class PlayerCardIdentityStripPathTest {
    @Test
    fun identityStripChipsOnPlayerCard() {
        val root = generateSequence(Paths.get("").toAbsolutePath()) { it.parent }
            .first { it.resolve("android").resolve("settings.gradle.kts").toFile().exists() }
        val strip = root.resolve(PlayerCardIdentityStripPath.STRIP).readText()
        assertTrue(strip.contains("PlayerCardIdentityStripChips"))
        assertTrue(
            root.resolve("android/core/src/main/kotlin/dev/novux/dynasty/cbb/core/player/card/PlayerCardIdentityStripPolicy.kt")
                .readText()
                .contains(PlayerCardIdentityStripPath.MEGA_ID),
        )
        assertTrue(strip.contains("injury_roster_out_chip"))
        assertTrue(root.resolve(PlayerCardIdentityStripPath.SCREEN).readText().contains("PlayerCardIdentityStrip"))
        assertTrue(root.resolve(PlayerCardIdentityStripPath.SHEET).readText().contains("PlayerCardIdentityStripChips"))
    }
}
