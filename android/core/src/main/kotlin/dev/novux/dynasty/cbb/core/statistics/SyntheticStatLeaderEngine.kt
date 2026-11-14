package dev.novux.dynasty.cbb.core.statistics

import dev.novux.dynasty.cbb.core.pack.TeamDisplayOverlay
import dev.novux.dynasty.cbb.core.player.DynastyPlayer
import dev.novux.dynasty.cbb.core.player.PlayerEligibility
import dev.novux.dynasty.cbb.core.player.PlayerIdentity
import dev.novux.dynasty.cbb.core.player.PlayerInjuryState
import dev.novux.dynasty.cbb.core.player.PlayerRatings
import dev.novux.dynasty.cbb.core.player.PlayerRosterRole
import dev.novux.dynasty.cbb.core.player.archetype.PlayerArchetypeAssigner
import dev.novux.dynasty.cbb.core.player.stats.PlayerCountingStats
import dev.novux.dynasty.cbb.core.player.stats.PlayerStatsEngine
import dev.novux.dynasty.cbb.core.player.stats.PlayerStatsSynth
import dev.novux.dynasty.cbb.core.roster.PlayerClassYear
import dev.novux.dynasty.cbb.core.roster.PlayerPosition
import kotlin.math.abs

/** One deterministic star per non-user team until full league box scores persist. */
object SyntheticStatLeaderEngine {
    fun starPlayer(dynastyId: String, team: TeamDisplayOverlay): DynastyPlayer {
        val bump = abs(team.teamId.hashCode() % 14)
        val ovr = (team.programPrestige + 6 + bump).coerceIn(58, 94)
        val off = (ovr + (bump % 5) - 2).coerceIn(50, 99)
        val def = (ovr - (bump % 7) + 1).coerceIn(45, 96)
        val ratings = PlayerRatings.headline(ovr, off, def)
        return DynastyPlayer(
            playerId = "stat-star-${team.teamId}",
            dynastyId = dynastyId,
            appearance = dev.novux.dynasty.cbb.core.player.appearance.PlayerAppearance.fromPlayerId(team.teamId),
            identity = PlayerIdentity(
                displayName = syntheticName(team),
                hometownCity = team.homeCity,
                hometownState = team.homeState,
                heightInches = 78,
                weightLbs = 210,
            ),
            position = PlayerPosition.SmallForward,
            classYear = PlayerClassYear.Junior,
            ratings = ratings,
            archetype = PlayerArchetypeAssigner.assign(PlayerPosition.SmallForward, ratings),
            eligibility = PlayerEligibility(yearsUsed = 2, yearsRemaining = 2),
            injury = PlayerInjuryState.Healthy,
            role = PlayerRosterRole(
                isStarter = true,
                depthOrder = 0,
                inTransferPortal = false,
                declaredForDraft = false,
                draftStockPoints = 0,
                proDayCompleted = false,
            ),
        )
    }

    fun seasonTotals(player: DynastyPlayer, gamesPlayed: Int): PlayerCountingStats =
        PlayerStatsSynth.totalsForSeason(player, gamesPlayed)

    fun rates(totals: PlayerCountingStats, gamesPlayed: Int) =
        PlayerStatsEngine.rateStats(totals, gamesPlayed)

    private fun syntheticName(team: TeamDisplayOverlay): String {
        val first = FIRST_NAMES[abs(team.teamId.hashCode()) % FIRST_NAMES.size]
        val lastInitial = team.shortName.firstOrNull()?.uppercaseChar() ?: 'X'
        return "$first $lastInitial."
    }

    private val FIRST_NAMES = listOf(
        "Jordan",
        "Elijah",
        "Marcus",
        "Cameron",
        "Devin",
        "Tyler",
        "Malik",
        "Andre",
        "Kai",
        "Noah",
    )
}
