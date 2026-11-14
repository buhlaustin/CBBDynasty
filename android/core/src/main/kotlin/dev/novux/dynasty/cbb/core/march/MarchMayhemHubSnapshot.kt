package dev.novux.dynasty.cbb.core.march

import dev.novux.dynasty.cbb.core.brackets.NationalBracketSnapshot
import dev.novux.dynasty.cbb.core.brackets.NcbaMainBracketSnapshot
import dev.novux.dynasty.cbb.core.brackets.NcbaOpeningRoundSnapshot
import dev.novux.dynasty.cbb.core.league.LeagueGameResult
import dev.novux.dynasty.cbb.core.league.LeagueStandingsState
import dev.novux.dynasty.cbb.core.pack.IdentityPackIndex
import dev.novux.dynasty.cbb.core.selection.SelectionSundaySnapshot

data class MarchMayhemTeamRow(
    val teamId: String,
    val displayName: String,
    val seedLabel: String,
    val regionLabel: String? = null,
    val isUserTeam: Boolean = false,
)

data class MarchMayhemGameRow(
    val postseasonWeek: Int,
    val homeTeamId: String,
    val awayTeamId: String,
    val homeDisplayName: String,
    val awayDisplayName: String,
    val homeScore: Int,
    val awayScore: Int,
    val homeSeedLabel: String?,
    val awaySeedLabel: String?,
)

data class MarchMayhemNcbaPanel(
    val openingSimmed: Boolean,
    val openingAdvancers: List<MarchMayhemTeamRow>,
    val mainBracketSimmed: Boolean,
    val field: List<MarchMayhemTeamRow>,
    val regionalChampions: List<MarchMayhemTeamRow>,
    val champion: MarchMayhemTeamRow?,
    val games: List<MarchMayhemGameRow>,
)

data class MarchMayhemInvitationalPanel(
    val tournamentShortLabel: String,
    val field: List<MarchMayhemTeamRow>,
    val champion: MarchMayhemTeamRow?,
    val games: List<MarchMayhemGameRow>,
    val completed: Boolean,
)

/** Mini progress rail for one national event on the Overview tab ([`postseason-frame.md`](postseason-frame.md)). */
data class MarchMayhemProgressRail(
    val eventLabel: String,
    val fieldSize: Int,
    val gamesPlayed: Int,
    val championDisplayName: String?,
    val userTeamAlive: Boolean,
    val userInField: Boolean,
    val openingRoundActive: Boolean = false,
    val openingAdvancerCount: Int = 0,
)

data class MarchMayhemOverviewPanel(
    val selectionFieldCount: Int,
    val rails: List<MarchMayhemProgressRail>,
    val todaysGames: List<MarchMayhemGameRow>,
)

data class MarchMayhemHubSnapshot(
    val seasonIndex: Int,
    val selectionPublished: Boolean,
    val userTeamId: String,
    val overview: MarchMayhemOverviewPanel,
    val ncba: MarchMayhemNcbaPanel,
    val nci: MarchMayhemInvitationalPanel,
    val heritage: MarchMayhemInvitationalPanel,
)

object MarchMayhemHubSnapshotFactory {
    fun build(
        packIndex: IdentityPackIndex,
        standings: LeagueStandingsState?,
        userTeamId: String,
    ): MarchMayhemHubSnapshot {
        val selection = standings?.selectionSunday
        val seasonIndex = standings?.seasonIndex ?: 1
        if (selection == null) {
            return emptySnapshot(seasonIndex, userTeamId)
        }
        val seedByTeam = selection.ncbaField.associate { it.teamId to it.ncbaSeed }
        val nciSeed = selection.nciField.associate { it.teamId to it.seed }
        val hcSeed = selection.heritageField.associate { it.teamId to it.seed }
        val opening = standings.ncbaOpeningRound
        val main = standings.ncbaMainBracket
        val nci = standings.nciBracket
        val heritage = standings.heritageBracket
        val ncbaPanel = buildNcba(packIndex, selection, opening, main, userTeamId, seedByTeam)
        val nciPanel = buildInvitational(
            packIndex,
            nci,
            selection.nciField,
            userTeamId,
            "NCI",
            nciSeed,
        )
        val heritagePanel = buildInvitational(
            packIndex,
            heritage,
            selection.heritageField,
            userTeamId,
            "HC",
            hcSeed,
        )
        return MarchMayhemHubSnapshot(
            seasonIndex = seasonIndex,
            selectionPublished = true,
            userTeamId = userTeamId,
            overview = buildOverview(
                selection,
                userTeamId,
                ncbaPanel,
                nciPanel,
                heritagePanel,
            ),
            ncba = ncbaPanel,
            nci = nciPanel,
            heritage = heritagePanel,
        )
    }

