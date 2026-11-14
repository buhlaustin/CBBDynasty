package dev.novux.dynasty.cbb.core.league

import dev.novux.dynasty.cbb.core.roster.PlayerClassYear
import dev.novux.dynasty.cbb.core.roster.PlayerPosition
import dev.novux.dynasty.cbb.core.roster.RosterPlayer

/** Placeholder roster for AI injury bank rolls in league batch ([MEGA-SIM-163]). */
object AiLeagueSyntheticRoster {
    fun playersForTeam(teamId: String, prestige: Int): List<RosterPlayer> {
        val baseOvr = (prestige + 4).coerceIn(58, 92)
        val slots = listOf(
            PlayerPosition.PointGuard,
            PlayerPosition.ShootingGuard,
            PlayerPosition.SmallForward,
            PlayerPosition.PowerForward,
            PlayerPosition.Center,
            PlayerPosition.ShootingGuard,
            PlayerPosition.SmallForward,
            PlayerPosition.PowerForward,
            PlayerPosition.Center,
            PlayerPosition.PointGuard,
        )
        return slots.mapIndexed { index, position ->
            val starter = index < 5
            RosterPlayer(
                playerId = "$teamId-ai-$index",
                dynastyId = "league-ai",
                position = position,
                classYear = PlayerClassYear.Sophomore,
                isStarter = starter,
                depthOrder = index + 1,
                inTransferPortal = false,
                displayName = "${teamId.take(8)} #$index",
                ovr = baseOvr - index,
                off = baseOvr - index,
                def = baseOvr - index - 1,
                starterSlotWire = if (starter) position.wire else null,
            )
        }
    }

    fun minutesByPlayer(players: List<RosterPlayer>, possessions: Int): Map<String, Double> {
        val scale = possessions.toDouble() / 70.0.coerceAtLeast(1.0)
        return players.associate { player ->
            val base = if (player.isStarter) 28.0 else 6.0
            player.playerId to base * scale
        }
    }
}
