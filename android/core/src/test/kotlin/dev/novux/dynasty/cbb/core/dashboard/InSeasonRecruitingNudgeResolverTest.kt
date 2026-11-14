package dev.novux.dynasty.cbb.core.dashboard

import dev.novux.dynasty.cbb.core.balance.BalanceRecruitingRecommended
import dev.novux.dynasty.cbb.core.qa.RecruitingDashboardNudgeKindsPath
import dev.novux.dynasty.cbb.core.recruiting.RecruitPoolKind
import dev.novux.dynasty.cbb.core.recruiting.RecruitProspect
import dev.novux.dynasty.cbb.core.recruiting.SchoolRecruitingContext
import dev.novux.dynasty.cbb.core.signingday.NationalRankRow
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class InSeasonRecruitingNudgeResolverTest {
    private val recommended = BalanceRecruitingRecommended(listCap = 40)
    private val school = SchoolRecruitingContext(
        programPrestige = 72,
        schoolHomeState = "OH",
        nilPoolAnnualUsd = 1_000_000,
    )

    @Test
    fun megaRecr146_emitsDistinctDashboardTaskKinds() {
        assertEquals(RecruitingDashboardNudgeKindsPath.RECR_ID, "MEGA-RECR-146")
        val prospect = sampleProspect("p1", "Jordan Hill", stars = 5, hidden = 92)
        val tasks = InSeasonRecruitingOptionalDashboardTasks.build(
            InSeasonRecruitingDashboardContext(
                phase = dev.novux.dynasty.cbb.core.calendar.CalendarPhase.RegularSeason,
                pointsAvailable = 0,
                cycleClosed = false,
                recruitingNudges = listOf(
                    RecruitingDashboardNudge(RecruitingDashboardNudgeKind.WatchlistOffer, "Jordan Hill"),
                    RecruitingDashboardNudge(RecruitingDashboardNudgeKind.WatchlistReEngage, "Sam Lee"),
                    RecruitingDashboardNudge(RecruitingDashboardNudgeKind.Top60Movement, "Jordan Hill", nationalRank = 8),
                    RecruitingDashboardNudge(
                        RecruitingDashboardNudgeKind.RivalCommit,
                        "Jordan Hill",
                        rivalSchoolName = "Blue Ridge U",
                    ),
                ),
            ),
        )
        assertTrue(tasks.any { it.id == DashboardTaskId.RecruitingWatchlistOffer })
        assertTrue(tasks.any { it.id == DashboardTaskId.RecruitingWatchlistReEngage })
        assertTrue(tasks.any { it.id == DashboardTaskId.RecruitingTop60Movement })
        assertTrue(tasks.any { it.id == DashboardTaskId.RecruitingRivalCommit })
    }

    @Test
    fun watchlistOnly_respectsBoardTargets() {
        val pool = listOf(
            sampleProspect("w1", "On Board", stars = 4, hidden = 80),
            sampleProspect("other", "Not Board", stars = 3, hidden = 50),
        )
        val nudges = InSeasonRecruitingNudgeResolver.resolve(
            pool = pool,
            watchlistIds = setOf("w1"),
            school = school,
            userProgramId = "user-dynasty",
            recommended = recommended,
            spendDeltaByProspect = mapOf("w1" to 200),
            top60 = listOf(NationalRankRow(rank = 3, prospect = pool[0])),
            previousTop60Ranks = emptyMap(),
        )
        assertTrue(nudges.isNotEmpty())
        assertTrue(nudges.none { it.prospectName == "Not Board" })
    }

    private fun sampleProspect(id: String, name: String, stars: Int, hidden: Int): RecruitProspect =
        RecruitProspect(
            prospectId = id,
            displayName = name,
            stars = stars,
            hiddenRating = hidden,
            homeState = "OH",
            position = dev.novux.dynasty.cbb.core.roster.PlayerPosition.SmallForward,
            poolKind = RecruitPoolKind.HighSchool,
            topPriorities = emptyList(),
            scoutLetterOverall = "B",
            nilAskUsd = 50_000,
        )
}
