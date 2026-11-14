package dev.novux.dynasty.cbb.core.qa

import dev.novux.dynasty.cbb.core.recruiting.ai.AiRecruitingLedgerPolicy
import java.nio.file.Paths
import kotlin.io.path.readText
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AiRecruitingLedgerPathTest {
    @Test
    fun policyIdAndAutomationHint() {
        assertEquals("MEGA-GDD-068", AiRecruitingLedgerPolicy.MEGA_ID)
        val root = generateSequence(Paths.get("").toAbsolutePath()) { it.parent }
            .first { it.resolve("android").resolve("settings.gradle.kts").toFile().exists() }
        assertTrue(
            root.resolve(
                "android/app/src/main/kotlin/dev/novux/dynasty/cbb/ui/shell/DynastyAutomationScreen.kt",
            ).readText().contains("dynasty_automation_recruiting_ledger_hint"),
        )
    }
}