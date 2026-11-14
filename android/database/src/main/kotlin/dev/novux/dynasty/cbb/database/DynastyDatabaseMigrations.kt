package dev.novux.dynasty.cbb.database

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import dev.novux.dynasty.cbb.core.save.DynastySchema

val MIGRATION_21_22 = object : Migration(21, 22) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS dynasty_strategy_prep (
                dynastyId TEXT NOT NULL PRIMARY KEY,
                offensiveSchemeWire TEXT NOT NULL,
                defensiveSchemeWire TEXT NOT NULL
            )
            """.trimIndent(),
        )
    }
}

val MIGRATION_20_21 = object : Migration(20, 21) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS player_season_stats (
                dynastyId TEXT NOT NULL,
                playerId TEXT NOT NULL,
                seasonIndex INTEGER NOT NULL,
                gamesPlayed INTEGER NOT NULL,
                schoolDisplayName TEXT,
                points INTEGER NOT NULL,
                assists INTEGER NOT NULL,
                rebounds INTEGER NOT NULL,
                steals INTEGER NOT NULL,
                blocks INTEGER NOT NULL,
                turnovers INTEGER NOT NULL,
                fieldGoalsMade INTEGER NOT NULL,
                fieldGoalsAttempted INTEGER NOT NULL,
                threePointsMade INTEGER NOT NULL,
                threePointsAttempted INTEGER NOT NULL,
                freeThrowsMade INTEGER NOT NULL,
                freeThrowsAttempted INTEGER NOT NULL,
                gameLogJson TEXT NOT NULL,
                PRIMARY KEY(dynastyId, playerId, seasonIndex)
            )
            """.trimIndent(),
        )
        db.execSQL(
            "CREATE INDEX IF NOT EXISTS index_player_season_stats_dynastyId ON player_season_stats(dynastyId)",
        )
    }
}

