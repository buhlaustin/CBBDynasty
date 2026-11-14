package dev.novux.dynasty.cbb.core.recruiting.ai

import dev.novux.dynasty.cbb.core.pack.TeamDisplayOverlay
import dev.novux.dynasty.cbb.core.recruiting.RecruitingPipelineStates
import dev.novux.dynasty.cbb.core.recruiting.SchoolRecruitingContext
import dev.novux.dynasty.cbb.core.roster.PlayerPosition

data class AiProgramSnapshot(
    val programId: String,
    val programPrestige: Int,
    val schoolHomeState: String,
    val pipelineStates: Set<String> = emptySet(),
    val nilPoolAnnualUsd: Int,
    val positionsNeed: Set<PlayerPosition> = PlayerPosition.entries.toSet(),
) {
    fun toSchoolContext(): SchoolRecruitingContext =
        SchoolRecruitingContext(
            programPrestige = programPrestige,
            schoolHomeState = schoolHomeState,
            pipelineStates = pipelineStates,
            nilPoolAnnualUsd = nilPoolAnnualUsd,
            positionsNeed = positionsNeed,
        )
}

object AiLeagueProgramGenerator {
    private val states = listOf("TX", "OH", "FL", "CA", "NC", "IN", "KY", "VA", "IL", "GA")

    /** Pack-backed league — same prestige / pipeline / NIL rules as the user ([MEGA-RECR-068]). */
    fun fromPackTeams(
        teams: List<TeamDisplayOverlay>,
        excludeTeamId: String?,
        nilPoolAnnualUsd: (TeamDisplayOverlay) -> Long,
    ): List<AiProgramSnapshot> =
        teams
            .asSequence()
            .filter { it.teamId != excludeTeamId }
            .map { team ->
                AiProgramSnapshot(
                    programId = team.teamId,
                    programPrestige = team.programPrestige,
                    schoolHomeState = team.homeState,
                    pipelineStates = RecruitingPipelineStates.forHomeState(team.homeState),
                    nilPoolAnnualUsd = nilPoolAnnualUsd(team).toInt().coerceAtLeast(0),
                )
            }
            .toList()

    fun demoLeague(
        count: Int = 365,
        seasonIndex: Int,
        excludeProgramId: String? = null,
    ): List<AiProgramSnapshot> =
        (0 until count)
            .mapNotNull { index ->
                val id = "team-conf-ai-$index"
                if (id == excludeProgramId) return@mapNotNull null
                val prestige = 15 + ((index * 17 + seasonIndex) % 72)
                val home = states[index % states.size]
                val pipeline = states.filterIndexed { i, state -> i % 5 == index % 5 && state != home }.toSet()
                val nilPool = 120_000 + prestige * 25_000
                AiProgramSnapshot(
                    programId = id,
                    programPrestige = prestige,
                    schoolHomeState = home,
                    pipelineStates = pipeline,
                    nilPoolAnnualUsd = nilPool,
                )
            }
}
