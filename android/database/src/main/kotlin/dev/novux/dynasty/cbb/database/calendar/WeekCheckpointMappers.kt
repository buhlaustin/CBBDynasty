package dev.novux.dynasty.cbb.database.calendar

import dev.novux.dynasty.cbb.core.calendar.CalendarPosition
import dev.novux.dynasty.cbb.core.calendar.PhaseWeekPolicy
import dev.novux.dynasty.cbb.core.calendar.checkpoint.ProvisionalGameBoxCodec
import dev.novux.dynasty.cbb.core.calendar.checkpoint.ProvisionalGameMark
import dev.novux.dynasty.cbb.core.calendar.checkpoint.WeekCheckpointState
import dev.novux.dynasty.cbb.core.save.DynastyMetadata

fun DynastyCalendarEntity.toCheckpointState(
    provisional: List<ProvisionalGameEntity>,
): WeekCheckpointState {
    val active = CalendarPosition(
        seasonIndex = seasonIndex,
        phase = phase,
        weekIndex = activeWeekIndex,
    )
    val committed = if (
        committedSeasonIndex != null &&
        committedPhase != null &&
        committedWeekIndex != null
    ) {
        CalendarPosition(
            seasonIndex = committedSeasonIndex,
            phase = committedPhase,
            weekIndex = committedWeekIndex,
        )
    } else {
        null
    }
  return WeekCheckpointState(
        activePosition = active,
        committedThrough = committed,
        provisionalGames = provisional.associate { it.gameId to it.toMark() },
    )
}

fun WeekCheckpointState.toCalendarEntity(dynastyId: String): DynastyCalendarEntity =
    DynastyCalendarEntity(
        dynastyId = dynastyId,
        seasonIndex = activePosition.seasonIndex,
        phase = activePosition.phase,
        activeWeekIndex = activePosition.weekIndex,
        committedSeasonIndex = committedThrough?.seasonIndex,
        committedPhase = committedThrough?.phase,
        committedWeekIndex = committedThrough?.weekIndex,
    )

fun ProvisionalGameEntity.toMark(): ProvisionalGameMark =
    ProvisionalGameMark(
        gameId = gameId,
        homeScore = homeScore,
        awayScore = awayScore,
        simGeneration = simGeneration,
        possessionEngine = possessionEngine,
        boxArchive = ProvisionalGameBoxCodec.decode(boxArchiveJson),
    )

fun ProvisionalGameMark.toEntity(dynastyId: String): ProvisionalGameEntity =
    ProvisionalGameEntity(
        dynastyId = dynastyId,
        gameId = gameId,
        homeScore = homeScore,
        awayScore = awayScore,
        simGeneration = simGeneration,
        possessionEngine = possessionEngine,
        boxArchiveJson = boxArchive?.let { ProvisionalGameBoxCodec.encode(it) },
    )

fun DynastyMetadata.bootstrapCalendarEntity(): DynastyCalendarEntity {
    val pos = PhaseWeekPolicy.bootstrapPosition(seasonIndex, phase)
    return DynastyCalendarEntity(
        dynastyId = dynastyId,
        seasonIndex = pos.seasonIndex,
        phase = pos.phase,
        activeWeekIndex = pos.weekIndex,
        committedSeasonIndex = null,
        committedPhase = null,
        committedWeekIndex = null,
    )
}
