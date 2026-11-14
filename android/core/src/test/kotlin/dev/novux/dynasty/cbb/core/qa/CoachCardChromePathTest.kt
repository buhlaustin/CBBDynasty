package dev.novux.dynasty.cbb.core.qa

import dev.novux.dynasty.cbb.core.coach.card.CoachCardIdentityStripPolicy
import dev.novux.dynasty.cbb.core.coach.card.CoachCardPersistentHeaderPolicy
import java.nio.file.Paths
import kotlin.io.path.readText
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CoachCardChromePathTest {
    @Test
    fun headCoachFullCardUsesPersistentHeaderAndTextIdentityStrip() {
        val root = repoRoot()
        assertTrue(root.resolve(CoachCardChromePath.PERSISTENT_HEADER).readText().contains("CoachCardPersistentHeader"))
        assertTrue(root.resolve(CoachCardChromePath.IDENTITY_STRIP).readText().contains(CoachCardIdentityStripPolicy.MEGA_ID))
        assertFalse(root.resolve(CoachCardChromePath.IDENTITY_STRIP).readText().contains("CoachPortraitSlot"))
        val head = root.resolve(CoachCardChromePath.HEAD_SCREEN).readText()
        assertTrue(head.contains("CoachCardPersistentHeader"))
        assertTrue(head.contains("CoachCardIdentityStrip"))
        assertTrue(head.contains("CoachCardTabRow"))
        val shell = root.resolve(CoachCardChromePath.SHELL).readText()
        assertTrue(shell.contains("CoachCardUserGate"))
        assertTrue(shell.contains("onBack"))
    }

    private fun repoRoot() =
        generateSequence(Paths.get("").toAbsolutePath()) { it.parent }
            .first { it.resolve("android").resolve("settings.gradle.kts").toFile().exists() }
}
