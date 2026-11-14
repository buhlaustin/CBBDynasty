package dev.novux.dynasty.cbb.core.qa

import java.nio.file.Paths
import kotlin.io.path.readText
import org.junit.Assert.assertTrue
import org.junit.Test

class StaffManagePanelPathTest {
    @Test
    fun staffPanelExposesManageHireFirePoach() {
        val root = generateSequence(Paths.get("").toAbsolutePath()) { it.parent }
            .first { it.resolve("android").resolve("settings.gradle.kts").toFile().exists() }
        val panel = root.resolve(StaffManagePanelPath.PANEL_SOURCE).readText()
        val sheet = root.resolve(StaffManagePanelPath.SHEET_SOURCE).readText()
        val repo = root.resolve(StaffManagePanelPath.REPOSITORY_SOURCE).readText()
        assertTrue(panel.contains("userCanManage"))
        assertTrue(panel.contains("onManageStaff"))
        assertTrue(sheet.contains("staff_manage_poach"))
        assertTrue(sheet.contains("staff_manage_fire"))
        assertTrue(repo.contains("poachAssistant"))
        assertTrue(repo.contains("fireStaff"))
        assertTrue(repo.contains("hireFromPool"))
    }
}
