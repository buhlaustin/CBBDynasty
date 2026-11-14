package dev.novux.dynasty.cbb.core.pack

import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class PackManifestSignatureVerifierTest {
    private val unsignedPack = """
        {
          "packVersion": 1,
          "membershipYear": "2025-26",
          "disclaimerAcceptedRequired": false,
          "conferences": [
            {
              "id": "conf_a",
              "displayName": "Conference A",
              "leagueGameCount": 18
            }
          ],
          "teams": [
            {
              "id": "team_a",
              "conferenceId": "conf_a",
              "displayName": "Team A",
              "shortName": "TA",
              "homeCity": "Columbus",
              "homeState": "OH",
              "colors": { "primary": "#111111", "secondary": "#EEEEEE" },
              "arenaBaseName": "Arena"
            }
          ],
          "rulesetYear": 2025
        }
    """.trimIndent()

    @Test
    fun unsignedManifestAccepted() {
        val result = IdentityPackCodec.parseAndValidate(unsignedPack)
        assertTrue(result is PackLoadResult.Success)
    }

    @Test
    fun signedManifestAcceptedWhenHmacMatches() {
        val signerId = "novux-official-v1"
        val secret = PackConstants.TRUSTED_MANIFEST_SIGNERS.getValue(signerId)
        val withSigner = unsignedPack.trim().removeSuffix("}") +
            ",\"manifestSignerId\":\"$signerId\"}"
        val signature = PackManifestSignatureVerifier.signCanonicalManifest(withSigner, signerId, secret)
        val signed = withSigner.trim().removeSuffix("}") +
            ",\"manifestSignature\":\"$signature\"}"
        val result = IdentityPackCodec.parseAndValidate(signed)
        assertTrue("expected success got $result", result is PackLoadResult.Success)
    }

    @Test
    fun wrongSignatureRejected() {
        val fixed = unsignedPack.trim().removeSuffix("}") +
            ",\"manifestSignerId\":\"novux-official-v1\",\"manifestSignature\":\"deadbeef\"}"
        val result = IdentityPackCodec.parseAndValidate(fixed)
        assertTrue(result is PackLoadResult.Rejected)
        val err = (result as PackLoadResult.Rejected).error
        assertTrue(err is PackValidationError.InvalidManifestSignature)
    }

    @Test
    fun verifierAllowsOmittedSignatureField() {
        val pack = (IdentityPackCodec.parseAndValidate(unsignedPack) as PackLoadResult.Success).pack
        assertNull(PackManifestSignatureVerifier.validate(unsignedPack, pack))
    }
}
