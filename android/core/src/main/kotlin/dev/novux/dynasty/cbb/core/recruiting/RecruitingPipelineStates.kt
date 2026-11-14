package dev.novux.dynasty.cbb.core.recruiting

/** Regional pipeline adjacency for Recommended ordering ([planning/recruiting-engine.md]). */
object RecruitingPipelineStates {
    fun forHomeState(homeState: String): Set<String> =
        when (homeState) {
            "OH" -> setOf("KY", "IN", "MI", "PA", "WV")
            else -> setOf(homeState)
        }
}
