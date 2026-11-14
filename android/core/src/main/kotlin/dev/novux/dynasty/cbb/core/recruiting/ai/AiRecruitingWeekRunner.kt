package dev.novux.dynasty.cbb.core.recruiting.ai

import dev.novux.dynasty.cbb.core.balance.BalanceRecruitingActions
import dev.novux.dynasty.cbb.core.balance.BalanceRecruitingEconomy
import dev.novux.dynasty.cbb.core.balance.BalanceRecruitingRecommended
import dev.novux.dynasty.cbb.core.recruiting.RecruitingActionSpend
import dev.novux.dynasty.cbb.core.recruiting.RecruitingPointAction
import dev.novux.dynasty.cbb.core.recruiting.RecruitProspect
import dev.novux.dynasty.cbb.core.recruiting.RecruitingPrestigePool
import dev.novux.dynasty.cbb.core.recruiting.SchoolRecruitingContext

data class AiRecruitingWeekResult(
    val leagueBoards: List<AiRecruitingBoard>,
    val userBoard: AiRecruitingBoard?,
    val userAutoMaintained: Boolean,
    val userPointsSpent: Int,
    val leagueProgramsProcessed: Int,
)

object AiRecruitingWeekRunner {
    fun estimateUserAutoMaintainSpend(
        prospectPool: List<RecruitProspect>,
        economy: BalanceRecruitingEconomy,
        recommended: BalanceRecruitingRecommended,
        userSchool: SchoolRecruitingContext,
        userProgramId: String,
        userPointsAvailable: Int,
        actionCosts: BalanceRecruitingActions,
    ): Int {
        if (userPointsAvailable <= 0) return 0
        return buildBoard(
            programId = userProgramId,
            school = userSchool,
            weeklyPoints = userPointsAvailable,
            pool = prospectPool,
            economy = economy,
            recommended = recommended,
            actionCosts = actionCosts,
        ).pointsSpentThisWeek
    }

    fun runWeek(
        prospectPool: List<RecruitProspect>,
        economy: BalanceRecruitingEconomy,
        recommended: BalanceRecruitingRecommended,
        userProgramId: String,
        userSchool: SchoolRecruitingContext,
        userPointsAvailable: Int,
        userPointsSpentByPlayer: Int,
        autoMaintainUserSchool: Boolean,
        leaguePrograms: List<AiProgramSnapshot>,
        actionCosts: BalanceRecruitingActions,
    ): AiRecruitingWeekResult {
        val leagueBoards = leaguePrograms.map { program ->
            val weeklyPoints = RecruitingPrestigePool.weeklyPoolPoints(economy, program.programPrestige)
            buildBoard(
                programId = program.programId,
                school = program.toSchoolContext(),
                weeklyPoints = weeklyPoints,
                pool = prospectPool,
                economy = economy,
                recommended = recommended,
                actionCosts = actionCosts,
            )
        }
        val shouldAutoMaintain = autoMaintainUserSchool && userPointsSpentByPlayer == 0 && userPointsAvailable > 0
        val userBoard = if (shouldAutoMaintain) {
            buildBoard(
                programId = userProgramId,
                school = userSchool,
                weeklyPoints = userPointsAvailable,
                pool = prospectPool,
                economy = economy,
                recommended = recommended,
                actionCosts = actionCosts,
            )
        } else {
            null
        }
        return AiRecruitingWeekResult(
            leagueBoards = leagueBoards,
            userBoard = userBoard,
            userAutoMaintained = userBoard != null,
            userPointsSpent = userBoard?.pointsSpentThisWeek ?: 0,
            leagueProgramsProcessed = leagueBoards.size,
        )
    }

    private fun buildBoard(
        programId: String,
        school: SchoolRecruitingContext,
        weeklyPoints: Int,
        pool: List<RecruitProspect>,
        economy: BalanceRecruitingEconomy,
        recommended: BalanceRecruitingRecommended,
        actionCosts: BalanceRecruitingActions,
    ): AiRecruitingBoard {
        val ordered = AiRecruitingInterestParityPolicy.rankedProspects(pool, school, recommended)
        val cap = AiRecruitingCapacity.coreTargetCap(economy, school.programPrestige)
        val targets = ordered.take(cap).map { it.prospect.prospectId }
        var remaining = weeklyPoints
        val offerCost = RecruitingActionSpend.catalogCost(RecruitingPointAction.ScholarshipOffer, actionCosts)
        val dmCost = RecruitingActionSpend.catalogCost(RecruitingPointAction.Dm, actionCosts)
        for (targetId in targets) {
            if (targetId.isEmpty()) continue
            if (remaining < dmCost) break
            if (remaining >= offerCost) {
                val offer = RecruitingActionSpend.spend(
                    RecruitingPointAction.ScholarshipOffer,
                    remaining,
                    actionCosts,
                )
                if (offer.success) remaining = offer.pointsRemaining
            }
            if (remaining >= dmCost) {
                val dm = RecruitingActionSpend.spend(RecruitingPointAction.Dm, remaining, actionCosts)
                if (dm.success) remaining = dm.pointsRemaining
            }
        }
        val spent = weeklyPoints - remaining
        return AiRecruitingBoard(
            programId = programId,
            targetProspectIds = targets,
            pointsSpentThisWeek = spent,
        )
    }
}
