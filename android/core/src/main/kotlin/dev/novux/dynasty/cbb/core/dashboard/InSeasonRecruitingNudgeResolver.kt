package dev.novux.dynasty.cbb.core.dashboard

import dev.novux.dynasty.cbb.core.balance.BalanceRecruitingRecommended
import dev.novux.dynasty.cbb.core.recruiting.InfluenceBoardEngine
import dev.novux.dynasty.cbb.core.recruiting.RecruitProspect
import dev.novux.dynasty.cbb.core.recruiting.RecommendedRecruitList
import dev.novux.dynasty.cbb.core.recruiting.SchoolRecruitingContext
import dev.novux.dynasty.cbb.core.signingday.NationalRankRow

object InSeasonRecruitingNudgeResolver {
    private const val WATCHLIST_INTEREST_THRESHOLD = 55
    private const val RIVAL_LEADER_MIN_SCORE = 70

    fun resolve(
        pool: List<RecruitProspect>,
        watchlistIds: Set<String>,
        school: SchoolRecruitingContext,
        userProgramId: String,
        recommended: BalanceRecruitingRecommended,
        spendDeltaByProspect: Map<String, Int>,
        top60: List<NationalRankRow>,
        scholarshipOfferProspectIds: Set<String> = emptySet(),
        previousTop60Ranks: Map<String, Int> = emptyMap(),
    ): List<RecruitingDashboardNudge> {
        if (pool.isEmpty() || watchlistIds.isEmpty()) return emptyList()
        val rankById = top60.associate { it.prospect.prospectId to it.rank }
        val rows = RecommendedRecruitList.build(
            pool,
            school,
            recommended,
            spendDeltaByProspect = spendDeltaByProspect,
        ).filter { it.prospect.prospectId in watchlistIds }
        if (rows.isEmpty()) return emptyList()

        return buildList {
            rows.firstOrNull { it.liveInterest >= WATCHLIST_INTEREST_THRESHOLD }?.let { row ->
                add(
                    RecruitingDashboardNudge(
                        kind = RecruitingDashboardNudgeKind.WatchlistOffer,
                        prospectName = row.prospect.displayName,
                    ),
                )
            }
            rows.firstOrNull { row ->
                val spent = spendDeltaByProspect[row.prospect.prospectId] ?: 0
                spent > 0 && row.liveInterest < WATCHLIST_INTEREST_THRESHOLD
            }?.let { row ->
                add(
                    RecruitingDashboardNudge(
                        kind = RecruitingDashboardNudgeKind.WatchlistReEngage,
                        prospectName = row.prospect.displayName,
                    ),
                )
            }
            val movement = RecruitingTop60MovementPolicy.detectMovements(
                watchlistIds = watchlistIds,
                prospectsById = pool.associateBy { it.prospectId },
                previousRankByProspect = previousTop60Ranks,
                currentRankByProspect = rankById,
            ).firstOrNull()
            movement?.let {
                add(
                    RecruitingDashboardNudge(
                        kind = RecruitingDashboardNudgeKind.Top60Movement,
                        prospectName = it.prospectName,
                        nationalRank = it.rank,
                    ),
                )
            }
            rows.firstNotNullOfOrNull { row ->
                val board = InfluenceBoardEngine.build(
                    prospect = row.prospect,
                    userSchool = school,
                    userProgramId = userProgramId,
                    recommended = recommended,
                    userHasScholarshipOffer = row.prospect.prospectId in scholarshipOfferProspectIds,
                )
                val leader = board.leaders.firstOrNull() ?: return@firstNotNullOfOrNull null
                if (leader.programId != userProgramId && leader.influenceScore >= RIVAL_LEADER_MIN_SCORE) {
                    RecruitingDashboardNudge(
                        kind = RecruitingDashboardNudgeKind.RivalCommit,
                        prospectName = row.prospect.displayName,
                        rivalSchoolName = leader.displayName,
                    )
                } else {
                    null
                }
            }?.let(::add)
        }
    }
}
