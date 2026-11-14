package dev.novux.dynasty.cbb.core.schedule

import dev.novux.dynasty.cbb.core.pack.ConferenceDisplayOverlay
import dev.novux.dynasty.cbb.core.pack.TeamDisplayOverlay
import dev.novux.dynasty.cbb.core.preseason.tournament.RegularSeasonSlotBudget
import dev.novux.dynasty.cbb.core.preseason.tournament.TournamentInviteResolution
import kotlin.random.Random

object PreseasonScheduleSolver {
    fun solve(
        userTeam: TeamDisplayOverlay,
        conference: ConferenceDisplayOverlay,
        conferenceOpponents: List<TeamDisplayOverlay>,
        nonConferencePool: List<TeamDisplayOverlay>,
        budget: RegularSeasonSlotBudget,
        tournamentResolution: TournamentInviteResolution,
        exhibitionCount: Int,
        seed: Long,
        exhibitionStubs: List<ScheduledGameStub>? = null,
    ): PreseasonSchedulePlan {
        val random = Random(seed)
        val confTarget = conference.leagueGameCount.coerceAtMost(budget.builderSlots)
        val eventGames = buildEarlyEventGames(tournamentResolution)
        val eventWeeks = eventGames.mapNotNull { it.rsWeekIndex }.toSet()
        val confGames = buildConferenceGames(userTeam, conferenceOpponents, confTarget, eventWeeks)
        val confWeeks = confGames.mapNotNull { it.rsWeekIndex }.toSet()
        val builderRemaining = (budget.builderSlots - confGames.size).coerceAtLeast(0)
        val nonConfGames = buildNonConferenceGames(
            userTeam,
            nonConferencePool,
            builderRemaining,
            random,
            eventWeeks + confWeeks,
        )
        val exhibitions =
            exhibitionStubs?.takeIf { it.size == exhibitionCount.coerceIn(0, PreseasonScheduleConstants.MAX_EXHIBITION_GAMES) }
                ?: buildExhibitions(nonConferencePool, exhibitionCount, random)
        val regularSeason = eventGames + confGames + nonConfGames
        return PreseasonSchedulePlan(
            exhibitions = exhibitions,
            regularSeason = regularSeason,
            conferenceGameCount = confGames.size,
            nonConferenceGameCount = nonConfGames.size,
            earlyEventGameCount = eventGames.size,
        )
    }

    private fun buildConferenceGames(
        userTeam: TeamDisplayOverlay,
        opponents: List<TeamDisplayOverlay>,
        count: Int,
        reservedWeeks: Set<Int>,
    ): List<ScheduledGameStub> {
        if (opponents.isEmpty() || count <= 0) return emptyList()
        val weeks = ScheduleWeekAllocator.allocate(count, 5..RegularSeasonSlotBudget.RS_CAP, reservedWeeks)
        return List(count) { index ->
            val opp = opponents[index % opponents.size]
            val repeatRound = index / opponents.size
            ScheduledGameStub(
                kind = ScheduleGameKind.Conference,
                opponentTeamId = opp.teamId,
                opponentDisplayName = opp.displayName,
                homeGame = (index + repeatRound) % 2 == 0,
                rsWeekIndex = weeks.getOrElse(index) { 5 + index },
            )
        }
    }

    private fun buildNonConferenceGames(
        userTeam: TeamDisplayOverlay,
        pool: List<TeamDisplayOverlay>,
        count: Int,
        random: Random,
        reservedWeeks: Set<Int>,
    ): List<ScheduledGameStub> {
        if (count <= 0 || pool.isEmpty()) return emptyList()
        val shuffled = pool.shuffled(random)
        val weeks = ScheduleWeekAllocator.allocate(count, 8..RegularSeasonSlotBudget.RS_CAP, reservedWeeks)
        return List(count) { index ->
            val opp = shuffled[index % shuffled.size]
            ScheduledGameStub(
                kind = ScheduleGameKind.NonConference,
                opponentTeamId = opp.teamId,
                opponentDisplayName = opp.displayName,
                homeGame = random.nextBoolean(),
                rsWeekIndex = weeks.getOrElse(index) { 8 + index },
            )
        }
    }

    private fun buildEarlyEventGames(
        resolution: TournamentInviteResolution,
    ): List<ScheduledGameStub> {
        val event = (resolution as? TournamentInviteResolution.Accepted)?.event ?: return emptyList()
        return List(event.rsGameSpots) { index ->
            ScheduledGameStub(
                kind = ScheduleGameKind.EarlyEvent,
                opponentTeamId = "event-${event.templateId}-$index",
                opponentDisplayName = "${event.displayName} bracket",
                homeGame = false,
                rsWeekIndex = 1 + index,
            )
        }
    }

    private fun buildExhibitions(
        pool: List<TeamDisplayOverlay>,
        count: Int,
        random: Random,
    ): List<ScheduledGameStub> {
        val capped = count.coerceIn(0, PreseasonScheduleConstants.MAX_EXHIBITION_GAMES)
        if (capped == 0 || pool.isEmpty()) return emptyList()
        return List(capped) { index ->
            val opp = pool[random.nextInt(pool.size)]
            ScheduledGameStub(
                kind = ScheduleGameKind.Exhibition,
                opponentTeamId = opp.teamId,
                opponentDisplayName = opp.displayName,
                homeGame = index % 2 == 0,
            )
        }
    }
}