val MIGRATION_19_20 = object : Migration(19, 20) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS coach_career_season (
                dynastyId TEXT NOT NULL,
                seasonIndex INTEGER NOT NULL,
                schoolDisplayName TEXT NOT NULL,
                recordLabel TEXT NOT NULL,
                conferenceRecordLabel TEXT NOT NULL,
                postseasonSummary TEXT NOT NULL,
                highlightLabelsWire TEXT NOT NULL,
                scheduleSummaryLine TEXT NOT NULL,
                tournamentPathLine TEXT NOT NULL,
                newsTagLinesWire TEXT NOT NULL,
                archivedAtEpochMillis INTEGER NOT NULL,
                PRIMARY KEY(dynastyId, seasonIndex)
            )
            """.trimIndent(),
        )
        db.execSQL(
            "CREATE INDEX IF NOT EXISTS index_coach_career_season_dynastyId ON coach_career_season(dynastyId)",
        )
    }
}

val MIGRATION_18_19 = object : Migration(18, 19) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            "ALTER TABLE dynasty_roster_player ADD COLUMN nilDealRaisedThisSeason INTEGER NOT NULL DEFAULT 0",
        )
    }
}

val MIGRATION_17_18 = object : Migration(17, 18) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            "ALTER TABLE dynasty_roster_player ADD COLUMN promiseHistoryWire TEXT NOT NULL DEFAULT ''",
        )
    }
}

val MIGRATION_16_17 = object : Migration(16, 17) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            "ALTER TABLE provisional_games ADD COLUMN possessionEngine INTEGER NOT NULL DEFAULT 0",
        )
        db.execSQL("ALTER TABLE provisional_games ADD COLUMN boxArchiveJson TEXT")
    }
}

val MIGRATION_15_16 = object : Migration(15, 16) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE dynasty_roster_player ADD COLUMN skinToneId TEXT NOT NULL DEFAULT ''")
        db.execSQL("ALTER TABLE dynasty_roster_player ADD COLUMN hairColorId TEXT NOT NULL DEFAULT ''")
        db.execSQL("ALTER TABLE dynasty_roster_player ADD COLUMN hairStyleId TEXT NOT NULL DEFAULT ''")
        db.execSQL("ALTER TABLE dynasty_roster_player ADD COLUMN avatarPresetId TEXT NOT NULL DEFAULT ''")
    }
}

val MIGRATION_14_15 = object : Migration(14, 15) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE dynasty_roster_player ADD COLUMN stamina INTEGER NOT NULL DEFAULT 100")
    }
}

val MIGRATION_13_14 = object : Migration(13, 14) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            "ALTER TABLE dynasty_roster_player ADD COLUMN gamesSuspendedRemaining INTEGER NOT NULL DEFAULT 0",
        )
        db.execSQL("ALTER TABLE dynasty_roster_player ADD COLUMN academicHold INTEGER NOT NULL DEFAULT 0")
        db.execSQL("ALTER TABLE dynasty_roster_player ADD COLUMN gpaCurrent REAL NOT NULL DEFAULT 2.5")
        db.execSQL("ALTER TABLE dynasty_roster_player ADD COLUMN academicCoverUp INTEGER NOT NULL DEFAULT 0")
        db.execSQL("ALTER TABLE dynasty_roster_player ADD COLUMN conductIncidentId TEXT")
    }
}

val MIGRATION_12_13 = object : Migration(12, 13) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE dynasty_roster_player ADD COLUMN injurySeverityWire TEXT")
        db.execSQL(
            "ALTER TABLE dynasty_roster_player ADD COLUMN injuryOccurredAtRsWeek INTEGER NOT NULL DEFAULT 0",
        )
        db.execSQL(
            "ALTER TABLE dynasty_roster_player ADD COLUMN injuryOccurredAtRsGameIndex INTEGER NOT NULL DEFAULT 0",
        )
        db.execSQL(
            "ALTER TABLE dynasty_roster_player ADD COLUMN rsGamesPlayedAtInjury INTEGER NOT NULL DEFAULT 0",
        )
        db.execSQL(
            "ALTER TABLE dynasty_roster_player ADD COLUMN rsGamesPlayedSeason INTEGER NOT NULL DEFAULT 0",
        )
        db.execSQL("ALTER TABLE dynasty_roster_player ADD COLUMN injuryWasStarterSlotWire TEXT")
        db.execSQL("ALTER TABLE dynasty_roster_player ADD COLUMN injuryPriorRotationBand INTEGER")
        db.execSQL("ALTER TABLE dynasty_roster_player ADD COLUMN starterSlotWire TEXT")
        db.execSQL(
            """
            UPDATE dynasty_roster_player
            SET starterSlotWire = positionWire
            WHERE isStarter = 1 AND starterSlotWire IS NULL
            """.trimIndent(),
        )
    }
}

val MIGRATION_11_12 = object : Migration(11, 12) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE dynasty_roster_player ADD COLUMN loyaltyRating INTEGER NOT NULL DEFAULT 50")
        db.execSQL("ALTER TABLE dynasty_roster_player ADD COLUMN loyaltyPersonalityWire TEXT NOT NULL DEFAULT ''")
        db.execSQL("ALTER TABLE dynasty_roster_player ADD COLUMN promiseTypeWire TEXT")
        db.execSQL("ALTER TABLE dynasty_roster_player ADD COLUMN promiseStatusWire TEXT NOT NULL DEFAULT 'pending'")
        db.execSQL("ALTER TABLE dynasty_roster_player ADD COLUMN promiseLockedSeasonIndex INTEGER")
        db.execSQL("ALTER TABLE dynasty_roster_player ADD COLUMN promiseStayTopN INTEGER")
        db.execSQL(
            """
            UPDATE dynasty_roster_player
            SET promiseTypeWire = CASE WHEN isStarter = 1 THEN 'minutes_starter' ELSE 'minutes_rotation' END
            WHERE promiseTypeWire IS NULL
            """.trimIndent(),
        )
    }
}

val MIGRATION_10_11 = object : Migration(10, 11) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS dynasty_staff_member (
                staffId TEXT NOT NULL PRIMARY KEY,
                dynastyId TEXT NOT NULL,
                staffKindWire TEXT NOT NULL,
                roleWire TEXT NOT NULL,
                firstName TEXT NOT NULL,
                lastName TEXT NOT NULL,
                age INTEGER NOT NULL DEFAULT 35,
                recruitingRating INTEGER NOT NULL DEFAULT 0,
                developmentRating INTEGER NOT NULL DEFAULT 0,
                scoutingRating INTEGER NOT NULL DEFAULT 0,
                supportContributionRating INTEGER NOT NULL DEFAULT 0,
                yearsInRole INTEGER NOT NULL DEFAULT 1,
                yearsAtSchool INTEGER NOT NULL DEFAULT 1,
                contractYearsLeft INTEGER NOT NULL DEFAULT 2,
                contractLengthYears INTEGER NOT NULL DEFAULT 3
            )
            """.trimIndent(),
        )
        db.execSQL(
            """
            CREATE INDEX IF NOT EXISTS index_dynasty_staff_member_dynastyId
            ON dynasty_staff_member (dynastyId)
            """.trimIndent(),
        )
    }
}

