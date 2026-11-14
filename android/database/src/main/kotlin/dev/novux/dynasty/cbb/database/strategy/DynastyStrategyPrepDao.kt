package dev.novux.dynasty.cbb.database.strategy

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface DynastyStrategyPrepDao {
    @Query("SELECT * FROM dynasty_strategy_prep WHERE dynastyId = :dynastyId LIMIT 1")
    suspend fun get(dynastyId: String): DynastyStrategyPrepEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: DynastyStrategyPrepEntity)
}
