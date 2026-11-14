package dev.novux.dynasty.cbb.core.qa

import java.nio.file.Paths
import kotlin.io.path.readText
import org.junit.Assert.assertTrue
import org.junit.Test

class NilAssignCapLinePathTest {
    @Test
    fun hubShowsNilCapLine() {
        val root = generateSequence(Paths.get("").toAbsolutePath()) { it.parent }
            .first { it.resolve("android").resolve("settings.gradle.kts").toFile().exists() }
        assertTrue(root.resolve(NilAssignCapLinePath.POLICY).readText().contains(NilAssignCapLinePath.MEGA_ID))
        assertTrue(root.resolve(NilAssignCapLinePath.CAP_LINE).readText().contains("RecruitNilAssignCapLine"))
        assertTrue(root.resolve(NilAssignCapLinePath.HUB).readText().contains("RecruitNilAssignCapLine"))
    }
}