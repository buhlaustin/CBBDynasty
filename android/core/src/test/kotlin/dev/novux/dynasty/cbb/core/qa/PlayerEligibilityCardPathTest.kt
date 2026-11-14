package dev.novux.dynasty.cbb.core.qa

import dev.novux.dynasty.cbb.core.player.card.PlayerEligibilityCardPolicy
import java.nio.file.Paths
import kotlin.io.path.readText
import org.junit.Assert.assertTrue
import org.junit.Test

class PlayerEligibilityCardPathTest {
    @Test
    fun playerCardOverviewWiresEligibilityCopy() {
        val root = generateSequence(Paths.get("").toAbsolutePath()) { it.parent }
            .first { it.resolve("android").resolve("settings.gradle.kts").toFile().exists() }
        val policy = root.resolve(
            "android/core/src/main/kotlin/dev/novux/dynasty/cbb/core/player/card/PlayerEligibilityCardPolicy.kt",
        ).readText()
        assertTrue(policy.contains(PlayerEligibilityCardPolicy.MEGA_ID))
        val sheet = root.resolve(
            "android/app/src/main/kotlin/dev/novux/dynasty/cbb/ui/player/PlayerCardSheet.kt",
        ).readText()
        assertTrue(sheet.contains("PlayerEligibilityStatusLines"))
        assertTrue(sheet.contains("PlayerRosterStatusChips"))
    }
}