package dev.novux.dynasty.cbb.data.nil

import dev.novux.dynasty.cbb.core.balance.BalanceNilAnnualPoolParser
import dev.novux.dynasty.cbb.core.nil.NilAnnualPoolEngine
import dev.novux.dynasty.cbb.core.recruiting.SchoolRecruitingContext
import dev.novux.dynasty.cbb.core.recruiting.RecruitingPipelineStates
import dev.novux.dynasty.cbb.core.save.DynastySchema
import dev.novux.dynasty.cbb.core.save.LoadedDynasty
import dev.novux.dynasty.cbb.data.pack.PackRepository
import dev.novux.dynasty.cbb.database.DynastyDatabase

class NilAnnualPoolRepository(
    private val database: DynastyDatabase,
    private val packRepository: PackRepository,
    private val nilBudgetRepository: NilBudgetRepository,
) {
    suspend fun effectivePoolUsd(loaded: LoadedDynasty, fundraisingBoostUsd: Int = 0): Long =
        cycleBasePoolUsd(loaded) + fundraisingBoostUsd

    suspend fun userSchoolRecruitingContext(
        loaded: LoadedDynasty,
        fundraisingBoostUsd: Int = 0,
        facilityGrades: dev.novux.dynasty.cbb.core.offseason.weeknine.FacilityGrades =
            dev.novux.dynasty.cbb.core.offseason.weeknine.FacilityGrades(),
    ): SchoolRecruitingContext? {
        val coach = database.dynastyCoachDao().getByDynastyId(loaded.dynastyId) ?: return null
        val packId = loaded.summary.packId ?: DynastySchema.DEFAULT_PACK_ID
        val team = packRepository.restorePack(packId).index.team(coach.userTeamId) ?: return null
        return SchoolRecruitingContext(
            programPrestige = team.programPrestige,
            schoolHomeState = team.homeState,
            pipelineStates = RecruitingPipelineStates.forHomeState(team.homeState),
            nilPoolAnnualUsd = effectivePoolUsd(loaded, fundraisingBoostUsd).toInt(),
            facilityGrades = facilityGrades,
        )
    }

    suspend fun cycleBasePoolUsd(loaded: LoadedDynasty): Long {
        val coach = database.dynastyCoachDao().getByDynastyId(loaded.dynastyId) ?: return fallbackPool(loaded)
        val packId = loaded.summary.packId ?: DynastySchema.DEFAULT_PACK_ID
        val team = packRepository.restorePack(packId).index.team(coach.userTeamId)
            ?: return fallbackPool(loaded)
        val tables = BalanceNilAnnualPoolParser.parse(loaded.balanceRuleset.nil)
        val computed = NilAnnualPoolEngine.resolve(
            conferenceId = team.conferenceId,
            programPrestige = team.programPrestige,
            tables = tables,
        )
        return nilBudgetRepository.ensureFrozenAnnualPool(
            loaded.dynastyId,
            loaded.metadata.seasonIndex,
            computed,
        )
    }

    suspend fun openCyclePoolUsd(
        dynastyId: String,
        balanceNil: kotlinx.serialization.json.JsonElement,
        cycleSeasonIndex: Int,
        conferenceId: String,
        programPrestige: Int,
    ): Long {
        nilBudgetRepository.resetFrozenAnnualPool(dynastyId, cycleSeasonIndex)
        val tables = BalanceNilAnnualPoolParser.parse(balanceNil)
        val computed = NilAnnualPoolEngine.resolve(conferenceId, programPrestige, tables)
        return nilBudgetRepository.ensureFrozenAnnualPool(dynastyId, cycleSeasonIndex, computed)
    }

    private suspend fun fallbackPool(loaded: LoadedDynasty): Long =
        nilBudgetRepository.ensureFrozenAnnualPool(
            loaded.dynastyId,
            loaded.metadata.seasonIndex,
            DEFAULT_FALLBACK_USD,
        )

    companion object {
        const val DEFAULT_FALLBACK_USD = 900_000L
    }
}
