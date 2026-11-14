package dev.novux.dynasty.cbb.core.advance

import dev.novux.dynasty.cbb.core.calendar.checkpoint.WeekCheckpointState

/**
 * Advance week confirmation + resolve hook
 * ([planning/game-frame.md#advance-week-confirmation-sim-week]).
 */
object SimWeekAdvanceResolver {
    fun buildConfirmation(input: SimWeekAdvanceInput): SimWeekConfirmation {
        val lines = mutableListOf<SimWeekConfirmationLine>()
        if (input.unplayedUserGameCount > 0) {
            val opponent = input.unplayedUserGameOpponentDisplayName?.takeIf { it.isNotBlank() }
            lines.add(
                SimWeekConfirmationLine(
                    kind = SimWeekConfirmationLineKind.UnplayedGameFastSim,
                    stringFormatArgs = listOf(opponent ?: "your opponent"),
                ),
            )
        }
        val willAutoMaintain = input.recruitingAutoMaintainEnabled &&
            input.recruitingHubOpen &&
            input.userManualRecruitingSpent == 0 &&
            input.recruitingPointsAvailable > 0
        val portalAutoSpendLine =
            willAutoMaintain && OffseasonPortalSimWeekPolicy.usePortalSpendLineInsteadOfGenericAutoMaintain(input)
        if (willAutoMaintain && !portalAutoSpendLine) {
            lines.add(
                SimWeekConfirmationLine(
                    SimWeekConfirmationLineKind.UserRecruitingAutoMaintain,
                    listOf(input.estimatedAutoRecruitingSpend.coerceAtLeast(1)),
                ),
            )
        }
        if (input.portalRecruitingWeekOpen) {
            lines.add(SimWeekConfirmationLine(SimWeekConfirmationLineKind.PortalRecruitingWeek))
        }
        if (input.portalRecruitingWindowClosed) {
            lines.add(SimWeekConfirmationLine(SimWeekConfirmationLineKind.PortalRecruitingWindowClosed))
        }
        if (OffseasonPortalSimWeekPolicy.showPortalOptionalNudgeLine(input)) {
            lines.add(
                SimWeekConfirmationLine(
                    SimWeekConfirmationLineKind.OffseasonPortalOptionalNudges,
                    formatArgs = listOf(input.unresolvedOptionalTaskCount),
                ),
            )
        }
        if (portalAutoSpendLine) {
            val spend = input.estimatedAutoRecruitingSpend.coerceAtLeast(1)
            lines.add(
                SimWeekConfirmationLine(
                    SimWeekConfirmationLineKind.PortalUserSchoolAutoSpend,
                    formatArgs = listOf(spend),
                ),
            )
        }
        InjuryByeAdvanceConfirmPolicy.injuryTickLineKind(input)?.let { kind ->
            lines.add(
                SimWeekConfirmationLine(
                    kind,
                    listOf(input.injuredPlayersTicking),
                ),
            )
        }
        if (InjuryByeAdvanceConfirmPolicy.appendSimpleAdvanceAfterInjuryBye(input)) {
            lines.add(
                SimWeekConfirmationLine(
                    SimWeekConfirmationLineKind.SimpleAdvance,
                    formatArgs = emptyList(),
                ),
            )
        }
        if (input.simulatesLeagueWeekBatch) {
            lines.add(SimWeekConfirmationLine(SimWeekConfirmationLineKind.LeagueWeekBatch))
        }
        if (input.recruitingHubOpen || input.portalRecruitingWeekOpen) {
            lines.add(SimWeekConfirmationLine(SimWeekConfirmationLineKind.LeagueRecruitingBatch))
        }
        if (input.offseasonMilestoneAdvance || input.inSeasonDestinationAcknowledgment) {
            if (lines.none { it.kind == SimWeekConfirmationLineKind.SimpleAdvance }) {
                lines.add(
                    SimWeekConfirmationLine(
                        SimWeekConfirmationLineKind.SimpleAdvance,
                        formatArgs = emptyList(),
                    ),
                )
            }
        }
        if (lines.isEmpty()) {
            lines.add(
                SimWeekConfirmationLine(
                    SimWeekConfirmationLineKind.SimpleAdvance,
                    formatArgs = emptyList(),
                ),
            )
        }
        val needsDialog = input.unplayedUserGameCount > 0 ||
            willAutoMaintain ||
            input.unresolvedOptionalTaskCount > 0 ||
            input.portalRecruitingWeekOpen ||
            input.portalRecruitingWindowClosed ||
            input.offseasonMilestoneAdvance ||
            input.inSeasonDestinationAcknowledgment ||
            input.injuredPlayersTicking > 0 ||
            input.simulatesLeagueWeekBatch
        return SimWeekConfirmation(
            needsDialog = needsDialog,
            nextWeekLabel = input.nextWeekLabel,
            lines = lines,
        )
    }

    fun resolvePreAdvance(
        checkpoint: WeekCheckpointState,
        input: SimWeekAdvanceInput,
        @Suppress("UNUSED_PARAMETER") dynastySeed: Long,
    ): SimWeekAdvanceResolution {
        if (input.unplayedUserGameCount <= 0) {
            return SimWeekAdvanceResolution(checkpoint, fastSimmedGames = 0)
        }
        if (checkpoint.provisionalGames.isNotEmpty()) {
            return SimWeekAdvanceResolution(checkpoint, fastSimmedGames = 0)
        }
        val (afterSim, outcomes) = UserLeagueGameSimulator.fastSimUnplayed(
            checkpoint,
            input.userLeagueFastSim,
            input.unplayedUserGameCount,
        )
        return SimWeekAdvanceResolution(
            checkpointAfterPreAdvance = afterSim,
            fastSimmedGames = outcomes.size,
        )
    }
}
