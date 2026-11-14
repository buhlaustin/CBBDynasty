package dev.novux.dynasty.cbb.core.qa

import dev.novux.dynasty.cbb.core.player.loyalty.PlayerPromiseAmendPolicy
import dev.novux.dynasty.cbb.core.player.loyalty.PlayerPromiseLockPolicy
import java.nio.file.Paths
import kotlin.io.path.readText
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PlayerPromiseLockPathTest {
    @Test
    fun promiseLocksAtCommitWithPickerNoAmend() {
        val root = generateSequence(Paths.get("").toAbsolutePath()) { it.parent }
            .first { it.resolve("android").resolve("settings.gradle.kts").toFile().exists() }
        val engine = root.resolve(PlayerPromiseLockPath.ENGINE).readText()
        assertTrue(engine.contains(PlayerPromiseLockPath.MEGA_ID))
        assertFalse(PlayerPromiseAmendPolicy.ALLOW_AMEND_UI)
        assertTrue(root.resolve(PlayerPromiseLockPath.LOCKED_REPO).readText().contains("saveAll"))
        val signing = root.resolve(PlayerPromiseLockPath.SIGNING_DAY).readText()
        assertTrue(signing.contains(PlayerPromiseLockPolicy.PICKER))
        assertTrue(signing.contains(PlayerPromiseLockPath.MEGA_ID))
        assertTrue(signing.contains("signing_day_promise_locked_note"))
        assertTrue(root.resolve(PlayerPromiseLockPath.SIGNING_REPO).readText().contains("recruitLockedPromiseRepository"))
    }
}
