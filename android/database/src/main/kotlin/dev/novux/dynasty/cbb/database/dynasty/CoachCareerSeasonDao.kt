package dev.novux.dynasty.cbb.database.dynasty

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface CoachCareerSeasonDao {
    @Query(
        """
        SELECT * FROM coach_career_season
        WHERE dynastyId = :dynastyId
        ORDER BY seasonIndex DESC
        """,
    )
    suspend fun listForDynasty(dynastyId: String): List<CoachCareerSeasonEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: CoachCareerSeasonEntity)

    @Query("DELETE FROM coach_career_season WHERE dynastyId = :dynastyId")
    suspend fun deleteForDynasty(dynastyId: String)
}
