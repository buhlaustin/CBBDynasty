package dev.novux.dynasty.cbb.core.recruiting

/** Debug / CI hooks for recruiting hub regression ([MEGA-QA-077]). */
object RecruitingHubTestHooks {
    @Volatile
    var forceEmptyRecommendedPool: Boolean = false
}
