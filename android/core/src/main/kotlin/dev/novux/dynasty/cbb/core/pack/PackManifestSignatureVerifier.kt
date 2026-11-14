package dev.novux.dynasty.cbb.core.pack

import java.util.Locale
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject

/**
 * Optional HMAC-SHA256 manifest signature for HTTPS pack imports ([MEGA-GDD-018]).
 */
object PackManifestSignatureVerifier {
    fun validate(rawJson: String, pack: IdentityPack): PackValidationError? {
        val signature = pack.manifestSignature?.trim().orEmpty()
        if (signature.isEmpty()) return null
        val signerId = pack.manifestSignerId?.trim().orEmpty()
        if (signerId.isEmpty()) {
            return PackValidationError.InvalidManifestSignature("manifestSignerId required when manifestSignature is set")
        }
        val secret = PackConstants.TRUSTED_MANIFEST_SIGNERS[signerId]
            ?: return PackValidationError.InvalidManifestSignature("unknown manifestSignerId: $signerId")
        val expected = hmacSha256Hex(canonicalManifestBytes(rawJson), secret)
        if (!constantTimeEquals(expected.lowercase(Locale.US), signature.lowercase(Locale.US))) {
            return PackValidationError.InvalidManifestSignature("manifest signature mismatch")
        }
        return null
    }

    /** Test / publisher helper — not used in production import path. */
    fun signCanonicalManifest(rawJson: String, signerId: String, secret: String): String {
        require(PackConstants.TRUSTED_MANIFEST_SIGNERS[signerId] == secret) {
            "signerId must match PackConstants.TRUSTED_MANIFEST_SIGNERS"
        }
        return hmacSha256Hex(canonicalManifestBytes(rawJson), secret)
    }

    fun canonicalManifestBytes(rawJson: String): ByteArray {
        val root = json.parseToJsonElement(rawJson).jsonObject
        val stripped = JsonObject(root.filterKeys { it != PackConstants.MANIFEST_SIGNATURE_FIELD })
        return json.encodeToString(JsonObject.serializer(), stripped).toByteArray(Charsets.UTF_8)
    }

    private val json = Json { encodeDefaults = true }

    private fun hmacSha256Hex(payload: ByteArray, secret: String): String {
        val mac = Mac.getInstance("HmacSHA256")
        mac.init(SecretKeySpec(secret.toByteArray(Charsets.UTF_8), "HmacSHA256"))
        return mac.doFinal(payload).joinToString("") { byte -> "%02x".format(byte) }
    }

    private fun constantTimeEquals(a: String, b: String): Boolean {
        if (a.length != b.length) return false
        var diff = 0
        for (i in a.indices) {
            diff = diff or (a[i].code xor b[i].code)
        }
        return diff == 0
    }
}
