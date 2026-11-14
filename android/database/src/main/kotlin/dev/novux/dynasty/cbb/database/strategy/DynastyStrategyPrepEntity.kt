package dev.novux.dynasty.cbb.database.strategy

import androidx.room.Entity
import androidx.room.PrimaryKey

/** Per-dynasty game prep scheme override ([MEGA-SIM-151]); coach row remains season default. */
@Entity(tableName = "dynasty_strategy_prep")
data class DynastyStrategyPrepEntity(
    @PrimaryKey val dynastyId: String,
    val offensiveSchemeWire: String,
    val defensiveSchemeWire: String,
)
