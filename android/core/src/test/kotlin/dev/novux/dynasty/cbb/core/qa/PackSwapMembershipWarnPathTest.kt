package dev.novux.dynasty.cbb.core.qa

import dev.novux.dynasty.cbb.core.pack.PackSwapMembershipWarnPolicy
import java.nio.file.Paths
import kotlin.io.path.readText
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PackSwapMembershipWarnPathTest {
    @Test
    fun dynastyLoadUsesPackSwapWarnPolicy() {
        assertEquals("MEGA-GDD-094", PackSwapMembershipWarnPolicy.MEGA_GDD_ID)
        val app = repoRoot().resolve(
            "android/app/src/main/kotlin/dev/novux/dynasty/cbb/ui/DynastyApp.kt",
        ).readText()
        assertTrue(app.contains("PackSwapMembershipWarnPolicy"))
        assertTrue(app.contains("pack_import_membership_year_warning"))
    }

    private fun repoRoot() =
        generateSequence(Paths.get("").toAbsolutePath()) { it.parent }
            .first { it.resolve("android").resolve("settings.gradle.kts").toFile().exists() }
}