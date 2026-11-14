package dev.novux.dynasty.cbb.data.notification

import dev.novux.dynasty.cbb.core.balance.BalanceMedicalRedshirtRules
import dev.novux.dynasty.cbb.core.calendar.CalendarPhase
import dev.novux.dynasty.cbb.core.carousel.CarouselStatus
import dev.novux.dynasty.cbb.core.dashboard.DashboardPostseasonSeedResolver
import dev.novux.dynasty.cbb.core.injuries.MedicalRedshirtPolicy
import dev.novux.dynasty.cbb.core.notification.NotificationSnapshotSidecar
import dev.novux.dynasty.cbb.core.offseason.weeknine.OffseasonWeekNineConstants
import dev.novux.dynasty.cbb.core.roster.RosterPlayer
import dev.novux.dynasty.cbb.core.save.LoadedDynasty
import dev.novux.dynasty.cbb.data.carousel.CoachingCarouselRepository
import dev.novux.dynasty.cbb.data.discipline.DisciplineRepository
import dev.novux.dynasty.cbb.data.offseason.OffseasonWeekNineRepository
import dev.novux.dynasty.cbb.data.roster.DynastyRosterRepository

/** Loads [NotificationSnapshotSidecar] catalog rows for the dashboard feed ([MEGA-SHELL-049]–[MEGA-SHELL-053]). */
object DashboardNotificationSnapshotSidecarLoader {
    suspend fun load(
        loaded: LoadedDynasty,
        rosterRepository: DynastyRosterRepository,
        disciplineRepository: DisciplineRepository,
        coachingCarouselRepository: CoachingCarouselRepository,
        weekNineRepository: OffseasonWeekNineRepository,
        userTeamId: String?,
        selectionSundayPublished: Boolean,
        loadStandingsSelection: suspend () -> dev.novux.dynasty.cbb.core.selection.SelectionSundaySnapshot?,
        medicalRules: BalanceMedicalRedshirtRules = BalanceMedicalRedshirtRules(),
    ): NotificationSnapshotSidecar {
        val discipline = disciplineRepository.loadState(loaded.dynastyId, loaded.metadata.seasonIndex).pending
        val disciplineHeadline = discipline?.let { "${it.headline} — ${it.playerDisplayName}" }

        val carousel = coachingCarouselRepository.loadOrCreate(loaded.dynastyId, loaded.metadata.seasonIndex)
        val carouselBodies =
            if (carousel.status == CarouselStatus.AwaitingUserApprovals) {
                carousel.pendingApprovals.map { "${it.headline} — ${it.detail}" }
            } else {
                emptyList()
            }

        val selectionSundayBody = if (selectionSundayPublished && userTeamId != null) {
            val selection = loadStandingsSelection()
            DashboardPostseasonSeedResolver.label(userTeamId, selection)?.let { seed ->
                "Your program is in the national field ($seed)."
            }
        } else {
            null
        }

        val position = loaded.weekCheckpoint.activePosition
        val rsWeek = position.weekIndex
        val players = rosterRepository.listPlayers(loaded.dynastyId)
        val medicalBodies = if (position.phase != CalendarPhase.Exhibition) {
            medicalRedshirtBodies(players, rsWeek, medicalRules)
        } else {
            emptyList()
        }

        val fundraisingBodies = if (
            position.phase == CalendarPhase.Offseason &&
            position.weekIndex == OffseasonWeekNineConstants.OFFSEASON_WEEK_INDEX
        ) {
            val economy = weekNineRepository.loadEconomy(loaded.dynastyId, loaded.metadata.seasonIndex)
            buildList {
                economy.completedEvents.forEach { event ->
                    add("${event.label} fundraiser completed.")
                }
                if (economy.namingRightsSold) {
                    add("Arena naming rights sold.")
                }
            }
        } else {
            emptyList()
        }

        return NotificationSnapshotSidecar(
            disciplineHeadline = disciplineHeadline,
            carouselApprovalBodies = carouselBodies,
            selectionSundayBody = selectionSundayBody,
            medicalRedshirtBodies = medicalBodies,
            fundraisingCompleteBodies = fundraisingBodies,
        )
    }

    private fun medicalRedshirtBodies(
        players: List<RosterPlayer>,
        rsWeekIndex: Int,
        rules: BalanceMedicalRedshirtRules,
    ): List<String> =
        players.mapNotNull { player ->
            if (MedicalRedshirtPolicy.evaluate(player, rsWeekIndex, rules).eligible) {
                "${player.displayLabel()} may qualify for a medical redshirt."
            } else {
                null
            }
        }
}