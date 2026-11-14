package dev.novux.dynasty.cbb.core.qa

import dev.novux.dynasty.cbb.core.save.DynastySaveExportPolicy
import java.nio.file.Paths
import kotlin.io.path.readText
import org.junit.Assert.assertTrue
import org.junit.Test

class DynastySaveExportPathTest {
    @Test
    fun loadScreenExportsArchiveManifest() {
        val root = generateSequence(Paths.get("").toAbsolutePath()) { it.parent }
            .first { it.resolve("android").resolve("settings.gradle.kts").toFile().exists() }
        assertTrue(root.resolve(DynastySaveExportPath.ENGINE).readText().contains(DynastySaveExportPath.MEGA_ID))
        assertTrue(root.resolve(DynastySaveExportPath.REPO).readText().contains("exportDynastyArchive"))
        val loadUi = root.resolve(DynastySaveExportPath.UI).readText()
        assertTrue(loadUi.contains(DynastySaveExportPolicy.MEGA_ID))
        assertTrue(loadUi.contains("load_slot_export_chip"))
        assertTrue(loadUi.contains("DynastyExportShare"))
    }
}
