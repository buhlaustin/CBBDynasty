package dev.novux.dynasty.cbb.data.strategy

import dev.novux.dynasty.cbb.core.strategy.DefensiveSchemePreset
import dev.novux.dynasty.cbb.core.strategy.OffensiveSchemePreset
import dev.novux.dynasty.cbb.database.DynastyDatabase
import dev.novux.dynasty.cbb.database.strategy.DynastyStrategyPrepEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class DynastyStrategyPrep(
    val coachOffensiveScheme: OffensiveSchemePreset,
    val coachDefensiveScheme: DefensiveSchemePreset,
    val gameOffensiveScheme: OffensiveSchemePreset,
    val gameDefensiveScheme: DefensiveSchemePreset,
)

class DynastyStrategyPrepRepository(
    database: DynastyDatabase,
) {
    private val dao = database.dynastyStrategyPrepDao()
    private val coachDao = database.dynastyCoachDao()

    suspend fun loadForStrategy(dynastyId: String): DynastyStrategyPrep {
        val coach = coachDao.getByDynastyId(dynastyId)
        val coachOff = OffensiveSchemePreset.fromWire(coach?.offensiveSchemeWire.orEmpty())
            ?: OffensiveSchemePreset.Balanced
        val coachDef = DefensiveSchemePreset.fromWire(coach?.defensiveSchemeWire.orEmpty())
            ?: DefensiveSchemePreset.ManToMan
        val saved = dao.get(dynastyId)
        if (saved == null) {
            return DynastyStrategyPrep(
                coachOffensiveScheme = coachOff,
                coachDefensiveScheme = coachDef,
                gameOffensiveScheme = coachOff,
                gameDefensiveScheme = coachDef,
            )
        }
        val gameOff = OffensiveSchemePreset.fromWire(saved.offensiveSchemeWire) ?: coachOff
        val gameDef = DefensiveSchemePreset.fromWire(saved.defensiveSchemeWire) ?: coachDef
        return DynastyStrategyPrep(
            coachOffensiveScheme = coachOff,
            coachDefensiveScheme = coachDef,
            gameOffensiveScheme = gameOff,
            gameDefensiveScheme = gameDef,
        )
    }

    suspend fun saveGamePrep(
        dynastyId: String,
        offensive: OffensiveSchemePreset,
        defensive: DefensiveSchemePreset,
    ) = withContext(Dispatchers.IO) {
        dao.upsert(
            DynastyStrategyPrepEntity(
                dynastyId = dynastyId,
                offensiveSchemeWire = offensive.wireId,
                defensiveSchemeWire = defensive.wireId,
            ),
        )
    }

    suspend fun resolveForLeagueSim(dynastyId: String): Pair<OffensiveSchemePreset, DefensiveSchemePreset> {
        val prep = loadForStrategy(dynastyId)
        return prep.gameOffensiveScheme to prep.gameDefensiveScheme
    }
}
