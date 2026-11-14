package dev.novux.dynasty.cbb.core.schedule

import dev.novux.dynasty.cbb.core.pack.ConferenceDisplayOverlay
import dev.novux.dynasty.cbb.core.pack.TeamDisplayOverlay
import dev.novux.dynasty.cbb.core.preseason.tournament.EarlySeasonEventCatalog
import dev.novux.dynasty.cbb.core.preseason.tournament.RegularSeasonSlotBudget
import dev.novux.dynasty.cbb.core.preseason.tournament.TournamentInviteResolution
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PreseasonScheduleSolverTest {
  private val user = TeamDisplayOverlay(
        teamId = "user",
        conferenceId = "conf-a",
        displayName = "User U",
        shortName = "UU",
        homeCity = "Columbus",
        homeState = "OH",
        primaryColorHex = "#000000",
        secondaryColorHex = "#FFFFFF",
        logoUri = null,
        postseasonEligible = true,
        programPrestige = 50,
    )

    private val conference = ConferenceDisplayOverlay(
        conferenceId = "conf-a",
        displayName = "Test Conference",
        leagueGameCount = 18,
        tournamentTemplateId = "tpl",
    )

    private val confOpponents = List(5) { i ->
        user.copy(teamId = "c$i", displayName = "Conf foe $i")
    }

    private val nonConf = List(10) { i ->
        user.copy(teamId = "nc$i", conferenceId = "conf-b", displayName = "Non-conf $i")
    }

    @Test
    fun solverRespectsTwentyEightBuilderSlotsWithFourGameEvent() {
        val budget = RegularSeasonSlotBudget.fromResolution(
            TournamentInviteResolution.Accepted(EarlySeasonEventCatalog.islandClassic),
        )
        val plan = PreseasonScheduleSolver.solve(
            userTeam = user,
            conference = conference,
            conferenceOpponents = confOpponents,
            nonConferencePool = nonConf,
            budget = budget,
            tournamentResolution = TournamentInviteResolution.Accepted(EarlySeasonEventCatalog.islandClassic),
            exhibitionCount = 2,
            seed = 7,
        )
        assertEquals(4, plan.earlyEventGameCount)
        assertEquals(18, plan.conferenceGameCount)
        assertEquals(10, plan.nonConferenceGameCount)
        assertEquals(32, plan.totalRsGames)
        assertEquals(2, plan.exhibitions.size)
        assertTrue(plan.isValidFor(budget))
    }

    @Test
    fun noDoubleBookedRegularSeasonWeeks() {
        val budget = RegularSeasonSlotBudget(eventRsGames = 0, builderSlots = 32)
        val plan = PreseasonScheduleSolver.solve(
            userTeam = user,
            conference = conference,
            conferenceOpponents = confOpponents,
            nonConferencePool = nonConf,
            budget = budget,
            tournamentResolution = TournamentInviteResolution.DeclinedAll,
            exhibitionCount = 0,
            seed = 11,
        )
        assertTrue(plan.isValidFor(budget))
    }

    @Test
    fun allowsDoubleConferenceOpponent() {
        val plan = PreseasonScheduleSolver.solve(
            userTeam = user,
            conference = conference.copy(leagueGameCount = 10),
            conferenceOpponents = confOpponents.take(3),
            nonConferencePool = nonConf,
            budget = RegularSeasonSlotBudget(eventRsGames = 0, builderSlots = 32),
            tournamentResolution = TournamentInviteResolution.DeclinedAll,
            exhibitionCount = 0,
            seed = 3,
        )
        val confIds = plan.regularSeason.filter { it.kind == ScheduleGameKind.Conference }.map { it.opponentTeamId }
        assertTrue(confIds.groupingBy { it }.eachCount().values.any { it >= 2 })
    }
}
