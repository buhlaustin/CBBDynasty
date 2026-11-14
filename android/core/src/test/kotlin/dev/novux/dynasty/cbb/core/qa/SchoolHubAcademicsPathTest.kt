package dev.novux.dynasty.cbb.core.qa

import java.nio.file.Paths
import kotlin.io.path.readText
import org.junit.Assert.assertTrue
import org.junit.Test

class SchoolHubAcademicsPathTest {
    @Test
    fun schoolHubShowsAcademicsGrade() {
        val root = generateSequence(Paths.get("").toAbsolutePath()) { it.parent }
            .first { it.resolve("android").resolve("settings.gradle.kts").toFile().exists() }
        val card = root.resolve(SchoolHubAcademicsPath.OVERVIEW_CARD).readText()
        assertTrue(card.contains("SchoolHubAcademicsPath"))
        assertTrue(card.contains("school_hub_academics_grade"))
        assertTrue(root.resolve(SchoolHubAcademicsPath.REPOSITORY).readText().contains("ProgramAcademicsGradeEngine"))
    }
}