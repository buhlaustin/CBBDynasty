package dev.novux.dynasty.cbb.core.scrimmage

import dev.novux.dynasty.cbb.core.calendar.checkpoint.ProvisionalGameBoxArchive
import dev.novux.dynasty.cbb.core.sim.GameSimResult
import kotlinx.serialization.Serializable

/** Last scrimmage box for dynasty replay ([MEGA-SIM-133]). */
@Serializable
data class ScrimmageGameRecord(
    val dynastyId: String,
    val opponentLabel: String,
    val homeScore: Int,
    val awayScore: Int,
    val archive: ProvisionalGameBoxArchive,
    val recordedAtEpochMs: Long = System.currentTimeMillis(),
) {
    companion object {
        fun fromSim(
            dynastyId: String,
            opponentLabel: String,
            result: GameSimResult,
        ): ScrimmageGameRecord = ScrimmageGameRecord(
            dynastyId = dynastyId,
            opponentLabel = opponentLabel,
            homeScore = result.homeScore,
            awayScore = result.awayScore,
            archive = ProvisionalGameBoxArchive.fromResult(result, userTeamIsHome = true),
        )
    }
}
