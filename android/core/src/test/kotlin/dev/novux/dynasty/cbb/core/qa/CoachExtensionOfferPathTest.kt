package dev.novux.dynasty.cbb.core.qa

import java.nio.file.Paths
import kotlin.io.path.readText
import org.junit.Assert.assertTrue
import org.junit.Test

class CoachExtensionOfferPathTest {
    @Test
    fun extensionOfferCardRepositoryAndEngineExist() {
        val root = generateSequence(Paths.get("").toAbsolutePath()) { it.parent }
            .first { it.resolve("android").resolve("settings.gradle.kts").toFile().exists() }
        assertTrue(root.resolve(CoachExtensionOfferPath.ENGINE).readText().contains(CoachExtensionOfferPath.MEGA_ID))
        assertTrue(root.resolve(CoachExtensionOfferPath.UI).readText().contains("CoachContractExtensionOfferCard"))
        assertTrue(root.resolve(CoachExtensionOfferPath.REPOSITORY).readText().contains("acceptExtension"))
    }
}
