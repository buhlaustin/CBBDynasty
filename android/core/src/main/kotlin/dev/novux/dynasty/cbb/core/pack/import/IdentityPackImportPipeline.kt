package dev.novux.dynasty.cbb.core.pack.import

import dev.novux.dynasty.cbb.core.pack.IdentityPack
import dev.novux.dynasty.cbb.core.pack.IdentityPackCodec
import dev.novux.dynasty.cbb.core.pack.PackLoadResult

/**
 * JSON / TXT / URL body import with disclaimer attestation — wizard UI wires this in Phase 4.
 */
object IdentityPackImportPipeline {
    fun importLocal(
        raw: String,
        format: PackImportFormat,
        attestation: PackImportAttestation,
        overlayBase: IdentityPack? = null,
    ): PackImportResult {
        val trimmed = raw.trim()
        if (trimmed.isEmpty()) {
            return PackImportResult.Rejected(PackImportError.InvalidTxt("Empty payload"))
        }
        PackPayloadPolicy.validateUtf8Payload(trimmed)?.let {
            return PackImportResult.Rejected(it)
        }
        val resolvedFormat = when (format) {
            PackImportFormat.Auto -> if (trimmed.startsWith("{")) PackImportFormat.Json else PackImportFormat.Txt
            else -> format
        }

        val draftPack = when (resolvedFormat) {
            PackImportFormat.Json -> {
                when (val decoded = IdentityPackCodec.parseAndValidate(trimmed)) {
                    is PackLoadResult.Rejected ->
                        return PackImportResult.Rejected(PackImportError.Validation(decoded.error))
                    is PackLoadResult.Success -> decoded.pack
                }
            }
            PackImportFormat.Txt -> {
                val parsed = try {
                    IdentityPackTxtCodec.parseOrThrow(trimmed)
                } catch (e: IllegalArgumentException) {
                    return PackImportResult.Rejected(PackImportError.InvalidTxt(e.message ?: "TXT parse failed"))
                }
                val (overlay, mode) = parsed
                val rawPack = when (mode) {
                    IdentityPackTxtCodec.ImportMode.Full -> overlay
                    IdentityPackTxtCodec.ImportMode.Overlay -> {
                        val base = overlayBase
                            ?: return PackImportResult.Rejected(PackImportError.OverlayBaseRequired())
                        try {
                            IdentityPackOverlayMerger.merge(base, overlay)
                        } catch (e: IllegalArgumentException) {
                            return PackImportResult.Rejected(
                                PackImportError.InvalidTxt(e.message ?: "overlay merge failed"),
                            )
                        }
                    }
                }
                when (val validated = IdentityPackCodec.parseAndValidate(IdentityPackCodec.encode(rawPack))) {
                    is PackLoadResult.Rejected ->
                        return PackImportResult.Rejected(PackImportError.Validation(validated.error))
                    is PackLoadResult.Success -> validated.pack
                }
            }
            PackImportFormat.Auto -> error("resolved above")
        }

        return finalizeAttestation(draftPack, attestation)
    }

    fun importFromUrl(
        url: String,
        attestation: PackImportAttestation,
        body: String,
        overlayBase: IdentityPack? = null,
    ): PackImportResult {
        PackUrlPolicy.validate(url)?.let { return PackImportResult.Rejected(it) }
        return importLocal(body, PackImportFormat.Auto, attestation, overlayBase)
    }

    private fun finalizeAttestation(
        pack: IdentityPack,
        attestation: PackImportAttestation,
    ): PackImportResult {
        if (PackDisclaimerAttestationPolicy.importBlockedWithoutAttestation(pack.disclaimerAcceptedRequired, attestation)) {
            return PackImportResult.Rejected(PackImportError.AttestationRequired())
        }
        return PackImportResult.Success(pack)
    }
}
