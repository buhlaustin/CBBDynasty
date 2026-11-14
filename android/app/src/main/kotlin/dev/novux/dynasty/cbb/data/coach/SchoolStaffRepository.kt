package dev.novux.dynasty.cbb.data.coach

import dev.novux.dynasty.cbb.core.coach.CoachArchetype
import dev.novux.dynasty.cbb.core.coach.card.HeadCoachCard
import dev.novux.dynasty.cbb.core.coach.card.HeadCoachCardFactory
import dev.novux.dynasty.cbb.core.coach.staff.SchoolStaffRoster
import dev.novux.dynasty.cbb.core.coach.staff.StaffRosterBootstrap
import dev.novux.dynasty.cbb.core.strategy.DefensiveSchemePreset
import dev.novux.dynasty.cbb.core.strategy.OffensiveSchemePreset
import dev.novux.dynasty.cbb.data.pack.PackRepository
import dev.novux.dynasty.cbb.database.DynastyDatabase
import dev.novux.dynasty.cbb.core.coach.staff.StaffCandidatePool
import dev.novux.dynasty.cbb.core.coach.staff.StaffManagePolicy
import dev.novux.dynasty.cbb.database.staff.toAssistant
import dev.novux.dynasty.cbb.database.staff.toEntity
import dev.novux.dynasty.cbb.database.staff.toSupport
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

sealed class StaffManageResult {
    data object Success : StaffManageResult()
    data class Blocked(val reason: String) : StaffManageResult()
}

