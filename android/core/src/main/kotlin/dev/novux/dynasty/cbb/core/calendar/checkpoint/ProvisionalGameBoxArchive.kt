package dev.novux.dynasty.cbb.core.calendar.checkpoint

import dev.novux.dynasty.cbb.core.injuries.GameSimInjuryEvent
import dev.novux.dynasty.cbb.core.injuries.InjurySeverity
import dev.novux.dynasty.cbb.core.sim.GameSimResult
import dev.novux.dynasty.cbb.core.sim.PlayerBoxScore
import dev.novux.dynasty.cbb.core.sim.TeamBoxScore
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/** Staged box lines without full possession log — persisted with [ProvisionalGameMark]. */
@Serializable
data class ProvisionalGameBoxArchive(
    val homeBox: ArchivedTeamBoxScore,
    val awayBox: ArchivedTeamBoxScore,
    val homePlayerBox: List<ArchivedPlayerBoxScore> = emptyList(),
    val awayPlayerBox: List<ArchivedPlayerBoxScore> = emptyList(),
    val periodsPlayed: Int = 1,
    val overtimePeriods: Int = 0,
    val userTeamInjuries: List<ArchivedGameInjuryEvent> = emptyList(),
) {
    fun toGameSimResult(homeScore: Int, awayScore: Int): GameSimResult = GameSimResult(
        homeScore = homeScore,
        awayScore = awayScore,
        homeBox = homeBox.toTeamBoxScore(),
        awayBox = awayBox.toTeamBoxScore(),
        totalPossessions = homeBox.possessions + awayBox.possessions,
        possessionLog = emptyList(),
        homeInjuries = emptyList(),
        awayInjuries = emptyList(),
        homePlayerBox = homePlayerBox.map { it.toPlayerBoxScore() },
        awayPlayerBox = awayPlayerBox.map { it.toPlayerBoxScore() },
        periodsPlayed = periodsPlayed,
        overtimePeriods = overtimePeriods,
    )

    private fun injuriesForUserHome(userTeamIsHome: Boolean): List<GameSimInjuryEvent> =
        if (userTeamIsHome) {
            userTeamInjuries.map { ArchivedGameInjuryEvent.toDomain(it) }
        } else {
            emptyList()
        }

    fun toGameSimResultForUser(
        userTeamIsHome: Boolean,
        homeScore: Int,
        awayScore: Int,
    ): GameSimResult {
        val base = toGameSimResult(homeScore, awayScore)
        return if (userTeamIsHome) {
            base.copy(homeInjuries = injuriesForUserHome(true))
        } else {
            base.copy(awayInjuries = userTeamInjuries.map { ArchivedGameInjuryEvent.toDomain(it) })
        }
    }

    companion object {
        fun fromResult(
            result: GameSimResult,
            userTeamIsHome: Boolean,
        ): ProvisionalGameBoxArchive {
            val userInjuries = result.userTeamInjuries(userTeamIsHome)
            return ProvisionalGameBoxArchive(
                homeBox = ArchivedTeamBoxScore.from(result.homeBox),
                awayBox = ArchivedTeamBoxScore.from(result.awayBox),
                homePlayerBox = result.homePlayerBox.map { ArchivedPlayerBoxScore.from(it) },
                awayPlayerBox = result.awayPlayerBox.map { ArchivedPlayerBoxScore.from(it) },
                periodsPlayed = result.periodsPlayed,
                overtimePeriods = result.overtimePeriods,
                userTeamInjuries = userInjuries.map { ArchivedGameInjuryEvent.from(it) },
            )
        }
    }
}

@Serializable
data class ArchivedTeamBoxScore(
    val teamId: String,
    val points: Int,
    val fieldGoalsMade: Int,
    val fieldGoalsAttempted: Int,
    val threePointsMade: Int,
    val threePointsAttempted: Int,
    val freeThrowsMade: Int,
    val freeThrowsAttempted: Int,
    val turnovers: Int,
    val possessions: Int,
    val personalFouls: Int,
) {
    companion object {
        fun from(box: TeamBoxScore) = ArchivedTeamBoxScore(
            teamId = box.teamId,
            points = box.points,
            fieldGoalsMade = box.fieldGoalsMade,
            fieldGoalsAttempted = box.fieldGoalsAttempted,
            threePointsMade = box.threePointsMade,
            threePointsAttempted = box.threePointsAttempted,
            freeThrowsMade = box.freeThrowsMade,
            freeThrowsAttempted = box.freeThrowsAttempted,
            turnovers = box.turnovers,
            possessions = box.possessions,
            personalFouls = box.personalFouls,
        )
    }

    fun toTeamBoxScore(): TeamBoxScore = TeamBoxScore(
        teamId = teamId,
        points = points,
        fieldGoalsMade = fieldGoalsMade,
        fieldGoalsAttempted = fieldGoalsAttempted,
        threePointsMade = threePointsMade,
        threePointsAttempted = threePointsAttempted,
        freeThrowsMade = freeThrowsMade,
        freeThrowsAttempted = freeThrowsAttempted,
        turnovers = turnovers,
        possessions = possessions,
        personalFouls = personalFouls,
    )
}

