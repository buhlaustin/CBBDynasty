package dev.novux.dynasty.cbb.core.qa

import java.nio.file.Paths
import kotlin.io.path.readText
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RecruitScoutLetterCategoriesPathTest {
    @Test
    fun hubRendersScoutCategorySheet() {
        assertEquals("MEGA-RECR-013", RecruitScoutLetterCategoriesPath.RECR_ID)
        val root = generateSequence(Paths.get("").toAbsolutePath()) { it.parent }
            .first { it.resolve("android").resolve("settings.gradle.kts").toFile().exists() }
        val hub = root.resolve(RecruitScoutLetterCategoriesPath.HUB_SCREEN).readText()
        assertTrue(hub.contains(RecruitScoutLetterCategoriesPath.SHEET_COMPOSABLE))
        assertTrue(hub.contains("RecruitScoutLetterEngine"))
        val engine = root.resolve(RecruitScoutLetterCategoriesPath.ENGINE_SOURCE).readText()
        assertTrue(engine.contains("RecruitScoutLetters"))
    }
}
