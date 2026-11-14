package dev.novux.dynasty.cbb.core.carousel

import dev.novux.dynasty.cbb.core.coach.card.HeadCoachCard
import dev.novux.dynasty.cbb.core.coach.security.CoachSecuritySnapshot
import dev.novux.dynasty.cbb.core.league.LeagueStandingsState
import dev.novux.dynasty.cbb.core.pack.TeamDisplayOverlay
import kotlin.random.Random

/** One head coach seat entering offseason carousel background sim. */
data class CarouselLeagueCoachRow(
    val teamId: String,
    val programPrestige: Int,
    val wins: Int,
    val losses: Int,
    val madePostseason: Boolean,
    val deepPostseasonRun: Boolean,
    val coachAge: Int,
    val tenureYears: Int,
    val portalDepartures: Int,
    val contractYearsLeft: Int,
    val jobSecurityScore: Int,
    val isUserProgram: Boolean,
)

data class CarouselSimContext(
    val dynastyId: String,
    val seasonIndex: Int,
    val userTeamId: String,
    val leagueCoaches: List<CarouselLeagueCoachRow>,
    val userHeadCoach: HeadCoachCard? = null,
    val userSecurity: CoachSecuritySnapshot? = null,
    val userUnemployed: Boolean = false,
    val backgroundSimSeedSalt: Int = 0,
) {
    fun deterministicRandom(): Random =
        Random(dynastyId.hashCode() + seasonIndex * 991 + backgroundSimSeedSalt)
}

object CarouselSimContextBootstrap {
    fun fromStandingsAndPack(
        dynastyId: String,
        seasonIndex: Int,
        userTeamId: String,
        standings: LeagueStandingsState?,
        teams: List<TeamDisplayOverlay>,
        userHeadCoach: HeadCoachCard? = null,
        userSecurity: CoachSecuritySnapshot? = null,
        userUnemployed: Boolean = false,
        priorSeasonIndex: Int = seasonIndex - 1,
    ): CarouselSimContext {
        val standingsForSeason = standings?.takeIf { it.seasonIndex == priorSeasonIndex }
        val rows = teams.map { team ->
            val record = standingsForSeason?.recordFor(team.teamId)
            val wins = record?.wins ?: syntheticWins(team, dynastyId, priorSeasonIndex)
            val losses = record?.losses ?: (32 - wins).coerceAtLeast(0)
            val madePostseason = standingsForSeason?.selectionSunday
                ?.ncbaField
                ?.any { it.teamId == team.teamId } == true
            val deepRun = madePostseason && wins >= 24
            val coachSeed = (dynastyId.hashCode() + team.teamId.hashCode() + priorSeasonIndex * 17)
            CarouselLeagueCoachRow(
                teamId = team.teamId,
                programPrestige = team.programPrestige,
                wins = wins,
                losses = losses,
                madePostseason = madePostseason,
                deepPostseasonRun = deepRun,
                coachAge = 38 + coachSeed.mod(22),
                tenureYears = 1 + coachSeed.mod(11),
                portalDepartures = coachSeed.mod(6),
                contractYearsLeft = 1 + coachSeed.mod(4),
                jobSecurityScore = (55 + wins - team.programPrestige / 3).coerceIn(10, 95),
                isUserProgram = team.teamId == userTeamId,
            )
        }
        return CarouselSimContext(
            dynastyId = dynastyId,
            seasonIndex = seasonIndex,
            userTeamId = userTeamId,
            leagueCoaches = rows,
            userHeadCoach = userHeadCoach,
            userSecurity = userSecurity,
            userUnemployed = userUnemployed,
        )
    }

    /** Deterministic placeholder when standings not yet hydrated. */
    fun minimalLeague(
        dynastyId: String,
        seasonIndex: Int,
        userTeamId: String = "user-team",
        teamCount: Int = 32,
    ): CarouselSimContext {
        val teams = (0 until teamCount).map { i ->
            val prestige = 45 + (i * 3).mod(50)
            TeamDisplayOverlay(
                teamId = if (i == 0) userTeamId else "ai-$i",
                conferenceId = "conf-${i % 8}",
                displayName = "Program $i",
                shortName = "P$i",
                homeCity = "City",
                homeState = "ST",
                primaryColorHex = "#000000",
                secondaryColorHex = "#FFFFFF",
                logoUri = null,
                postseasonEligible = true,
                programPrestige = prestige,
            )
        }
        return fromStandingsAndPack(
            dynastyId = dynastyId,
            seasonIndex = seasonIndex,
            userTeamId = userTeamId,
            standings = null,
            teams = teams,
        )
    }

    private fun syntheticWins(team: TeamDisplayOverlay, dynastyId: String, seasonIndex: Int): Int {
        val roll = (dynastyId.hashCode() + team.teamId.hashCode() + seasonIndex * 31).mod(100)
        val base = team.programPrestige / 4
        return (base + roll.mod(12) - 4).coerceIn(8, 28)
    }
}
