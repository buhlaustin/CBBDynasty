package dev.novux.dynasty.cbb.data.seasonreview

import dev.novux.dynasty.cbb.core.balance.BalanceNilAnnualPoolParser
import dev.novux.dynasty.cbb.core.balance.BalanceRecruitingParser
import dev.novux.dynasty.cbb.core.seasonreview.SeasonReviewSnapshot
import dev.novux.dynasty.cbb.core.seasonreview.SeasonReviewSnapshotFactory
import dev.novux.dynasty.cbb.data.awards.YearlyAwardsRepository
import dev.novux.dynasty.cbb.data.coach.CoachSecurityRepository
import dev.novux.dynasty.cbb.data.league.LeagueWeekBatchRepository
import dev.novux.dynasty.cbb.data.offseason.OffseasonWeekNineRepository
import dev.novux.dynasty.cbb.data.pack.PackRepository
import dev.novux.dynasty.cbb.data.recruiting.RecruitingEconomyRepository
import dev.novux.dynasty.cbb.core.save.LoadedDynasty
import dev.novux.dynasty.cbb.database.DynastyDatabase

class SeasonReviewRepository(
    private val database: DynastyDatabase,
    private val packRepository: PackRepository,
    private val leagueWeekBatchRepository: LeagueWeekBatchRepository,
    private val recruitingEconomyRepository: RecruitingEconomyRepository,
    private val offseasonWeekNineRepository: OffseasonWeekNineRepository,
    private val coachSecurityRepository: CoachSecurityRepository,
    private val yearlyAwardsRepository: YearlyAwardsRepository,
) {
    suspend fun loadSnapshot(loaded: LoadedDynasty): SeasonReviewSnapshot? {
        yearlyAwardsRepository.wrapSeasonIfNeeded(loaded.dynastyId, loaded.metadata.seasonIndex)
        val index = packRepository.activeSession().index
        val coachEntity = database.dynastyCoachDao().getByDynastyId(loaded.dynastyId) ?: return null
        val team = index.team(coachEntity.userTeamId) ?: return null
        val standings = leagueWeekBatchRepository.loadStandings(loaded.dynastyId, loaded.metadata.seasonIndex)
        val ledger = recruitingEconomyRepository.loadOrOpenCycle(loaded, team.programPrestige)
        val economy = BalanceRecruitingParser.parse(loaded.balanceRuleset.recruiting)
        val nilTables = BalanceNilAnnualPoolParser.parse(loaded.balanceRuleset.nil)
        val facilities = offseasonWeekNineRepository.loadEconomy(loaded.dynastyId, loaded.metadata.seasonIndex).facilityGrades
        val record = standings?.let {
            dev.novux.dynasty.cbb.core.seasonreview.SeasonReviewRecordBuilder.build(
                coachEntity.userTeamId,
                it,
                index,
            )
        }
        val contractYearsLeft = 4
        val coachSecurity = coachSecurityRepository.loadOrBootstrap(
            dynastyId = loaded.dynastyId,
            contractYearsLeft = contractYearsLeft,
            record = record ?: dev.novux.dynasty.cbb.core.school.SchoolSeasonRecord.Zero,
        )
        val awards = yearlyAwardsRepository.loadArchive(loaded.dynastyId, loaded.metadata.seasonIndex)
        val awardHighlights = awards?.let { archive ->
            archive.ncbaOverview + listOfNotNull(archive.userTeamMvp)
        }.orEmpty()
        return SeasonReviewSnapshotFactory.build(
            seasonIndex = loaded.metadata.seasonIndex,
            userTeam = team,
            packIndex = index,
            standings = standings,
            ledger = ledger,
            economy = economy,
            nilTables = nilTables,
            facilityGrades = facilities,
            coach = coachSecurity,
            contractYearsLeft = contractYearsLeft,
            awardHighlights = awardHighlights,
        )
    }
}
