package dev.novux.dynasty.cbb.database.roster

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface PlayerSeasonStatsDao {
    @Query("SELECT * FROM player_season_stats WHERE dynastyId = :dynastyId")
    suspend fun listForDynasty(dynastyId: String): List<PlayerSeasonStatsEntity>

    @Query(
        """
        SELECT * FROM player_season_stats
        WHERE dynastyId = :dynastyId AND playerId = :playerId AND seasonIndex = :seasonIndex
        LIMIT 1
        """,
    )
    suspend fun getSeason(dynastyId: String, playerId: String, seasonIndex: Int): PlayerSeasonStatsEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: PlayerSeasonStatsEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(entities: List<PlayerSeasonStatsEntity>)

    @Query("DELETE FROM player_season_stats WHERE dynastyId = :dynastyId")
    suspend fun deleteForDynasty(dynastyId: String)
}
