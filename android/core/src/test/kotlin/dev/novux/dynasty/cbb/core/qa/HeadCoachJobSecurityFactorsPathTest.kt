package dev.novux.dynasty.cbb.core.qa

import java.nio.file.Paths
import kotlin.io.path.readText
import org.junit.Assert.assertTrue
import org.junit.Test

class HeadCoachJobSecurityFactorsPathTest {
    @Test
    fun userHcOverviewShowsSecurityFactors() {
        val root = generateSequence(Paths.get("").toAbsolutePath()) { it.parent }
            .first { it.resolve("android").resolve("settings.gradle.kts").toFile().exists() }
        val screen = root.resolve(
            "android/app/src/main/kotlin/dev/novux/dynasty/cbb/ui/coach/HeadCoachCardScreen.kt",
        ).readText()
        assertTrue(screen.contains("HeadCoachCarouselContractPolicy"))
        assertFalseOverviewHidesFactors(screen)
    }

    private fun assertFalseOverviewHidesFactors(screen: String) {
        assertTrue(
            screen.contains("showJobSecurityFactorDetailsForUserCoach") ||
                !screen.contains("showFactorDetails = false"),
        )
    }
}