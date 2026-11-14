package dev.novux.dynasty.cbb.core.qa

import dev.novux.dynasty.cbb.core.signingday.NationalRecruitingInSeasonPoolPolicy
import java.nio.file.Paths
import kotlin.io.path.readText
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class NationalRecruitingInSeasonPoolPathTest {
    @Test
    fun hubUsesSeededPoolTop60() {
        assertEquals("MEGA-GDD-116", NationalRecruitingInSeasonPoolPolicy.MEGA_ID)
        val root = repoRoot()
        assertTrue(
            root.resolve(
                "android/app/src/main/kotlin/dev/novux/dynasty/cbb/ui/recruiting/RecruitingHubScreen.kt",
            ).readText().contains("top60FromPool"),
        )
        assertTrue(
            root.resolve(
                "android/app/src/main/kotlin/dev/novux/dynasty/cbb/ui/recruiting/RecruitingHubTabBodies.kt",
            ).readText().contains("recruiting_hs_top60_pool_hint"),
        )
    }

    private fun repoRoot() =
        generateSequence(Paths.get("").toAbsolutePath()) { it.parent }
            .first { it.resolve("android").resolve("settings.gradle.kts").toFile().exists() }
}