val MIGRATION_9_10 = object : Migration(9, 10) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            "ALTER TABLE dynasty_roster_player ADD COLUMN archetypeWire TEXT NOT NULL DEFAULT ''",
        )
    }
}

val MIGRATION_8_9 = object : Migration(8, 9) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE dynasty_roster_player ADD COLUMN displayName TEXT NOT NULL DEFAULT ''")
        db.execSQL("ALTER TABLE dynasty_roster_player ADD COLUMN hometownCity TEXT NOT NULL DEFAULT ''")
        db.execSQL("ALTER TABLE dynasty_roster_player ADD COLUMN hometownState TEXT NOT NULL DEFAULT ''")
        db.execSQL("ALTER TABLE dynasty_roster_player ADD COLUMN heightInches INTEGER NOT NULL DEFAULT 76")
        db.execSQL("ALTER TABLE dynasty_roster_player ADD COLUMN weightLbs INTEGER NOT NULL DEFAULT 200")
        db.execSQL("ALTER TABLE dynasty_roster_player ADD COLUMN nilUsdPerYear INTEGER NOT NULL DEFAULT 0")
        db.execSQL("ALTER TABLE dynasty_roster_player ADD COLUMN ovr INTEGER NOT NULL DEFAULT 60")
        db.execSQL("ALTER TABLE dynasty_roster_player ADD COLUMN offRating INTEGER NOT NULL DEFAULT 60")
        db.execSQL("ALTER TABLE dynasty_roster_player ADD COLUMN defRating INTEGER NOT NULL DEFAULT 60")
        db.execSQL("ALTER TABLE dynasty_roster_player ADD COLUMN eligibilityYearsUsed INTEGER NOT NULL DEFAULT 0")
        db.execSQL(
            "ALTER TABLE dynasty_roster_player ADD COLUMN eligibilityYearsRemaining INTEGER NOT NULL DEFAULT 4",
        )
        db.execSQL("ALTER TABLE dynasty_roster_player ADD COLUMN medicalRedshirtSeason INTEGER NOT NULL DEFAULT 0")
        db.execSQL("ALTER TABLE dynasty_roster_player ADD COLUMN injuryActive INTEGER NOT NULL DEFAULT 0")
        db.execSQL("ALTER TABLE dynasty_roster_player ADD COLUMN injuryId TEXT")
        db.execSQL("ALTER TABLE dynasty_roster_player ADD COLUMN injuryWeeksRemaining INTEGER NOT NULL DEFAULT 0")
        db.execSQL("ALTER TABLE dynasty_roster_player ADD COLUMN injuryLabel TEXT")
    }
}

val MIGRATION_7_8 = object : Migration(7, 8) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            "ALTER TABLE dynasty_roster_player ADD COLUMN declaredForDraft INTEGER NOT NULL DEFAULT 0",
        )
        db.execSQL(
            "ALTER TABLE dynasty_roster_player ADD COLUMN draftStockPoints INTEGER NOT NULL DEFAULT 0",
        )
        db.execSQL(
            "ALTER TABLE dynasty_roster_player ADD COLUMN proDayCompleted INTEGER NOT NULL DEFAULT 0",
        )
    }
}

val MIGRATION_6_7 = object : Migration(6, 7) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            "ALTER TABLE dynasty_roster_player ADD COLUMN inTransferPortal INTEGER NOT NULL DEFAULT 0",
        )
    }
}

