package dev.novux.dynasty.cbb.database.calendar

import androidx.room.Entity
import androidx.room.Index

@Entity(
    tableName = "provisional_games",
    primaryKeys = ["dynastyId", "gameId"],
    indices = [Index("dynastyId")],
)
data class ProvisionalGameEntity(
    val dynastyId: String,
    val gameId: String,
    val homeScore: Int,
    val awayScore: Int,
    val simGeneration: Int,
    val possessionEngine: Boolean = false,
    val boxArchiveJson: String? = null,
)
