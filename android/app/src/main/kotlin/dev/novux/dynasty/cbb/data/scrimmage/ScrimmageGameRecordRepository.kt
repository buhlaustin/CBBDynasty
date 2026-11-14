package dev.novux.dynasty.cbb.data.scrimmage

import dev.novux.dynasty.cbb.core.scrimmage.ScrimmageGameRecord
import dev.novux.dynasty.cbb.core.sim.GameSimResult

/** In-session scrimmage box persistence per dynasty (MEGA-SIM-133). */
class ScrimmageGameRecordRepository {
    private val byDynasty = mutableMapOf<String, ScrimmageGameRecord>()

    fun save(dynastyId: String, opponentLabel: String, result: GameSimResult) {
        byDynasty[dynastyId] = ScrimmageGameRecord.fromSim(dynastyId, opponentLabel, result)
    }

    fun lastForDynasty(dynastyId: String): ScrimmageGameRecord? = byDynasty[dynastyId]
}
