package dev.novux.dynasty.cbb.core.pack

import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json

object IdentityPackCodec {
    private val json = Json {
        ignoreUnknownKeys = false
        isLenient = false
        explicitNulls = false
    }

    fun parseAndValidate(raw: String): PackLoadResult {
        val decoded = try {
            json.decodeFromString<IdentityPack>(raw)
        } catch (e: SerializationException) {
            return PackLoadResult.Rejected(PackValidationError.MalformedJson(e.message ?: "decode failed"))
        } catch (e: IllegalArgumentException) {
            return PackLoadResult.Rejected(PackValidationError.MalformedJson(e.message ?: "decode failed"))
        }

        val error = IdentityPackValidator.validate(decoded)
            ?: PackManifestSignatureVerifier.validate(raw, decoded)
        return if (error != null) {
            PackLoadResult.Rejected(error)
        } else {
            PackLoadResult.Success(decoded)
        }
    }

    fun encode(pack: IdentityPack): String = json.encodeToString(IdentityPack.serializer(), pack)
}
