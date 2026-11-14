package dev.novux.dynasty.cbb.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import dev.novux.dynasty.cbb.database.calendar.DynastyCalendarDao
import dev.novux.dynasty.cbb.database.calendar.DynastyCalendarEntity
import dev.novux.dynasty.cbb.database.calendar.ProvisionalGameDao
import dev.novux.dynasty.cbb.database.calendar.ProvisionalGameEntity
import dev.novux.dynasty.cbb.database.dynasty.CoachCareerSeasonDao
import dev.novux.dynasty.cbb.database.dynasty.CoachCareerSeasonEntity
import dev.novux.dynasty.cbb.database.dynasty.DynastyCoachDao
import dev.novux.dynasty.cbb.database.dynasty.DynastyCoachEntity
import dev.novux.dynasty.cbb.database.dynasty.DynastyMetadataDao
import dev.novux.dynasty.cbb.database.dynasty.DynastyMetadataEntity
import dev.novux.dynasty.cbb.database.roster.DynastyRosterDao
import dev.novux.dynasty.cbb.database.roster.DynastyRosterPlayerEntity
import dev.novux.dynasty.cbb.database.roster.PlayerSeasonStatsDao
import dev.novux.dynasty.cbb.database.roster.PlayerSeasonStatsEntity
import dev.novux.dynasty.cbb.database.save.SaveSlotDao
import dev.novux.dynasty.cbb.database.save.SaveSlotEntity
import dev.novux.dynasty.cbb.database.staff.DynastyStaffDao
import dev.novux.dynasty.cbb.database.staff.DynastyStaffMemberEntity
import dev.novux.dynasty.cbb.database.strategy.DynastyStrategyPrepDao
import dev.novux.dynasty.cbb.database.strategy.DynastyStrategyPrepEntity

@Database(
    entities = [
        SaveSlotEntity::class,
        DynastyMetadataEntity::class,
        DynastyCalendarEntity::class,
        ProvisionalGameEntity::class,
        DynastyCoachEntity::class,
        CoachCareerSeasonEntity::class,
        DynastyRosterPlayerEntity::class,
        PlayerSeasonStatsEntity::class,
        DynastyStaffMemberEntity::class,
        DynastyStrategyPrepEntity::class,
    ],
    version = 22,
    exportSchema = false,
)
@androidx.room.TypeConverters(Converters::class)
abstract class DynastyDatabase : RoomDatabase() {
    abstract fun saveSlotDao(): SaveSlotDao

    abstract fun dynastyMetadataDao(): DynastyMetadataDao

    abstract fun dynastyCoachDao(): DynastyCoachDao

    abstract fun coachCareerSeasonDao(): CoachCareerSeasonDao

    abstract fun dynastyRosterDao(): DynastyRosterDao

    abstract fun playerSeasonStatsDao(): PlayerSeasonStatsDao

    abstract fun dynastyStaffDao(): DynastyStaffDao

    abstract fun dynastyCalendarDao(): DynastyCalendarDao

    abstract fun provisionalGameDao(): ProvisionalGameDao

    abstract fun dynastyStrategyPrepDao(): DynastyStrategyPrepDao

    companion object {
        const val DB_NAME = "dynasty.db"

        fun build(context: Context): DynastyDatabase =
            Room.databaseBuilder(context, DynastyDatabase::class.java, DB_NAME)
                .addMigrations(*ALL_DYNASTY_MIGRATIONS)
                .build()
    }
}
