package dev.novux.dynasty.cbb.core.qa

import java.nio.file.Paths
import kotlin.io.path.readText
import org.junit.Assert.assertTrue
import org.junit.Test

class StaffFourthSlotPathTest {
    @Test
    fun staffPanelDocumentsFourthSlotPolicy() {
        val root = generateSequence(Paths.get("").toAbsolutePath()) { it.parent }
            .first { it.resolve("android").resolve("settings.gradle.kts").toFile().exists() }
        val panel = root.resolve(
            "android/app/src/main/kotlin/dev/novux/dynasty/cbb/ui/coach/SchoolStaffTabPanel.kt",
        ).readText()
        assertTrue(panel.contains("StaffFourthAssistantSlotPolicy"))
        assertTrue(panel.contains("staff_fourth_slot_locked"))
    }
}