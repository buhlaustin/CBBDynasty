package dev.novux.dynasty.cbb.data.pack

import android.content.Context
import dev.novux.dynasty.cbb.core.pack.IdentityPack
import dev.novux.dynasty.cbb.core.pack.IdentityPackCodec
import dev.novux.dynasty.cbb.core.pack.PackLoadResult
import dev.novux.dynasty.cbb.core.pack.PackOverlaySession
import dev.novux.dynasty.cbb.core.pack.PackOverlaySessions
import dev.novux.dynasty.cbb.core.pack.PackSwapResult
import dev.novux.dynasty.cbb.core.pack.TeamDisplayOverlay
import dev.novux.dynasty.cbb.core.pack.import.IdentityPackImportPipeline
import dev.novux.dynasty.cbb.core.pack.import.PackImportAttestation
import dev.novux.dynasty.cbb.core.pack.import.PackImportFormat
import dev.novux.dynasty.cbb.core.pack.import.PackImportResult
import dev.novux.dynasty.cbb.core.save.DynastySchema
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Loads and holds the active identity pack overlay — display strings only.
 * Dynasty SQLite rows reference `teamId` / `confId`; swapping packs rebuilds this cache.
 */
class PackRepository(
    private val context: Context,
) {
    private val mutex = Mutex()
    private var session: PackOverlaySession? = null
    private var activePack: IdentityPack? = null

    suspend fun ensureEmbeddedLoaded(): PackOverlaySession = mutex.withLock {
        session?.let { return it }
        applyLoaded(loadAsset(DynastySchema.EMBEDDED_PACK_ASSET))
    }

    private fun applyLoaded(loaded: Pair<PackOverlaySession, IdentityPack>): PackOverlaySession {
        session = loaded.first
        activePack = loaded.second
        return loaded.first
    }

    suspend fun activeSession(): PackOverlaySession = ensureEmbeddedLoaded()

    suspend fun teamDisplay(teamId: String): TeamDisplayOverlay? =
        activeSession().teamDisplay(teamId)

    suspend fun swapFromJson(raw: String): Result<PackSwapResult> = mutex.withLock {
        when (val parsed = IdentityPackCodec.parseAndValidate(raw)) {
            is PackLoadResult.Rejected ->
                Result.failure(PackParseException(parsed.error.toString()))
            is PackLoadResult.Success -> {
                val current = session ?: PackOverlaySessions.fromValidatedPack(parsed.pack)
                val swap = PackOverlaySessions.swap(current, parsed.pack)
                session = swap.session
                activePack = parsed.pack
                Result.success(swap)
            }
        }
    }

    suspend fun importPackFromBytes(
        bytes: ByteArray,
        format: PackImportFormat,
        attestation: PackImportAttestation,
    ): Result<PackSwapResult> {
        val raw = bytes.toString(Charsets.UTF_8)
        return importPack(raw, format, attestation)
    }

    suspend fun importPack(
        raw: String,
        format: PackImportFormat,
        attestation: PackImportAttestation,
    ): Result<PackSwapResult> {
        val base = requireActivePack()
        when (
            val imported = IdentityPackImportPipeline.importLocal(
                raw,
                format,
                attestation,
                overlayBase = base,
            )
        ) {
            is PackImportResult.Rejected ->
                return Result.failure(PackParseException(imported.error.toString()))
            is PackImportResult.Success ->
                return swapFromValidatedPack(imported.pack)
        }
    }

    suspend fun importPackFromUrl(
        url: String,
        attestation: PackImportAttestation,
        fetcher: HttpPackRemoteFetcher,
    ): Result<PackSwapResult> {
        val body = fetcher.fetch(url).getOrElse { return Result.failure(it) }
        val base = requireActivePack()
        when (
            val imported = IdentityPackImportPipeline.importFromUrl(url, attestation, body, overlayBase = base)
        ) {
            is PackImportResult.Rejected ->
                return Result.failure(PackParseException(imported.error.toString()))
            is PackImportResult.Success ->
                return swapFromValidatedPack(imported.pack)
        }
    }

    private suspend fun swapFromValidatedPack(pack: IdentityPack): Result<PackSwapResult> = mutex.withLock {
        val current = session ?: PackOverlaySessions.fromValidatedPack(pack)
        val swap = PackOverlaySessions.swap(current, pack)
        session = swap.session
        activePack = pack
        Result.success(swap)
    }

    private suspend fun requireActivePack(): IdentityPack {
        ensureEmbeddedLoaded()
        return mutex.withLock {
            activePack ?: error("active pack missing after load")
        }
    }

    suspend fun restorePack(packId: String): PackOverlaySession = mutex.withLock {
        if (packId == DynastySchema.DEFAULT_PACK_ID) {
            return applyLoaded(loadAsset(DynastySchema.EMBEDDED_PACK_ASSET))
        }
        val existing = session
        if (existing != null && existing.packId == packId) return existing
        error("Unknown pack id: $packId")
    }

    private fun loadAsset(assetPath: String): Pair<PackOverlaySession, IdentityPack> {
        val raw = context.assets.open(assetPath).bufferedReader().use { it.readText() }
        return when (val result = IdentityPackCodec.parseAndValidate(raw)) {
            is PackLoadResult.Success ->
                PackOverlaySessions.fromValidatedPack(result.pack) to result.pack
            is PackLoadResult.Rejected -> throw PackParseException(result.error.toString())
        }
    }
}

class PackParseException(message: String) : IllegalStateException(message)
