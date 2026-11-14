package dev.novux.dynasty.cbb.core.qa

import java.nio.file.Paths
import kotlin.io.path.readText
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SigningDayDashboardFirstPaintPathTest {
    @Test
    fun offseasonScreenShowsSkeletonWhileProgressNull() {
        assertEquals("MEGA-QA-094", SigningDayDashboardFirstPaint.QA_ID)
        val root = generateSequence(Paths.get("").toAbsolutePath()) { it.parent }
            .first { it.resolve("android").resolve("settings.gradle.kts").toFile().exists() }
        val source = root.resolve("android/app/src/main/kotlin/dev/novux/dynasty/cbb/ui/offseason/OffseasonScreen.kt")
            .readText()
        assertTrue(source.contains("SigningDayRequiredTasksSkeleton"))
        assertTrue(
            "Week 6 panel must branch on null signingProgress",
            source.contains("when (val progress = signingProgress)") ||
                source.contains("signingProgress") && source.contains("null -> SigningDayRequiredTasksSkeleton"),
        )
    }
}
