package dev.novux.dynasty.cbb.core.qa

import dev.novux.dynasty.cbb.core.signingday.SigningDayClassBoardSummaryPolicy
import java.nio.file.Paths
import kotlin.io.path.readText
import org.junit.Assert.assertTrue
import org.junit.Test

class SigningDayBoardSummaryPathTest {
    @Test
    fun signingDayScreenShowsBoardSummary() {
        val root = generateSequence(Paths.get("").toAbsolutePath()) { it.parent }
            .first { it.resolve("android").resolve("settings.gradle.kts").toFile().exists() }
        val policy = root.resolve(
            "android/core/src/main/kotlin/dev/novux/dynasty/cbb/core/signingday/SigningDayClassBoardSummaryPolicy.kt",
        ).readText()
        assertTrue(policy.contains(SigningDayClassBoardSummaryPolicy.MEGA_ID))
        val screen = root.resolve(
            "android/app/src/main/kotlin/dev/novux/dynasty/cbb/ui/signingday/SigningDayScreen.kt",
        ).readText()
        assertTrue(screen.contains("signing_day_board_summary"))
    }
}