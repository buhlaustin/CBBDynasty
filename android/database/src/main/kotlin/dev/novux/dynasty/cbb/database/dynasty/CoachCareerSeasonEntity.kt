package dev.novux.dynasty.cbb.database.dynasty

import androidx.room.Entity

/** Persisted head-coach season row for coach card History tab ([MEGA-COACH-193]). */
@Entity(
    tableName = "coach_career_season",
    primaryKeys = ["dynastyId", "seasonIndex"],
)
data class CoachCareerSeasonEntity(
    val dynastyId: String,
    val seasonIndex: Int,
    val schoolDisplayName: String,
    val recordLabel: String,
    val conferenceRecordLabel: String,
    val postseasonSummary: String,
    val highlightLabelsWire: String,
    val scheduleSummaryLine: String,
    val tournamentPathLine: String,
    val newsTagLinesWire: String,
    val archivedAtEpochMillis: Long,
)