@Serializable
data class ArchivedPlayerBoxScore(
    val playerId: String,
    val minutesSeconds: Int,
    val points: Int,
    val fieldGoalsMade: Int,
    val fieldGoalsAttempted: Int,
    val threePointsMade: Int,
    val threePointsAttempted: Int,
    val freeThrowsMade: Int,
    val freeThrowsAttempted: Int,
    val offensiveRebounds: Int,
    val defensiveRebounds: Int,
    val assists: Int,
    val steals: Int,
    val blocks: Int,
    val turnovers: Int,
    val personalFouls: Int,
) {
    companion object {
        fun from(line: PlayerBoxScore) = ArchivedPlayerBoxScore(
            playerId = line.playerId,
            minutesSeconds = line.minutesSeconds,
            points = line.points,
            fieldGoalsMade = line.fieldGoalsMade,
            fieldGoalsAttempted = line.fieldGoalsAttempted,
            threePointsMade = line.threePointsMade,
            threePointsAttempted = line.threePointsAttempted,
            freeThrowsMade = line.freeThrowsMade,
            freeThrowsAttempted = line.freeThrowsAttempted,
            offensiveRebounds = line.offensiveRebounds,
            defensiveRebounds = line.defensiveRebounds,
            assists = line.assists,
            steals = line.steals,
            blocks = line.blocks,
            turnovers = line.turnovers,
            personalFouls = line.personalFouls,
        )
    }

    fun toPlayerBoxScore(): PlayerBoxScore = PlayerBoxScore(
        playerId = playerId,
        minutesSeconds = minutesSeconds,
        points = points,
        fieldGoalsMade = fieldGoalsMade,
        fieldGoalsAttempted = fieldGoalsAttempted,
        threePointsMade = threePointsMade,
        threePointsAttempted = threePointsAttempted,
        freeThrowsMade = freeThrowsMade,
        freeThrowsAttempted = freeThrowsAttempted,
        offensiveRebounds = offensiveRebounds,
        defensiveRebounds = defensiveRebounds,
        assists = assists,
        steals = steals,
        blocks = blocks,
        turnovers = turnovers,
        personalFouls = personalFouls,
    )
}

@Serializable
data class ArchivedGameInjuryEvent(
    val playerId: String,
    val injuryId: String,
    val label: String,
    val bodyPart: String,
    val severityWire: String,
    val weeksRemaining: Int,
    val occurredInGameMinute: Int? = null,
) {
    companion object {
        fun from(event: GameSimInjuryEvent) = ArchivedGameInjuryEvent(
            playerId = event.playerId,
            injuryId = event.injuryId,
            label = event.label,
            bodyPart = event.bodyPart,
            severityWire = event.severity.wire,
            weeksRemaining = event.weeksRemaining,
            occurredInGameMinute = event.occurredInGameMinute,
        )

        fun toDomain(event: ArchivedGameInjuryEvent): GameSimInjuryEvent {
            val severity = InjurySeverity.entries.firstOrNull { it.wire == event.severityWire }
                ?: InjurySeverity.Minor
            return GameSimInjuryEvent(
                playerId = event.playerId,
                injuryId = event.injuryId,
                label = event.label,
                bodyPart = event.bodyPart,
                severity = severity,
                weeksRemaining = event.weeksRemaining,
                occurredInGameMinute = event.occurredInGameMinute,
            )
        }
    }
}

object ProvisionalGameBoxCodec {
    private val json = Json { ignoreUnknownKeys = true }

    fun encode(archive: ProvisionalGameBoxArchive): String = json.encodeToString(archive)

    fun decode(raw: String?): ProvisionalGameBoxArchive? {
        if (raw.isNullOrBlank()) return null
        return runCatching { json.decodeFromString<ProvisionalGameBoxArchive>(raw) }.getOrNull()
    }
}
