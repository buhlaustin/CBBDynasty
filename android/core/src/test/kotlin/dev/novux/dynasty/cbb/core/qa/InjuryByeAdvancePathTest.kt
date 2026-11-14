package dev.novux.dynasty.cbb.core.qa

import dev.novux.dynasty.cbb.core.advance.InjuryByeAdvanceConfirmPolicy
import java.nio.file.Paths
import kotlin.io.path.readText
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** [MEGA-FRAME-080] */
class InjuryByeAdvancePathTest {
    @Test
    fun megaFrame080_wiresPolicyResolverRepositoryAndDialog() {
        val root = repoRoot()
        val policy = root.resolve(
            "android/core/src/main/kotlin/dev/novux/dynasty/cbb/core/advance/InjuryByeAdvanceConfirmPolicy.kt",
        ).readText()
        val resolver = root.resolve(
            "android/core/src/main/kotlin/dev/novux/dynasty/cbb/core/advance/SimWeekAdvanceResolver.kt",
        ).readText()
        val repository = root.resolve(
            "android/app/src/main/kotlin/dev/novux/dynasty/cbb/data/save/SaveSlotRepository.kt",
        ).readText()
        val dialog = root.resolve(
            "android/app/src/main/kotlin/dev/novux/dynasty/cbb/ui/advance/SimWeekAdvanceDialog.kt",
        ).readText()
        assertEquals("MEGA-FRAME-080", InjuryByeAdvanceConfirmPolicy.MEGA_FRAME_ID)
        assertTrue(policy.contains("MEGA-FRAME-080"))
        assertTrue(resolver.contains("InjuryByeAdvanceConfirmPolicy"))
        assertTrue(repository.contains("userOnByeWeek"))
        assertTrue(dialog.contains("InjuryWeeksTickByeWeek"))
    }

    private fun repoRoot() =
        generateSequence(Paths.get("").toAbsolutePath()) { it.parent }
            .first { it.resolve("android").resolve("settings.gradle.kts").toFile().exists() }
}
