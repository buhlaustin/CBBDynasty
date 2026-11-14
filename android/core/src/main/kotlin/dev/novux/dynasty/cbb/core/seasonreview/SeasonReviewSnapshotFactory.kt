package dev.novux.dynasty.cbb.core.seasonreview

import dev.novux.dynasty.cbb.core.balance.BalanceNilAnnualPoolTables
import dev.novux.dynasty.cbb.core.balance.BalanceRecruitingEconomy
import dev.novux.dynasty.cbb.core.nil.NilAnnualPoolEngine
import dev.novux.dynasty.cbb.core.awards.AwardHonorLine
import dev.novux.dynasty.cbb.core.coach.security.CoachSecuritySnapshot
import dev.novux.dynasty.cbb.core.league.LeagueStandingsState
import dev.novux.dynasty.cbb.core.offseason.weeknine.FacilityCategory
import dev.novux.dynasty.cbb.core.offseason.weeknine.FacilityGrades
import dev.novux.dynasty.cbb.core.pack.IdentityPackIndex
import dev.novux.dynasty.cbb.core.pack.TeamDisplayOverlay
import dev.novux.dynasty.cbb.core.recruiting.RecruitingCycleLedger
import dev.novux.dynasty.cbb.core.recruiting.RecruitingPrestigePool
import dev.novux.dynasty.cbb.core.recruiting.RecruitingWeekGrant
import dev.novux.dynasty.cbb.core.school.SchoolSeasonRecord
import kotlin.math.roundToInt

object SeasonReviewSnapshotFactory {
    fun build(
        seasonIndex: Int,
        userTeam: TeamDisplayOverlay,
        packIndex: IdentityPackIndex,
        standings: LeagueStandingsState?,
        ledger: RecruitingCycleLedger,
        economy: BalanceRecruitingEconomy,
        nilTables: BalanceNilAnnualPoolTables,
        facilityGrades: FacilityGrades,
        coach: CoachSecuritySnapshot,
        contractYearsLeft: Int,
        awardHighlights: List<AwardHonorLine> = emptyList(),
    ): SeasonReviewSnapshot {
        val record = standings?.let { SeasonReviewRecordBuilder.build(userTeam.teamId, it, packIndex) }
            ?: SchoolSeasonRecord.Zero
        val net = standings?.netRankByTeam?.get(userTeam.teamId)
        val media = standings?.mediaPollRankByTeam?.get(userTeam.teamId)
        val coaches = standings?.coachesPollRankByTeam?.get(userTeam.teamId)
        val postseason = SeasonReviewPostseasonLabel.forUserTeam(
            userTeamId = userTeam.teamId,
            selection = standings?.selectionSunday,
            opening = standings?.ncbaOpeningRound,
            main = standings?.ncbaMainBracket,
            nci = standings?.nciBracket,
            heritage = standings?.heritageBracket,
        )
        val prestigeStart = ledger.programPrestige
        val prestigeEnd = userTeam.programPrestige
        val nextPool = RecruitingWeekGrant.previewNextCycleWeeklyPool(economy, prestigeEnd)
        val nextNil = NilAnnualPoolEngine.resolve(userTeam.conferenceId, prestigeEnd, nilTables)
        val tier = RecruitingPrestigePool.tierForPrestige(economy, prestigeEnd)
        val gradeRows = FacilityCategory.entries.map { cat ->
            val g = facilityGrades.grade(cat)
            SeasonReviewFacilityGradeRow(
                category = cat,
                grade = g,
                deltaFromBaseline = g - FacilityGrades.DEFAULT_GRADE,
            )
        }
        val avg = gradeRows.map { it.grade }.average().roundToInt()
        return SeasonReviewSnapshot(
            seasonIndex = seasonIndex,
            schoolDisplayName = userTeam.displayName,
            record = record,
            postseasonFinishLabel = postseason,
            netRank = net,
            mediaPollRank = media,
            coachesPollRank = coaches,
            mediaTop25 = media != null && media <= 25,
            coachesTop25 = coaches != null && coaches <= 25,
            prestigeStart = prestigeStart,
            prestigeEnd = prestigeEnd,
            facilityGrades = gradeRows,
            facilityGradeAverage = avg,
            currentCycleWeeklyPool = ledger.weeklyPoolBase,
            nextCycleWeeklyPool = nextPool,
            nextCycleNilPoolUsd = nextNil,
            recruitingTierLabel = tier.label,
            jobSecurityScore = coach.jobSecurity.score,
            jobSecurityLabel = coach.jobSecurity.tier.label,
            adTrustScore = coach.adTrust.score,
            contractYearsLeft = contractYearsLeft,
            awardHighlights = awardHighlights,
        )
    }
}
