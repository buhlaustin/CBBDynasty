package dev.novux.dynasty.cbb.core.qa

import dev.novux.dynasty.cbb.core.nil.NilLedgerBreakdownPolicy
import java.nio.file.Paths
import kotlin.io.path.readText
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class NilLedgerBreakdownPathTest {
    @Test
    fun hubShowsCommittedReservedRemaining() {
        assertEquals("MEGA-GDD-113", NilLedgerBreakdownPolicy.MEGA_ID)
        val hub = repoRoot().resolve(
            "android/app/src/main/kotlin/dev/novux/dynasty/cbb/ui/recruiting/RecruitingHubScreen.kt",
        ).readText()
        assertTrue(hub.contains("recruiting_nil_breakdown_line"))
        assertTrue(hub.contains("nilCommittedUsd"))
        assertTrue(hub.contains("nilReservedUsd"))
    }

    private fun repoRoot() =
        generateSequence(Paths.get("").toAbsolutePath()) { it.parent }
            .first { it.resolve("android").resolve("settings.gradle.kts").toFile().exists() }
}