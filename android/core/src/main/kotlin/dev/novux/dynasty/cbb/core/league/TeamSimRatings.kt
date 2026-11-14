package dev.novux.dynasty.cbb.core.league

/** Maps pack prestige to possession-sim team ratings for non-roster opponents. */
object TeamSimRatings {
    fun fromProgramPrestige(programPrestige: Int): Pair<Int, Int> {
        val offensive = (programPrestige + 6).coerceIn(58, 94)
        val defensive = (offensive - 2).coerceAtLeast(50)
        return offensive to defensive
    }
}
