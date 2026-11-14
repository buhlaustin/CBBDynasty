package dev.novux.dynasty.cbb.core.qa

import java.nio.file.Paths
import kotlin.io.path.readText
import org.junit.Assert.assertTrue
import org.junit.Test

class PlayerNilDealChipPathTest {
    @Test
    fun nilDealRaisedChipOnOverview() {
        val root = generateSequence(Paths.get("").toAbsolutePath()) { it.parent }
            .first { it.resolve("android").resolve("settings.gradle.kts").toFile().exists() }
        assertTrue(root.resolve(PlayerNilDealChipPath.ENGINE).readText().contains(PlayerNilDealChipPath.MEGA_ID))
        assertTrue(root.resolve(PlayerNilDealChipPath.UI).readText().contains("player_nil_deal_raised_chip"))
        assertTrue(root.resolve(PlayerNilDealChipPath.IDENTITY).readText().contains("nilDealRaisedThisSeason"))
    }
}
