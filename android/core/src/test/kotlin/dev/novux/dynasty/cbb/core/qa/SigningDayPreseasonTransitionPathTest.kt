package dev.novux.dynasty.cbb.core.qa

import java.nio.file.Paths
import kotlin.io.path.readText
import org.junit.Assert.assertTrue
import org.junit.Test

class SigningDayPreseasonTransitionPathTest {
    @Test
    fun transitionWiredInSaveAndEconomy() {
        val root = generateSequence(Paths.get("").toAbsolutePath()) { it.parent }
            .first { it.resolve("android").resolve("settings.gradle.kts").toFile().exists() }
        assertTrue(root.resolve(SigningDayPreseasonTransitionPath.TRANSITION).readText().contains(SigningDayPreseasonTransitionPath.MEGA_ID))
        assertTrue(root.resolve(SigningDayPreseasonTransitionPath.SAVE_WIRE).readText().contains("SigningDayPreseasonRecruitingTransition"))
        assertTrue(root.resolve(SigningDayPreseasonTransitionPath.ECONOMY_WIRE).readText().contains("ledgerForPreseasonEntry"))
    }
}
