package dev.novux.dynasty.cbb.core.qa

import dev.novux.dynasty.cbb.core.recruiting.RecruitOfferPromisePolicy
import java.nio.file.Paths
import kotlin.io.path.readText
import org.junit.Assert.assertTrue
import org.junit.Test

class RecruitPromisePickerPathTest {
    @Test
    fun scholarshipOfferOpensPromisePickerOnRecruitHub() {
        val root = generateSequence(Paths.get("").toAbsolutePath()) { it.parent }
            .first { it.resolve("android").resolve("settings.gradle.kts").toFile().exists() }
        val hub = root.resolve("android/app/src/main/kotlin/dev/novux/dynasty/cbb/ui/recruiting/RecruitingHubScreen.kt")
            .readText()
        val picker = root.resolve("android/app/src/main/kotlin/dev/novux/dynasty/cbb/ui/recruiting/RecruitPromisePickerSheet.kt")
            .readText()
        val repo = root.resolve("android/app/src/main/kotlin/dev/novux/dynasty/cbb/data/recruiting/RecruitOfferPromiseRepository.kt")
            .readText()
        assertTrue(hub.contains(RecruitOfferPromisePolicy.PICKER_COMPOSABLE))
        assertTrue(hub.contains("onRequestScholarshipOffer"))
        assertTrue(hub.contains("RecruitOfferPromiseRepository"))
        assertTrue(picker.contains("RecruitOfferPromisePolicy.OFFERABLE_TYPES"))
        assertTrue(repo.contains("encode"))
    }
}
