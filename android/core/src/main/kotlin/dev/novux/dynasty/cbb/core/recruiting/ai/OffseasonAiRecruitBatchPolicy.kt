package dev.novux.dynasty.cbb.core.recruiting.ai

/**
 * Each week advance runs league AI recruiting via [AiRecruitingRepository.runLeagueWeekIfActive]
 * between player sessions ([MEGA-GDD-118]).
 */
object OffseasonAiRecruitBatchPolicy {
    const val MEGA_ID: String = "MEGA-GDD-118"
}