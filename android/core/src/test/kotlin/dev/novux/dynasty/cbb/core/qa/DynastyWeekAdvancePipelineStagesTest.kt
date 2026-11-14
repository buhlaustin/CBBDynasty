package dev.novux.dynasty.cbb.core.qa

import java.nio.file.Paths
import kotlin.io.path.readText
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DynastyWeekAdvancePipelineStagesTest {
    @Test
    fun advanceDynastyWeekRunsDocumentedStagesInOrder() {
        assertEquals("MEGA-FRAME-114", DynastyWeekAdvancePipelineStages.FRAME_ID)
        val root = generateSequence(Paths.get("").toAbsolutePath()) { it.parent }
            .first { it.resolve("android").resolve("settings.gradle.kts").toFile().exists() }
        val source = root.resolve(DynastyWeekAdvancePipelineStages.REPOSITORY_SOURCE).readText()
        val fnStart = source.indexOf("suspend fun advanceDynastyWeek")
        assertTrue(fnStart >= 0)
        val fnBody = source.substring(fnStart)
        var searchFrom = 0
        var lastIndex = -1
        DynastyWeekAdvancePipelineStages.confirmedAdvanceStageMarkers.forEach { marker ->
            val idx = fnBody.indexOf(marker, searchFrom)
            assertTrue("Missing advance stage marker: $marker", idx >= 0)
            assertTrue("Stage $marker appears before prior stage", idx > lastIndex)
            lastIndex = idx
            searchFrom = idx + marker.length
        }
    }
}