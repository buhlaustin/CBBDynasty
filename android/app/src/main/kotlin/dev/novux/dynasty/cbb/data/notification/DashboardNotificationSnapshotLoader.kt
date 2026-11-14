package dev.novux.dynasty.cbb.data.notification

import dev.novux.dynasty.cbb.core.calendar.CalendarPhase
import dev.novux.dynasty.cbb.core.coach.CoachArchetype
import dev.novux.dynasty.cbb.core.coach.employment.CoachEmploymentStatus
import dev.novux.dynasty.cbb.core.dashboard.InSeasonWeekGames
import dev.novux.dynasty.cbb.core.notification.NotificationSnapshotInput
import dev.novux.dynasty.cbb.core.notification.NotificationSnapshotSidecar
import dev.novux.dynasty.cbb.core.save.LoadedDynasty
import dev.novux.dynasty.cbb.core.signingday.SigningDayConstants
import dev.novux.dynasty.cbb.data.coach.CoachEmploymentRepository
import dev.novux.dynasty.cbb.data.coach.CoachSkillPointRepository
import dev.novux.dynasty.cbb.data.preseason.PreseasonScheduleRepository
import dev.novux.dynasty.cbb.data.roster.DynastyRosterRepository
import dev.novux.dynasty.cbb.data.signingday.SigningDayRepository

/** Builds dashboard notification snapshots for every dynasty shell phase ([MEGA-FRAME-037]). */
object DashboardNotificationSnapshotLoader {
    suspend fun snapshotFor(
        loaded: LoadedDynasty,
        rosterRepository: DynastyRosterRepository,
        coachEmploymentRepository: CoachEmploymentRepository,
        coachSkillPointRepository: CoachSkillPointRepository,
        scheduleRepository: PreseasonScheduleRepository,
        signingDayRepository: SigningDayRepository,
        headCoachArchetype: CoachArchetype = CoachArchetype.Balanced,
        sidecar: NotificationSnapshotSidecar = NotificationSnapshotSidecar(),
    ): NotificationSnapshotInput {
        val players = rosterRepository.listPlayers(loaded.dynastyId)
        val injuryLines = players.filter { it.injuryActive }.map { player ->
            "${player.displayName} is injured."
        }
        val position = loaded.weekCheckpoint.activePosition
        val exhibitionCount = scheduleRepository.loadExhibitionCount(
            loaded.dynastyId,
            loaded.metadata.seasonIndex,
        )
        val plan = scheduleRepository.loadPlan(loaded.dynastyId, loaded.metadata.seasonIndex)
        val gameLabel = InSeasonWeekGames.forPosition(position, plan, exhibitionCount).firstOrNull()?.let { game ->
            val prep = if (game.homeGame) "vs" else "at"
            "Week ${position.weekIndex} — $prep ${game.opponentDisplayName}"
        }.takeIf { position.phase != CalendarPhase.Offseason && position.phase != CalendarPhase.Preseason }

        val employment = coachEmploymentRepository.loadState(loaded.dynastyId)
        val skillState = coachSkillPointRepository.ensureSundayGrant(
            loaded.dynastyId,
            position,
            headCoachArchetype,
        )

        var signingUnsigned = 0
        if (position.phase == CalendarPhase.Offseason &&
            position.weekIndex == SigningDayConstants.OFFSEASON_WEEK_INDEX
        ) {
            val progress = signingDayRepository.loadProgress(loaded.dynastyId, loaded.metadata.seasonIndex)
            if (!progress.hardSignComplete) {
                signingUnsigned = signingDayRepository.loadIncomingClass(loaded).size
            }
        }

        return NotificationSnapshotInput(
            asOfMs = System.currentTimeMillis(),
            newInjurySummaries = injuryLines,
            gameThisWeekLabel = gameLabel,
            coachSkillPointsBank = skillState.unspentBank,
            signingDayUnsignedCount = signingUnsigned,
            careerFired = employment.status == CoachEmploymentStatus.Unemployed,
            disciplineHeadline = sidecar.disciplineHeadline,
            carouselApprovalBodies = sidecar.carouselApprovalBodies,
            selectionSundayBody = sidecar.selectionSundayBody,
            medicalRedshirtBodies = sidecar.medicalRedshirtBodies,
            fundraisingCompleteBodies = sidecar.fundraisingCompleteBodies,
        )
    }
}
