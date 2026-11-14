package dev.novux.dynasty.cbb.core.qa

import java.nio.file.Paths
import kotlin.io.path.readText
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SimWeekAdvancePreDialogPathTest {
    @Test
    fun saveSlotRepositoryUsesPreDialogFastPath() {
        assertEquals("MEGA-QA-008", SimWeekAdvancePreDialogPath.QA_ID)
        val root = generateSequence(Paths.get("").toAbsolutePath()) { it.parent }
            .first { it.resolve("android").resolve("settings.gradle.kts").toFile().exists() }
        val source = root.resolve(SimWeekAdvancePreDialogPath.REPOSITORY_SOURCE).readText()
        assertTrue(
            "Confirmation build must set ${SimWeekAdvancePreDialogPath.PRE_DIALOG_PARAM} = true",
            source.contains("${SimWeekAdvancePreDialogPath.PRE_DIALOG_PARAM} = true"),
        )
        assertTrue(
            "Pre-dialog path must use ${SimWeekAdvancePreDialogPath.LOAD_POOL_CALL}",
            source.contains("if (preDialogOnly)") &&
                source.contains(SimWeekAdvancePreDialogPath.LOAD_POOL_CALL),
        )
        assertTrue(
            "Fast-sim bundle must be deferred until confirmed advance",
            source.contains("!preDialogOnly && unplayed") &&
                source.contains(SimWeekAdvancePreDialogPath.FAST_SIM_FACTORY_CALL),
        )
        assertTrue(
            "Full advance still seeds pool when estimating recruiting",
            source.contains(SimWeekAdvancePreDialogPath.ENSURE_POOL_CALL),
        )
    }
}
