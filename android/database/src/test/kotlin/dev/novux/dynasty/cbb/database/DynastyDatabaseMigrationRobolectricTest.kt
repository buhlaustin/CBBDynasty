package dev.novux.dynasty.cbb.database

import android.database.Cursor
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.SupportSQLiteOpenHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class DynastyDatabaseMigrationRobolectricTest {
    @Test
    fun applyFullMigrationChain_reachesCurrentVersion() {
        openAtVersion1().use { db ->
            var version = 1
            ALL_DYNASTY_MIGRATIONS.forEach { migration ->
                migration.migrate(db)
                version = migration.endVersion
                db.execSQL("PRAGMA user_version = $version")
            }
            assertEquals(DYNASTY_DB_VERSION, version)
        }
    }

    @Test
    fun playUpdateOccupiedSlot_fullMigrationChain_preservesDynastyIdForContinue() {
        openAtVersion1().use { db ->
            db.execSQL(
                """
                INSERT INTO save_slots (
                    slotIndex, dynastyId, lastPlayedAtEpochMillis,
                    schoolDisplayName, coachDisplayName, seasonLabel,
                    wins, losses, packId
                ) VALUES (
                    0, 'dynasty-play-update', 1700000000000,
                    'Test U', 'Coach', 'Season 1',
                    12, 3, 'default'
                )
                """.trimIndent(),
            )
            ALL_DYNASTY_MIGRATIONS.forEach { migration -> migration.migrate(db) }
            db.query(
                "SELECT dynastyId FROM save_slots WHERE slotIndex = 0 LIMIT 1",
            ).use { cursor ->
                assertTrue(cursor.moveToFirst())
                assertEquals("dynasty-play-update", cursor.getString(0))
            }
        }
    }

    @Test
    fun migration16To17_preservesProvisionalGameRowAndAddsColumns() {
        openAtVersion1().use { db ->
            ALL_DYNASTY_MIGRATIONS
                .filter { it.endVersion <= 16 }
                .forEach { migration -> migration.migrate(db) }
            db.execSQL(
                """
                INSERT INTO provisional_games (dynastyId, gameId, homeScore, awayScore, simGeneration)
                VALUES ('dynasty-fixture', 'game-1', 72, 68, 3)
                """.trimIndent(),
            )
            MIGRATION_16_17.migrate(db)
            db.query(
                """
                SELECT possessionEngine, boxArchiveJson, homeScore, awayScore
                FROM provisional_games
                WHERE dynastyId = 'dynasty-fixture' AND gameId = 'game-1'
                """.trimIndent(),
            ).use { cursor: Cursor ->
                assertTrue(cursor.moveToFirst())
                assertEquals(0, cursor.getInt(0))
                assertTrue(cursor.isNull(1))
                assertEquals(72, cursor.getInt(2))
                assertEquals(68, cursor.getInt(3))
            }
        }
    }

    private fun openAtVersion1(): SupportSQLiteDatabase {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val config =
            SupportSQLiteOpenHelper.Configuration.builder(context)
                .name(null)
                .callback(
                    object : SupportSQLiteOpenHelper.Callback(1) {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            bootstrapVersion1Schema(db)
                        }

                        override fun onUpgrade(
                            db: SupportSQLiteDatabase,
                            oldVersion: Int,
                            newVersion: Int,
                        ) {
                            error("Tests apply migrations explicitly")
                        }
                    },
                )
                .build()
        val helper = FrameworkSQLiteOpenHelperFactory().create(config)
        return helper.writableDatabase
    }

    private fun bootstrapVersion1Schema(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS save_slots (
                slotIndex INTEGER NOT NULL PRIMARY KEY,
                dynastyId TEXT,
                lastPlayedAtEpochMillis INTEGER,
                schoolDisplayName TEXT,
                coachDisplayName TEXT,
                seasonLabel TEXT,
                wins INTEGER NOT NULL,
                losses INTEGER NOT NULL,
                packId TEXT
            )
            """.trimIndent(),
        )
    }
}