    private fun emptySnapshot(seasonIndex: Int, userTeamId: String) = MarchMayhemHubSnapshot(
        seasonIndex = seasonIndex,
        selectionPublished = false,
        userTeamId = userTeamId,
        overview = MarchMayhemOverviewPanel(0, emptyList(), emptyList()),
        ncba = MarchMayhemNcbaPanel(false, emptyList(), false, emptyList(), emptyList(), null, emptyList()),
        nci = MarchMayhemInvitationalPanel("NCI", emptyList(), null, emptyList(), false),
        heritage = MarchMayhemInvitationalPanel("HC", emptyList(), null, emptyList(), false),
    )

    private fun buildOverview(
        selection: SelectionSundaySnapshot,
        userTeamId: String,
        ncba: MarchMayhemNcbaPanel,
        nci: MarchMayhemInvitationalPanel,
        heritage: MarchMayhemInvitationalPanel,
    ): MarchMayhemOverviewPanel {
        val allGames = ncba.games + nci.games + heritage.games
        val latestWeek = allGames.maxOfOrNull { it.postseasonWeek }
        val todaysGames = latestWeek?.let { week ->
            allGames.filter { it.postseasonWeek == week }.sortedBy { it.homeDisplayName }
        }.orEmpty()
        val fieldCount =
            selection.ncbaField.size + selection.nciField.size + selection.heritageField.size
        val rails = listOf(
            progressRail(
                eventLabel = "NCBA",
                panelField = ncba.field,
                games = ncba.games,
                champion = ncba.champion,
                userTeamId = userTeamId,
                openingRoundActive = ncba.openingSimmed && !ncba.mainBracketSimmed && ncba.openingAdvancers.isNotEmpty(),
                openingAdvancerCount = ncba.openingAdvancers.size,
            ),
            progressRail(
                eventLabel = nci.tournamentShortLabel,
                panelField = nci.field,
                games = nci.games,
                champion = nci.champion,
                userTeamId = userTeamId,
            ),
            progressRail(
                eventLabel = heritage.tournamentShortLabel,
                panelField = heritage.field,
                games = heritage.games,
                champion = heritage.champion,
                userTeamId = userTeamId,
            ),
        )
        return MarchMayhemOverviewPanel(
            selectionFieldCount = fieldCount,
            rails = rails,
            todaysGames = todaysGames,
        )
    }

    private fun progressRail(
        eventLabel: String,
        panelField: List<MarchMayhemTeamRow>,
        games: List<MarchMayhemGameRow>,
        champion: MarchMayhemTeamRow?,
        userTeamId: String,
        openingRoundActive: Boolean = false,
        openingAdvancerCount: Int = 0,
    ): MarchMayhemProgressRail {
        val fieldSize = panelField.size.coerceAtLeast(
            games.flatMap { listOf(it.homeTeamId, it.awayTeamId) }.distinct().size,
        )
        val userInField = panelField.any { it.teamId == userTeamId }
        return MarchMayhemProgressRail(
            eventLabel = eventLabel,
            fieldSize = fieldSize,
            gamesPlayed = games.size,
            championDisplayName = champion?.displayName,
            userTeamAlive = isUserTeamAlive(userTeamId, panelField, games, champion),
            userInField = userInField,
            openingRoundActive = openingRoundActive,
            openingAdvancerCount = openingAdvancerCount,
        )
    }

    private fun isUserTeamAlive(
        userTeamId: String,
        field: List<MarchMayhemTeamRow>,
        games: List<MarchMayhemGameRow>,
        champion: MarchMayhemTeamRow?,
    ): Boolean {
        if (field.none { it.teamId == userTeamId }) return false
        if (champion != null) return champion.teamId == userTeamId
        val eliminated = games.any { game ->
            val userHome = game.homeTeamId == userTeamId
            val userAway = game.awayTeamId == userTeamId
            when {
                userHome -> game.homeScore < game.awayScore
                userAway -> game.awayScore < game.homeScore
                else -> false
            }
        }
        return !eliminated
    }

