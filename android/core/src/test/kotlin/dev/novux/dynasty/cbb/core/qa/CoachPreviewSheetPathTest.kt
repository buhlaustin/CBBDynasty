package dev.novux.dynasty.cbb.core.qa

import java.nio.file.Paths
import kotlin.io.path.readText
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CoachPreviewSheetPathTest {
    @Test
    fun shellOpensPreviewBeforeFullHeadCoachCard() {
        val root = repoRoot()
        val shell = root.resolve(CoachPreviewSheetPath.SHELL_SOURCE).readText()
        assertTrue(shell.contains("CoachPreviewBottomSheet"))
        assertTrue(shell.contains(CoachPreviewSheetPath.PREVIEW_ENTRY_SYMBOL))
        assertTrue(shell.contains(CoachPreviewSheetPath.FULL_CARD_SYMBOL))
        assertTrue(shell.contains("onViewMore"))
    }

    @Test
    fun previewSheetIsTextOnlyQuickInfo() {
        val source = repoRoot().resolve(CoachPreviewSheetPath.SHEET_SOURCE).readText()
        assertTrue(source.contains("object CoachPreviewSheet"))
        assertTrue(source.contains(CoachPreviewSheetPath.QA_ID))
        assertFalse(source.contains(CoachPreviewSheetPath.FORBIDDEN_IN_PREVIEW))
        assertTrue(source.contains("CoachPreviewBottomSheet"))
    }

    private fun repoRoot() =
        generateSequence(Paths.get("").toAbsolutePath()) { it.parent }
            .first { it.resolve("android").resolve("settings.gradle.kts").toFile().exists() }
}