class SchoolStaffRepository(
    private val database: DynastyDatabase,
    private val packRepository: PackRepository,
    private val headCoachUserContractRepository: HeadCoachUserContractRepository,
) {
    private val staffDao = database.dynastyStaffDao()
    private val coachDao = database.dynastyCoachDao()

    suspend fun userTeamId(dynastyId: String): String? = withContext(Dispatchers.IO) {
        coachDao.getByDynastyId(dynastyId)?.userTeamId
    }

    suspend fun ensureBootstrap(dynastyId: String, schoolDisplayName: String) {
        val existing = staffDao.listForDynasty(dynastyId)
        if (existing.isNotEmpty()) return
        val seed = dynastyId.hashCode().toLong()
        val assistants = StaffRosterBootstrap.defaultAssistants(dynastyId, schoolDisplayName, seed)
        val support = StaffRosterBootstrap.defaultSupport(dynastyId, seed)
        staffDao.upsertAll(
            assistants.map { it.toEntity(dynastyId) } + support.map { it.toEntity(dynastyId) },
        )
    }

    suspend fun loadStaffRoster(dynastyId: String, seasonRecordLabel: String = "0-0"): SchoolStaffRoster? {
        val coach = coachDao.getByDynastyId(dynastyId) ?: return null
        val team = packRepository.activeSession().index.team(coach.userTeamId)
        val schoolName = team?.displayName ?: "Your program"
        ensureBootstrap(dynastyId, schoolName)
        val archetype = CoachArchetype.fromWire(coach.archetypeWire) ?: CoachArchetype.Balanced
        val offensive = OffensiveSchemePreset.fromWire(coach.offensiveSchemeWire) ?: OffensiveSchemePreset.Balanced
        val defensive = DefensiveSchemePreset.fromWire(coach.defensiveSchemeWire) ?: DefensiveSchemePreset.ManToMan
        val alma = coach.almaMaterTeamId?.let { packRepository.activeSession().index.team(it)?.displayName }
        val programPrestige = team?.programPrestige ?: 50
        headCoachUserContractRepository.ensureDefaults(dynastyId)
        val contractState = headCoachUserContractRepository.load(dynastyId)
        val head = HeadCoachCardFactory.build(
            dynastyId = dynastyId,
            displayName = HeadCoachCardFactory.displayName(coach.firstName, coach.lastName, coach.nameSuffixWire),
            age = coach.age,
            schoolDisplayName = schoolName,
            archetype = archetype,
            offensiveScheme = offensive,
            defensiveScheme = defensive,
            almaMaterLabel = alma,
            seasonRecordLabel = seasonRecordLabel,
            avatarPresetId = coach.avatarPresetId,
            programPrestige = programPrestige,
            contractLengthYears = contractState.contractLengthYears,
            contractYearsLeft = contractState.contractYearsLeft,
            annualSalaryUsd = contractState.annualSalaryUsd,
        )
        val rows = staffDao.listForDynasty(dynastyId)
        val assistants = rows.mapNotNull { it.toAssistant(schoolName, seasonRecordLabel) }
        val support = rows.mapNotNull { it.toSupport() }
        return SchoolStaffRoster(headCoach = head, assistants = assistants, supportStaff = support)
    }

    suspend fun loadHeadCoachCard(dynastyId: String): HeadCoachCard? =
        loadStaffRoster(dynastyId)?.headCoach

    suspend fun loadAssistantCard(dynastyId: String, staffId: String) =
        loadStaffRoster(dynastyId)?.assistants?.firstOrNull { it.staffId == staffId }

    suspend fun fireStaff(dynastyId: String, staffId: String): StaffManageResult = withContext(Dispatchers.IO) {
        val roster = loadStaffRoster(dynastyId) ?: return@withContext StaffManageResult.Blocked("no_roster")
        val isAssistant = roster.assistants.any { it.staffId == staffId }
        if (isAssistant && roster.assistants.size <= StaffManagePolicy.MIN_ASSISTANT_COUNT) {
            return@withContext StaffManageResult.Blocked("min_assistants")
        }
        val known = roster.assistants.any { it.staffId == staffId } ||
            roster.supportStaff.any { it.staffId == staffId }
        if (!known) return@withContext StaffManageResult.Blocked("unknown_staff")
        staffDao.deleteStaff(dynastyId, staffId)
        StaffManageResult.Success
    }

    suspend fun hireFromPool(
        dynastyId: String,
        replacingStaffId: String?,
        seasonRecordLabel: String = "0-0",
    ): StaffManageResult = withContext(Dispatchers.IO) {
        val roster = loadStaffRoster(dynastyId) ?: return@withContext StaffManageResult.Blocked("no_roster")
        replacingStaffId?.let { fireStaff(dynastyId, it) }
        val coach = coachDao.getByDynastyId(dynastyId) ?: return@withContext StaffManageResult.Blocked("no_coach")
        val schoolName = packRepository.activeSession().index.team(coach.userTeamId)?.displayName ?: "Your program"
        val replacedAssistant = replacingStaffId?.let { id -> roster.assistants.firstOrNull { it.staffId == id } }
        val replacedSupport = replacingStaffId?.let { id -> roster.supportStaff.firstOrNull { it.staffId == id } }
        val seed = System.currentTimeMillis()
        val entity = when {
            replacedSupport != null -> StaffCandidatePool.generateSupportHire(
                dynastyId,
                replacedSupport.role,
                seed,
            ).toEntity(dynastyId)
            else -> StaffCandidatePool.generateAssistantHire(
                dynastyId,
                schoolName,
                replacedAssistant?.role ?: dev.novux.dynasty.cbb.core.coach.staff.CoachTrackRole.Assistant,
                seed,
                seasonRecordLabel,
            ).toEntity(dynastyId)
        }
        staffDao.upsertAll(listOf(entity))
        StaffManageResult.Success
    }

    suspend fun poachAssistant(
        dynastyId: String,
        replacingStaffId: String,
        seasonRecordLabel: String = "0-0",
    ): StaffManageResult = withContext(Dispatchers.IO) {
        val roster = loadStaffRoster(dynastyId) ?: return@withContext StaffManageResult.Blocked("no_roster")
        val existing = roster.assistants.firstOrNull { it.staffId == replacingStaffId }
            ?: return@withContext StaffManageResult.Blocked("not_assistant")
        staffDao.deleteStaff(dynastyId, replacingStaffId)
        val coach = coachDao.getByDynastyId(dynastyId) ?: return@withContext StaffManageResult.Blocked("no_coach")
        val schoolName = packRepository.activeSession().index.team(coach.userTeamId)?.displayName ?: "Your program"
        val hire = StaffCandidatePool.generateAssistantPoach(
            dynastyId,
            schoolName,
            existing.role,
            System.currentTimeMillis(),
            seasonRecordLabel,
        )
        staffDao.upsertAll(listOf(hire.toEntity(dynastyId)))
        StaffManageResult.Success
    }
}