    private fun buildNcba(
        index: IdentityPackIndex,
        selection: SelectionSundaySnapshot,
        opening: NcbaOpeningRoundSnapshot?,
        main: NcbaMainBracketSnapshot?,
        userTeamId: String,
        seedByTeam: Map<String, Int>,
    ): MarchMayhemNcbaPanel {
        val openingAdvancers = opening
            ?.takeIf { it.completed }
            ?.winnerTeamIds
            ?.map { teamId ->
                teamRow(index, teamId, userTeamId, "NCBA #${seedByTeam[teamId]}", null)
            }
            .orEmpty()
        val field = main?.entrants?.map { entrant ->
            teamRow(
                index,
                entrant.teamId,
                userTeamId,
                "NCBA #${entrant.ncbaSeed}",
                entrant.regionId.name,
            )
        }.orEmpty()
        val regionalChampions = main?.regionalChampions.orEmpty().map { (region, teamId) ->
            teamRow(index, teamId, userTeamId, "NCBA #${seedByTeam[teamId]}", region.name)
        }
        val champion = main?.championTeamId?.let { teamId ->
            teamRow(index, teamId, userTeamId, "NCBA #${seedByTeam[teamId]}", null)
        }
        val games = buildGameRows(
            index,
            (opening?.gameResults.orEmpty() + main?.gameResults.orEmpty()),
            seedByTeam.mapValues { "NCBA #${it.value}" },
        )
        return MarchMayhemNcbaPanel(
            openingSimmed = opening?.completed == true,
            openingAdvancers = openingAdvancers,
            mainBracketSimmed = main?.completed == true,
            field = field,
            regionalChampions = regionalChampions,
            champion = champion,
            games = games,
        )
    }

    private fun buildInvitational(
        index: IdentityPackIndex,
        bracket: NationalBracketSnapshot?,
        invites: List<dev.novux.dynasty.cbb.core.selection.NationalTournamentInvite>,
        userTeamId: String,
        label: String,
        seedByTeam: Map<String, Int>,
    ): MarchMayhemInvitationalPanel {
        val field = invites.map { invite ->
            teamRow(index, invite.teamId, userTeamId, "$label #${invite.seed}", null)
        }
        val champion = bracket?.championTeamId?.let { teamId ->
            teamRow(index, teamId, userTeamId, "$label #${seedByTeam[teamId]}", null)
        }
        val games = buildGameRows(
            index,
            bracket?.gameResults.orEmpty(),
            seedByTeam.mapValues { "$label #${it.value}" },
        )
        return MarchMayhemInvitationalPanel(
            tournamentShortLabel = label,
            field = field,
            champion = champion,
            games = games,
            completed = bracket?.completed == true,
        )
    }

    private fun teamRow(
        index: IdentityPackIndex,
        teamId: String,
        userTeamId: String,
        seedLabel: String,
        regionLabel: String?,
    ) = MarchMayhemTeamRow(
        teamId = teamId,
        displayName = index.team(teamId)?.displayName ?: teamId,
        seedLabel = seedLabel,
        regionLabel = regionLabel,
        isUserTeam = teamId == userTeamId,
    )

    private fun buildGameRows(
        index: IdentityPackIndex,
        results: List<LeagueGameResult>,
        seedLabels: Map<String, String>,
    ): List<MarchMayhemGameRow> =
        results.map { game ->
            MarchMayhemGameRow(
                postseasonWeek = game.rsWeekIndex,
                homeTeamId = game.homeTeamId,
                awayTeamId = game.awayTeamId,
                homeDisplayName = index.team(game.homeTeamId)?.displayName ?: game.homeTeamId,
                awayDisplayName = index.team(game.awayTeamId)?.displayName ?: game.awayTeamId,
                homeScore = game.homeScore,
                awayScore = game.awayScore,
                homeSeedLabel = seedLabels[game.homeTeamId],
                awaySeedLabel = seedLabels[game.awayTeamId],
            )
        }
}
