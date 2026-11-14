package dev.novux.dynasty.cbb.core.school

import dev.novux.dynasty.cbb.core.pack.IdentityPackIndex
import dev.novux.dynasty.cbb.core.pack.TeamDisplayOverlay
import dev.novux.dynasty.cbb.core.academics.ProgramAcademicsSnapshot
import dev.novux.dynasty.cbb.core.facilities.FacilityGradeSummary
import dev.novux.dynasty.cbb.core.resume.TeamResumeMetrics

object SchoolHubSnapshotFactory {
    fun build(
        team: TeamDisplayOverlay,
        packIndex: IdentityPackIndex,
        userTeamId: String,
        record: SchoolSeasonRecord = SchoolSeasonRecord.Zero,
        rankingsPublished: Boolean,
        resume: TeamResumeMetrics? = null,
        netRankings: Map<String, Int>? = null,
        mediaPollRankings: Map<String, Int>? = null,
        coachesPollRankings: Map<String, Int>? = null,
        facilitySummary: FacilityGradeSummary? = null,
        programAcademics: ProgramAcademicsSnapshot? = null,
    ): SchoolHubOverview {
        val conference = packIndex.conference(team.conferenceId)
        val mates = packIndex.allTeamDisplays().filter {
            it.conferenceId == team.conferenceId && it.teamId != team.teamId
        }
        val netMap = netRankings?.takeIf { it.isNotEmpty() }
            ?: SchoolNetRankIndex.build(packIndex.allTeamDisplays())
        val netRank = netMap[team.teamId]
        val confRanks = mates.plus(team).sortedByDescending { it.programPrestige }
        val confStanding = confRanks.indexOfFirst { it.teamId == team.teamId }.let { idx ->
            if (idx < 0) null else SchoolMetricsEstimator.conferenceStandingPlaceholder(idx + 1, confRanks.size)
        }
        val sos = resume?.sosRating
            ?: SchoolMetricsEstimator.strengthOfSchedule(team, mates)
        val (off, def) = SchoolMetricsEstimator.teamRatings(team)
        val mediaRank = mediaPollRankings?.get(team.teamId)
            ?: netRank?.let { SchoolNetRankIndex.mediaPollRank(it) }
        val coachesRank = coachesPollRankings?.get(team.teamId)
            ?: netRank?.let { SchoolNetRankIndex.coachesPollRank(it) }
        val rankings = if (rankingsPublished && netRank != null) {
            SchoolRankingsSnapshot(
                netRank = netRank,
                mediaPollRank = mediaRank,
                coachesPollRank = coachesRank,
                mediaTop25 = mediaRank != null && mediaRank <= 25,
                coachesTop25 = coachesRank != null && coachesRank <= 25,
                conferenceStandingLabel = confStanding,
            )
        } else {
            SchoolRankingsSnapshot(
                netRank = null,
                mediaPollRank = null,
                coachesPollRank = null,
                conferenceStandingLabel = confStanding,
            )
        }
        return SchoolHubOverview(
            teamId = team.teamId,
            displayName = team.displayName,
            conferenceName = conference?.displayName ?: team.conferenceId,
            isUserSchool = team.teamId == userTeamId,
            programPrestige = team.programPrestige,
            sosRating = sos,
            powerIndex = SchoolMetricsEstimator.powerIndex(team.programPrestige, sos),
            teamOffRating = off,
            teamDefRating = def,
            record = record,
            rankings = rankings,
            facilitySummary = if (team.teamId == userTeamId) facilitySummary else null,
            programAcademicsGrade = if (team.teamId == userTeamId) programAcademics?.programGrade else null,
            recruitingBudgetMultiplier = if (team.teamId == userTeamId) {
                programAcademics?.recruitingBudgetMultiplier
            } else {
                null
            },
        )
    }
}
