package dev.novux.dynasty.cbb.core.qa

import java.nio.file.Paths
import kotlin.io.path.readText
import org.junit.Assert.assertTrue
import org.junit.Test

class CoachOverviewCareerRecordPathTest {
    @Test
    fun careerRecordHeroOnOverviewAchievements() {
        val root = generateSequence(Paths.get("").toAbsolutePath()) { it.parent }
            .first { it.resolve("android").resolve("settings.gradle.kts").toFile().exists() }
        val ui = root.resolve(CoachOverviewCareerRecordPath.UI).readText()
        assertTrue(ui.contains(CoachOverviewCareerRecordPath.HERO))
        assertTrue(ui.contains("coach_overview_career_record_value"))
        assertTrue(
            root.resolve(CoachOverviewCareerRecordPath.MODEL).readText().contains("fromSeasonRecordLabel"),
        )
        assertTrue(
            root.resolve("android/app/src/main/kotlin/dev/novux/dynasty/cbb/ui/coach/HeadCoachCardScreen.kt")
                .readText()
                .contains("fromSeasonRecordLabel"),
        )
    }
}
