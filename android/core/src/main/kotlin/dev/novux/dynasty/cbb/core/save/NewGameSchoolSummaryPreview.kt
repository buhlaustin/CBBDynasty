package dev.novux.dynasty.cbb.core.save

import dev.novux.dynasty.cbb.core.balance.BalanceNilAnnualPoolTables
import dev.novux.dynasty.cbb.core.balance.BalanceRecruitingEconomy
import dev.novux.dynasty.cbb.core.nil.NilAnnualPoolEngine
import dev.novux.dynasty.cbb.core.pack.TeamDisplayOverlay
import dev.novux.dynasty.cbb.core.recruiting.RecruitingPipelineStates
import dev.novux.dynasty.cbb.core.recruiting.RecruitingPrestigePool

data class NewGameSchoolSummaryPreview(
    val weeklyRecruitingPoints: Int,
    val annualNilUsd: Long,
    val pipelineStates: Set<String>,
)

/**
 * Wizard school summary recruiting/NIL preview ([planning/game-start.md#school-summary-sheet], [MEGA-RECR-072]).
 */
object NewGameSchoolSummaryPreviewEngine {
    fun build(
        school: TeamDisplayOverlay,
        economy: BalanceRecruitingEconomy,
        nilTables: BalanceNilAnnualPoolTables,
    ): NewGameSchoolSummaryPreview {
        val pipeline = RecruitingPipelineStates.forHomeState(school.homeState)
        return NewGameSchoolSummaryPreview(
            weeklyRecruitingPoints = RecruitingPrestigePool.weeklyPoolPoints(economy, school.programPrestige),
            annualNilUsd = NilAnnualPoolEngine.resolve(
                school.conferenceId,
                school.programPrestige,
                nilTables,
            ),
            pipelineStates = pipeline,
        )
    }
}
