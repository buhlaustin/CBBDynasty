package dev.novux.dynasty.cbb.core.qa

import java.nio.file.Paths
import kotlin.io.path.readText
import org.junit.Assert.assertTrue
import org.junit.Test

class OffseasonRecruitingHubClosedPathTest {
    @Test
    fun closedCycleShowsAdministrationCopyNotHubButton() {
        val root = generateSequence(Paths.get("").toAbsolutePath()) { it.parent }
            .first { it.resolve("android").resolve("settings.gradle.kts").toFile().exists() }
        val screen = root.resolve(
            "android/app/src/main/kotlin/dev/novux/dynasty/cbb/ui/offseason/OffseasonScreen.kt",
        ).readText()
        assertTrue(screen.contains("RecruitingCyclePolicy.hubNavigable"))
        assertTrue(screen.contains("recruiting_administration_only_title"))
        assertTrue(screen.contains("recruiting_cycle_closed_body"))
    }
}