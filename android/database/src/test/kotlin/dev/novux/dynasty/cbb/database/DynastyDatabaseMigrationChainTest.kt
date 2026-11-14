package dev.novux.dynasty.cbb.database

import androidx.room.migration.Migration
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Test

class DynastyDatabaseMigrationChainTest {
    @Test
    fun allMigrations_formContinuousChain_toCurrentVersion() {
        assertEquals(20, ALL_DYNASTY_MIGRATIONS.size)
        var version = 1
        ALL_DYNASTY_MIGRATIONS.forEach { migration: Migration ->
            assertEquals(version, migration.startVersion)
            version = migration.endVersion
        }
        assertEquals(DYNASTY_DB_VERSION, version)
    }

    @Test
    fun registeredMigrations_endAtDynastyDatabaseVersion() {
        assertSame(MIGRATION_20_21, ALL_DYNASTY_MIGRATIONS.last())
        assertEquals(DYNASTY_DB_VERSION, ALL_DYNASTY_MIGRATIONS.last().endVersion)
    }
}
