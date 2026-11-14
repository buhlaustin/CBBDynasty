package dev.novux.dynasty.cbb.database.dynasty

import dev.novux.dynasty.cbb.core.coach.card.history.CoachSeasonHistoryDetail
import dev.novux.dynasty.cbb.core.coach.card.history.CoachSeasonHistoryRow

private const val LIST_DELIM = '\u001F'

fun CoachSeasonHistoryRow.toEntity(dynastyId: String, archivedAtEpochMillis: Long): CoachCareerSeasonEntity =
    CoachCareerSeasonEntity(
        dynastyId = dynastyId,
        seasonIndex = seasonIndex,
        schoolDisplayName = schoolDisplayName,
        recordLabel = recordLabel,
        conferenceRecordLabel = conferenceRecordLabel,
        postseasonSummary = postseasonSummary,
        highlightLabelsWire = highlightLabels.joinToString(LIST_DELIM.toString()),
        scheduleSummaryLine = detail.scheduleSummaryLine,
        tournamentPathLine = detail.tournamentPathLine,
        newsTagLinesWire = detail.newsTagLines.joinToString(LIST_DELIM.toString()),
        archivedAtEpochMillis = archivedAtEpochMillis,
    )

fun CoachCareerSeasonEntity.toRow(): CoachSeasonHistoryRow =
    CoachSeasonHistoryRow(
        seasonIndex = seasonIndex,
        schoolDisplayName = schoolDisplayName,
        recordLabel = recordLabel,
        conferenceRecordLabel = conferenceRecordLabel,
        postseasonSummary = postseasonSummary,
        highlightLabels = highlightLabelsWire.split(LIST_DELIM).filter { it.isNotBlank() },
        detail = CoachSeasonHistoryDetail(
            scheduleSummaryLine = scheduleSummaryLine,
            tournamentPathLine = tournamentPathLine,
            newsTagLines = newsTagLinesWire.split(LIST_DELIM).filter { it.isNotBlank() },
        ),
    )
