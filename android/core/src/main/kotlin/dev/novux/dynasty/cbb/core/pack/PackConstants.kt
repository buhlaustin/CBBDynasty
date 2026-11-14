package dev.novux.dynasty.cbb.core.pack

/** Identity pack manifest version — [planning/game-design.md#identity-packs--save-model]. */
object PackConstants {
    const val SUPPORTED_PACK_VERSION = 1

    /** Optional manifest signature ([MEGA-GDD-018]). */
    const val MANIFEST_SIGNATURE_FIELD: String = "manifestSignature"
    const val MANIFEST_SIGNER_FIELD: String = "manifestSignerId"
    const val MANIFEST_SIGNATURE_ALGORITHM: String = "HmacSHA256"

    /**
     * Trusted signer id → HMAC secret for dev/official URL packs.
     * Unsigned manifests remain valid (signature field omitted).
     */
    val TRUSTED_MANIFEST_SIGNERS: Map<String, String> = mapOf(
        "novux-official-v1" to "novux-pack-signing-v1-dev",
    )
}
