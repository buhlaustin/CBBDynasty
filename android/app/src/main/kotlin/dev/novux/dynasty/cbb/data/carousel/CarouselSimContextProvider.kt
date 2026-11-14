package dev.novux.dynasty.cbb.data.carousel

import dev.novux.dynasty.cbb.core.carousel.CarouselSimContext
import dev.novux.dynasty.cbb.core.carousel.CarouselSimContextBootstrap
import dev.novux.dynasty.cbb.core.coach.employment.CoachEmploymentStatus
import dev.novux.dynasty.cbb.data.coach.CoachEmploymentRepository
import dev.novux.dynasty.cbb.data.coach.CoachSecurityRepository
import dev.novux.dynasty.cbb.data.coach.SchoolStaffRepository
import dev.novux.dynasty.cbb.data.league.LeagueWeekBatchRepository
import dev.novux.dynasty.cbb.data.pack.PackRepository
import dev.novux.dynasty.cbb.database.DynastyDatabase

/** Hydrates carousel league background sim from dynasty persistence ([MEGA-GDD-119]). */
class CarouselSimContextProvider(
    private val database: DynastyDatabase,
    private val packRepository: PackRepository,
    private val leagueWeekBatchRepository: LeagueWeekBatchRepository,
    private val schoolStaffRepository: SchoolStaffRepository,
    private val coachSecurityRepository: CoachSecurityRepository,
    private val coachEmploymentRepository: CoachEmploymentRepository,
) {
    suspend fun load(dynastyId: String, seasonIndex: Int): CarouselSimContext {
        val coach = database.dynastyCoachDao().getByDynastyId(dynastyId)
        val userTeamId = coach?.userTeamId ?: "user-$dynastyId"
        val packIndex = packRepository.activeSession().index
        val teams = packIndex.allTeamDisplays()
        val priorSeasonIndex = (seasonIndex - 1).coerceAtLeast(1)
        val standings = leagueWeekBatchRepository.loadStandings(dynastyId, priorSeasonIndex)
        val headCoach = schoolStaffRepository.loadHeadCoachCard(dynastyId)
        val security = coachSecurityRepository.loadForUserDynasty(dynastyId)
        val employment = coachEmploymentRepository.loadState(dynastyId)
        val unemployed = employment.status == CoachEmploymentStatus.Unemployed
        return CarouselSimContextBootstrap.fromStandingsAndPack(
            dynastyId = dynastyId,
            seasonIndex = seasonIndex,
            userTeamId = userTeamId,
            standings = standings,
            teams = teams,
            userHeadCoach = headCoach,
            userSecurity = security,
            userUnemployed = unemployed,
            priorSeasonIndex = priorSeasonIndex,
        )
    }
}
