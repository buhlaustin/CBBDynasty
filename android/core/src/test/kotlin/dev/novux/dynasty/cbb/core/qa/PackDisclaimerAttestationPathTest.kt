package dev.novux.dynasty.cbb.core.qa

import dev.novux.dynasty.cbb.core.pack.import.PackDisclaimerAttestationPolicy
import dev.novux.dynasty.cbb.core.pack.import.PackImportAttestation
import java.nio.file.Paths
import kotlin.io.path.readText
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PackDisclaimerAttestationPathTest {
    @Test
    fun pipelineAndImportUiEnforceAttestation() {
        assertEquals("MEGA-GDD-097", PackDisclaimerAttestationPolicy.MEGA_ID)
        assertTrue(
            PackDisclaimerAttestationPolicy.importBlockedWithoutAttestation(
                disclaimerAcceptedRequired = true,
                attestation = PackImportAttestation(userAcceptedDisclaimer = false),
            ),
        )
        val root = repoRoot()
        assertTrue(
            root.resolve(
                "android/core/src/main/kotlin/dev/novux/dynasty/cbb/core/pack/import/IdentityPackImportPipeline.kt",
            ).readText().contains("PackDisclaimerAttestationPolicy"),
        )
        assertTrue(
            root.resolve(
                "android/app/src/main/kotlin/dev/novux/dynasty/cbb/ui/pack/IdentityPackImportScreen.kt",
            ).readText().contains("pack_import_disclaimer_required_hint"),
        )
    }

    private fun repoRoot() =
        generateSequence(Paths.get("").toAbsolutePath()) { it.parent }
            .first { it.resolve("android").resolve("settings.gradle.kts").toFile().exists() }
}