val MIGRATION_5_6 = object : Migration(5, 6) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS dynasty_roster_player (
                playerId TEXT NOT NULL PRIMARY KEY,
                dynastyId TEXT NOT NULL,
                positionWire TEXT NOT NULL,
                classYearWire TEXT NOT NULL,
                isStarter INTEGER NOT NULL,
                depthOrder INTEGER NOT NULL
            )
            """.trimIndent(),
        )
        db.execSQL(
            """
            CREATE INDEX IF NOT EXISTS index_dynasty_roster_player_dynastyId
            ON dynasty_roster_player (dynastyId)
            """.trimIndent(),
        )
    }
}

val MIGRATION_4_5 = object : Migration(4, 5) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS dynasty_coach (
                dynastyId TEXT NOT NULL PRIMARY KEY,
                userTeamId TEXT NOT NULL,
                firstName TEXT NOT NULL,
                lastName TEXT NOT NULL,
                nameSuffixWire TEXT,
                age INTEGER NOT NULL,
                almaMaterTeamId TEXT,
                skinToneId TEXT NOT NULL,
                hairColorId TEXT NOT NULL,
                hairStyleId TEXT NOT NULL,
                beardStyleId TEXT NOT NULL,
                avatarPresetId TEXT NOT NULL,
                archetypeWire TEXT NOT NULL,
                offensiveSchemeWire TEXT NOT NULL,
                defensiveSchemeWire TEXT NOT NULL,
                autoUpgrade INTEGER NOT NULL,
                unlimitedCoachAge INTEGER NOT NULL
            )
            """.trimIndent(),
        )
    }
}

val MIGRATION_3_4 = object : Migration(3, 4) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            ALTER TABLE dynasty_metadata ADD COLUMN balanceRulesetId TEXT NOT NULL
            DEFAULT '${DynastySchema.DEFAULT_BALANCE_RULESET_ID}'
            """.trimIndent(),
        )
        db.execSQL(
            """
            ALTER TABLE dynasty_metadata ADD COLUMN balanceSchemaVersion INTEGER NOT NULL
            DEFAULT ${DynastySchema.DEFAULT_BALANCE_SCHEMA_VERSION}
            """.trimIndent(),
        )
        db.execSQL(
            """
            ALTER TABLE dynasty_metadata ADD COLUMN simTuningId TEXT NOT NULL
            DEFAULT '${DynastySchema.DEFAULT_SIM_TUNING_ID}'
            """.trimIndent(),
        )
        db.execSQL(
            """
            ALTER TABLE dynasty_metadata ADD COLUMN simSchemaVersion INTEGER NOT NULL
            DEFAULT ${DynastySchema.DEFAULT_SIM_SCHEMA_VERSION}
            """.trimIndent(),
        )
    }
}

val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS dynasty_calendar (
                dynastyId TEXT NOT NULL PRIMARY KEY,
                seasonIndex INTEGER NOT NULL,
                phase TEXT NOT NULL,
                activeWeekIndex INTEGER NOT NULL,
                committedSeasonIndex INTEGER,
                committedPhase TEXT,
                committedWeekIndex INTEGER
            )
            """.trimIndent(),
        )
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS provisional_games (
                dynastyId TEXT NOT NULL,
                gameId TEXT NOT NULL,
                homeScore INTEGER NOT NULL,
                awayScore INTEGER NOT NULL,
                simGeneration INTEGER NOT NULL,
                PRIMARY KEY(dynastyId, gameId)
            )
            """.trimIndent(),
        )
        db.execSQL(
            "CREATE INDEX IF NOT EXISTS index_provisional_games_dynastyId ON provisional_games(dynastyId)",
        )
    }
}

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS dynasty_metadata (
                dynastyId TEXT NOT NULL PRIMARY KEY,
                seasonIndex INTEGER NOT NULL,
                phase TEXT NOT NULL,
                packId TEXT NOT NULL,
                dynastySchemaVersion INTEGER NOT NULL
            )
            """.trimIndent(),
        )
    }
}

/** Room user version after the latest migration in this chain. */
const val DYNASTY_DB_VERSION: Int = 22

/** Ordered migration chain registered on [DynastyDatabase]; used in CI migration tests. */
val ALL_DYNASTY_MIGRATIONS: Array<Migration> =
    arrayOf(
        MIGRATION_1_2,
        MIGRATION_2_3,
        MIGRATION_3_4,
        MIGRATION_4_5,
        MIGRATION_5_6,
        MIGRATION_6_7,
        MIGRATION_7_8,
        MIGRATION_8_9,
        MIGRATION_9_10,
        MIGRATION_10_11,
        MIGRATION_11_12,
        MIGRATION_12_13,
        MIGRATION_13_14,
        MIGRATION_14_15,
        MIGRATION_15_16,
        MIGRATION_16_17,
        MIGRATION_17_18,
        MIGRATION_18_19,
        MIGRATION_19_20,
        MIGRATION_20_21,
        MIGRATION_21_22,
    )
