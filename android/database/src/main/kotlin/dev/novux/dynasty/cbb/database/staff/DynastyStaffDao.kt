package dev.novux.dynasty.cbb.database.staff

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert

@Dao
interface DynastyStaffDao {
    @Upsert
    suspend fun upsertAll(entities: List<DynastyStaffMemberEntity>)

    @Query("SELECT * FROM dynasty_staff_member WHERE dynastyId = :dynastyId ORDER BY staffKindWire, staffId")
    suspend fun listForDynasty(dynastyId: String): List<DynastyStaffMemberEntity>

    @Query("DELETE FROM dynasty_staff_member WHERE dynastyId = :dynastyId")
    suspend fun deleteByDynastyId(dynastyId: String)

    @Query("DELETE FROM dynasty_staff_member WHERE dynastyId = :dynastyId AND staffId = :staffId")
    suspend fun deleteStaff(dynastyId: String, staffId: String)
}
