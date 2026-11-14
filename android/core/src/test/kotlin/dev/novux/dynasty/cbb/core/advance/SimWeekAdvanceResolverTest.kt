package dev.novux.dynasty.cbb.core.advance

import dev.novux.dynasty.cbb.core.calendar.CalendarPhase
import dev.novux.dynasty.cbb.core.calendar.PhaseWeekPolicy
import dev.novux.dynasty.cbb.core.calendar.checkpoint.ProvisionalGameMark
import dev.novux.dynasty.cbb.core.calendar.checkpoint.WeekCheckpointState
import dev.novux.dynasty.cbb.core.sim.GameSimTeamInput
import dev.novux.dynasty.cbb.core.sim.tuning.IntMinMax
import dev.novux.dynasty.cbb.core.sim.tuning.SimShotClock
import dev.novux.dynasty.cbb.core.sim.tuning.SimTimeUsedSeconds
import dev.novux.dynasty.cbb.core.sim.tuning.SimTuning
import dev.novux.dynasty.cbb.core.sim.tuning.SimVenueModifiers
import kotlinx.serialization.json.JsonObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SimWeekAdvanceResolverTest {
    private val tuning = SimTuning(
        simTuningId = "test",
        schemaVersion = 1,
        possessionsPerGameTarget = IntMinMax(60, 70),
        shotClock = SimShotClock(full = 30, offensiveRebound = 20),
        timeUsedSeconds = SimTimeUsedSeconds(
            quickTurnover = listOf(3, 6),
            quickShot = listOf(8, 14),
            foulStoppage = listOf(12, 18),
        ),
        eventWeights = JsonObject(emptyMap()),
        homeNeutralModifiers = SimVenueModifiers(1.0, 1.0, 1.0),
    )

    @Test
    fun confirmation_fastSimLineNamesOpponent() {
        val confirmation = SimWeekAdvanceResolver.buildConfirmation(
            SimWeekAdvanceInput(
                nextWeekLabel = "Regular Season · Week 2",
                unplayedUserGameCount = 1,
                unplayedUserGameOpponentDisplayName = "Kentucky",
                recruitingPointsAvailable = 0,
                userManualRecruitingSpent = 0,
                recruitingHubOpen = false,
                estimatedAutoRecruitingSpend = 0,
                portalRecruitingWeekOpen = false,
            ),
        )
        val line = confirmation.lines.first { it.kind == SimWeekConfirmationLineKind.UnplayedGameFastSim }
        assertEquals(listOf("Kentucky"), line.stringFormatArgs)
    }

    @Test
    fun confirmation_includesRecruitingLineWhenAutoMaintain() {
        val confirmation = SimWeekAdvanceResolver.buildConfirmation(
            SimWeekAdvanceInput(
                nextWeekLabel = "Regular Season · Week 2",
                unplayedUserGameCount = 0,
                recruitingPointsAvailable = 1500,
                userManualRecruitingSpent = 0,
                recruitingHubOpen = true,
                estimatedAutoRecruitingSpend = 200,
                portalRecruitingWeekOpen = false,
            ),
        )
        assertTrue(confirmation.needsDialog)
        assertTrue(
            confirmation.lines.any {
                it.kind == SimWeekConfirmationLineKind.UserRecruitingAutoMaintain
            },
        )
    }

    @Test
    fun confirmation_skipsRecruitingLineWhenAutomationOff() {
        val confirmation = SimWeekAdvanceResolver.buildConfirmation(
            SimWeekAdvanceInput(
                nextWeekLabel = "Regular Season · Week 2",
                unplayedUserGameCount = 0,
                recruitingPointsAvailable = 1500,
                userManualRecruitingSpent = 0,
                recruitingHubOpen = true,
                estimatedAutoRecruitingSpend = 200,
                portalRecruitingWeekOpen = false,
                recruitingAutoMaintainEnabled = false,
            ),
        )
        assertTrue(
            confirmation.lines.none {
                it.kind == SimWeekConfirmationLineKind.UserRecruitingAutoMaintain
            },
        )
    }

    @Test
    fun resolve_fastSimsUnplayedGameBeforeAdvance() {
        val state = WeekCheckpointState.bootstrap(1, CalendarPhase.RegularSeason)
        val gameId = UserLeagueGameIds.forPosition(state.activePosition)
        val resolution = SimWeekAdvanceResolver.resolvePreAdvance(
            state,
            SimWeekAdvanceInput(
                nextWeekLabel = "Week 2",
                unplayedUserGameCount = 1,
                recruitingPointsAvailable = 0,
                userManualRecruitingSpent = 0,
                recruitingHubOpen = false,
                estimatedAutoRecruitingSpend = 0,
                portalRecruitingWeekOpen = false,
                userLeagueFastSim = UserLeagueFastSimBundle(
                    listOf(
                        UserLeagueGameSimContext(
                            gameId = gameId,
                            userTeamIsHome = true,
                            tuning = tuning,
                            seed = 42L,
                            home = GameSimTeamInput("h", 78, 74),
                            away = GameSimTeamInput("a", 76, 75),
                        ),
                    ),
                ),
            ),
            dynastySeed = 42L,
        )
        assertEquals(1, resolution.fastSimmedGames)
        assertEquals(1, resolution.checkpointAfterPreAdvance.provisionalGames.size)
        assertTrue(resolution.checkpointAfterPreAdvance.provisionalGames.values.first().possessionEngine)
    }

    @Test
    fun resolve_skipsFastSimWhenProvisionalStaged() {
        val gameId = UserLeagueGameIds.forPosition(
            WeekCheckpointState.bootstrap(1, CalendarPhase.RegularSeason).activePosition,
        )
        val state = WeekCheckpointState.bootstrap(1, CalendarPhase.RegularSeason)
            .stageSim(ProvisionalGameMark(gameId, 80, 70, possessionEngine = true))
        val resolution = SimWeekAdvanceResolver.resolvePreAdvance(
            state,
            SimWeekAdvanceInput(
                nextWeekLabel = "Week 2",
                unplayedUserGameCount = 0,
                recruitingPointsAvailable = 0,
                userManualRecruitingSpent = 0,
                recruitingHubOpen = false,
                estimatedAutoRecruitingSpend = 0,
                portalRecruitingWeekOpen = false,
            ),
            dynastySeed = 1L,
        )
        assertEquals(0, resolution.fastSimmedGames)
        assertEquals(80, resolution.checkpointAfterPreAdvance.provisionalGames[gameId]?.homeScore)
    }

    @Test
    fun confirmation_byeWeekInjuryTickShowsByeCopyAndDestination() {
        val confirmation = SimWeekAdvanceResolver.buildConfirmation(
            SimWeekAdvanceInput(
                nextWeekLabel = "Regular Season · Week 8",
                unplayedUserGameCount = 0,
                recruitingPointsAvailable = 0,
                userManualRecruitingSpent = 0,
                recruitingHubOpen = false,
                estimatedAutoRecruitingSpend = 0,
                portalRecruitingWeekOpen = false,
                injuredPlayersTicking = 2,
                userOnByeWeek = true,
            ),
        )
        assertTrue(confirmation.needsDialog)
        assertEquals(
            SimWeekConfirmationLineKind.InjuryWeeksTickByeWeek,
            confirmation.lines.first().kind,
        )
        assertTrue(
            confirmation.lines.any { it.kind == SimWeekConfirmationLineKind.SimpleAdvance },
        )
    }

    @Test
    fun unplayedGames_detectedWhenNoProvisionalMark() {
        val state = WeekCheckpointState.bootstrap(1, CalendarPhase.RegularSeason)
        val count = UserWeekGameStatus.unplayedUserGames(state, teamHasScheduledGame = true)
        assertEquals(1, count)
    }

    @Test
    fun confirmation_offseasonMilestoneShowsDestination() {
        val confirmation = SimWeekAdvanceResolver.buildConfirmation(
            SimWeekAdvanceInput(
                nextWeekLabel = "Offseason · Week 7",
                unplayedUserGameCount = 0,
                recruitingPointsAvailable = 0,
                userManualRecruitingSpent = 0,
                recruitingHubOpen = false,
                estimatedAutoRecruitingSpend = 0,
                portalRecruitingWeekOpen = false,
                offseasonMilestoneAdvance = true,
            ),
        )
        assertTrue(confirmation.needsDialog)
        assertTrue(confirmation.lines.any { it.kind == SimWeekConfirmationLineKind.SimpleAdvance })
    }

    @Test
    fun confirmation_portalWindowClosedLine() {
        val confirmation = SimWeekAdvanceResolver.buildConfirmation(
            SimWeekAdvanceInput(
                nextWeekLabel = "Offseason · Week 7",
                unplayedUserGameCount = 0,
                recruitingPointsAvailable = 0,
                userManualRecruitingSpent = 0,
                recruitingHubOpen = false,
                estimatedAutoRecruitingSpend = 0,
                portalRecruitingWeekOpen = false,
                portalRecruitingWindowClosed = true,
            ),
        )
        assertTrue(confirmation.needsDialog)
        assertTrue(
            confirmation.lines.any { it.kind == SimWeekConfirmationLineKind.PortalRecruitingWindowClosed },
        )
    }

    @Test
    fun confirmation_inSeasonShowsSimpleAdvanceWhenPolicySet() {
        val confirmation = SimWeekAdvanceResolver.buildConfirmation(
            SimWeekAdvanceInput(
                nextWeekLabel = "Regular Season · Week 4",
                unplayedUserGameCount = 0,
                recruitingPointsAvailable = 0,
                userManualRecruitingSpent = 0,
                recruitingHubOpen = false,
                estimatedAutoRecruitingSpend = 0,
                portalRecruitingWeekOpen = false,
                inSeasonDestinationAcknowledgment = true,
            ),
        )
        assertTrue(confirmation.needsDialog)
        assertTrue(confirmation.lines.any { it.kind == SimWeekConfirmationLineKind.SimpleAdvance })
    }

    @Test
    fun confirmation_includesLeagueBatchLineDuringRegularSeason() {
        val confirmation = SimWeekAdvanceResolver.buildConfirmation(
            SimWeekAdvanceInput(
                nextWeekLabel = "Regular Season · Week 3",
                unplayedUserGameCount = 0,
                recruitingPointsAvailable = 0,
                userManualRecruitingSpent = 0,
                recruitingHubOpen = false,
                estimatedAutoRecruitingSpend = 0,
                portalRecruitingWeekOpen = false,
                simulatesLeagueWeekBatch = true,
            ),
        )
        assertTrue(confirmation.needsDialog)
        assertTrue(
            confirmation.lines.any { it.kind == SimWeekConfirmationLineKind.LeagueWeekBatch },
        )
    }
}
