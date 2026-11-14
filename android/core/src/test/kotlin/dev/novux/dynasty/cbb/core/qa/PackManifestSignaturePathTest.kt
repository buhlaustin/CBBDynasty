package dev.novux.dynasty.cbb.core.qa

import dev.novux.dynasty.cbb.core.pack.PackConstants
import dev.novux.dynasty.cbb.core.pack.PackManifestSignaturePolicy
import java.nio.file.Paths
import kotlin.io.path.readText
import org.junit.Assert.assertTrue
import org.junit.Test

class PackManifestSignaturePathTest {
    @Test
    fun optionalManifestSignatureVerifierWiredInCodec() {
        val root = generateSequence(Paths.get("").toAbsolutePath()) { it.parent }
            .first { it.resolve("android").resolve("settings.gradle.kts").toFile().exists() }
        val constants = root.resolve(PackManifestSignaturePath.CONSTANTS).readText()
        assertTrue(constants.contains(PackManifestSignaturePath.MEGA_ID))
        assertTrue(constants.contains(PackConstants.MANIFEST_SIGNATURE_FIELD))
        assertTrue(constants.contains("TRUSTED_MANIFEST_SIGNERS"))
        val verifier = root.resolve(PackManifestSignaturePath.VERIFIER).readText()
        assertTrue(verifier.contains(PackManifestSignaturePolicy.VERIFIER))
        val codec = root.resolve(PackManifestSignaturePath.CODEC).readText()
        assertTrue(codec.contains("PackManifestSignatureVerifier.validate"))
    }
}
