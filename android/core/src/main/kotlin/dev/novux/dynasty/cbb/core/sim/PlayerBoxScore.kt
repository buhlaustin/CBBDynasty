package dev.novux.dynasty.cbb.core.sim

/**
 * Per-player box line — [planning/simulation-engine.md#stats].
 */
data class PlayerBoxScore(
    val playerId: String,
    val minutesSeconds: Int = 0,
    val points: Int = 0,
    val fieldGoalsMade: Int = 0,
    val fieldGoalsAttempted: Int = 0,
    val threePointsMade: Int = 0,
    val threePointsAttempted: Int = 0,
    val freeThrowsMade: Int = 0,
    val freeThrowsAttempted: Int = 0,
    val offensiveRebounds: Int = 0,
    val defensiveRebounds: Int = 0,
    val assists: Int = 0,
    val steals: Int = 0,
    val blocks: Int = 0,
    val turnovers: Int = 0,
    val personalFouls: Int = 0,
) {
    val rebounds: Int get() = offensiveRebounds + defensiveRebounds

    fun minutesPlayed(): Int = if (minutesSeconds <= 0) 0 else (minutesSeconds + 59) / 60
}

class PlayerBoxScoreLedger(
    playerIds: Collection<String> = emptyList(),
    initial: Map<String, PlayerBoxScore> = emptyMap(),
) {
    private val lines = mutableMapOf<String, PlayerBoxScore>()

    init {
        playerIds.forEach { id -> lines.putIfAbsent(id, PlayerBoxScore(id)) }
        initial.forEach { (id, line) -> lines[id] = line }
    }

    fun snapshot(): List<PlayerBoxScore> = lines.values.sortedBy { it.playerId }

    fun snapshotMap(): Map<String, PlayerBoxScore> = lines.toMap()

    private fun mutate(id: String, block: (PlayerBoxScore) -> PlayerBoxScore) {
        val current = lines[id] ?: PlayerBoxScore(id)
        lines[id] = block(current)
    }

    fun setMinutesSeconds(id: String, seconds: Int) {
        mutate(id) { it.copy(minutesSeconds = seconds) }
    }

    fun addMinutesSeconds(id: String, seconds: Int) {
        if (seconds == 0) return
        mutate(id) { it.copy(minutesSeconds = (it.minutesSeconds + seconds).coerceAtLeast(0)) }
    }

    fun setPersonalFouls(id: String, fouls: Int) {
        mutate(id) { it.copy(personalFouls = fouls) }
    }

    fun addTurnover(id: String) {
        mutate(id) { it.copy(turnovers = it.turnovers + 1) }
    }

    fun addSteal(id: String) {
        mutate(id) { it.copy(steals = it.steals + 1) }
    }

    fun addBlock(id: String) {
        mutate(id) { it.copy(blocks = it.blocks + 1) }
    }

    fun addAssist(id: String) {
        mutate(id) { it.copy(assists = it.assists + 1) }
    }

    fun addOffensiveRebound(id: String) {
        mutate(id) { it.copy(offensiveRebounds = it.offensiveRebounds + 1) }
    }

    fun addDefensiveRebound(id: String) {
        mutate(id) { it.copy(defensiveRebounds = it.defensiveRebounds + 1) }
    }

    fun addFieldGoalAttempt(id: String, three: Boolean) {
        mutate(id) {
            it.copy(
                fieldGoalsAttempted = it.fieldGoalsAttempted + 1,
                threePointsAttempted = it.threePointsAttempted + if (three) 1 else 0,
            )
        }
    }

    fun addFieldGoalMake(id: String, points: Int, three: Boolean) {
        mutate(id) {
            it.copy(
                fieldGoalsMade = it.fieldGoalsMade + 1,
                points = it.points + points,
                threePointsMade = it.threePointsMade + if (three) 1 else 0,
            )
        }
    }

    fun addFreeThrows(id: String, makes: Int, attempts: Int) {
        mutate(id) {
            it.copy(
                freeThrowsMade = it.freeThrowsMade + makes,
                freeThrowsAttempted = it.freeThrowsAttempted + attempts,
                points = it.points + makes,
            )
        }
    }

    fun replace(id: String, line: PlayerBoxScore) {
        lines[id] = line
    }
}
