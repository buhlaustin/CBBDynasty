package dev.novux.dynasty.cbb.core.save

import dev.novux.dynasty.cbb.core.calendar.checkpoint.WeekCheckpointState
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/** Portable dynasty backup manifest ([ADR-001](docs/ADR-001-dynasty-save-backend.md), [MEGA-COACH-183]). */
@Serializable
data class DynastyExportArchive(
    val exportFormatVersion: Int = EXPORT_FORMAT_VERSION,
    val exportedAtEpochMillis: Long,
    val slotIndex: Int,
    val dynastyId: String,
    val dynastySchemaVersion: Int,
    val packId: String,
    val balanceRulesetId: String,
    val simTuningId: String,
    val schoolDisplayName: String,
    val coachDisplayName: String,
    val seasonLabel: String,
    val wins: Int,
    val losses: Int,
    val calendarPhaseWire: String,
    val seasonIndex: Int,
    val weekIndex: Int,
    val rosterPlayerCount: Int,
) {
    fun toJson(pretty: Boolean = false): String {
        val json = Json {
            prettyPrint = pretty
            encodeDefaults = true
        }
        return json.encodeToString(this)
    }

    companion object {
        const val EXPORT_FORMAT_VERSION: Int = 1
        const val MEGA_ID: String = "MEGA-COACH-183"
    }
}

object DynastyExportEngine {
    fun build(
        slotIndex: Int,
        summary: SaveSlotSummary,
        metadata: DynastyMetadata,
        weekCheckpoint: WeekCheckpointState,
        rosterPlayerCount: Int,
        exportedAtEpochMillis: Long = System.currentTimeMillis(),
    ): DynastyExportArchive {
        val pos = weekCheckpoint.activePosition
        return DynastyExportArchive(
            exportedAtEpochMillis = exportedAtEpochMillis,
            slotIndex = slotIndex,
            dynastyId = metadata.dynastyId,
            dynastySchemaVersion = metadata.dynastySchemaVersion,
            packId = metadata.packId,
            balanceRulesetId = metadata.balanceRulesetId,
            simTuningId = metadata.simTuningId,
            schoolDisplayName = summary.schoolDisplayName.orEmpty(),
            coachDisplayName = summary.coachDisplayName.orEmpty(),
            seasonLabel = summary.seasonLabel.orEmpty(),
            wins = summary.wins,
            losses = summary.losses,
            calendarPhaseWire = pos.phase.name,
            seasonIndex = metadata.seasonIndex,
            weekIndex = pos.weekIndex,
            rosterPlayerCount = rosterPlayerCount,
        )
    }

    fun build(loaded: LoadedDynasty, rosterPlayerCount: Int, exportedAtEpochMillis: Long = System.currentTimeMillis()): DynastyExportArchive =
        build(
            slotIndex = loaded.slotIndex,
            summary = loaded.summary,
            metadata = loaded.metadata,
            weekCheckpoint = loaded.weekCheckpoint,
            rosterPlayerCount = rosterPlayerCount,
            exportedAtEpochMillis = exportedAtEpochMillis,
        )
}
