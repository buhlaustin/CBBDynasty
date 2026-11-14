package dev.novux.dynasty.cbb.core.recruiting

/**
 * Deterministic small pools for unit tests — delegates to [IncomingClassSeeder] so national
 * Top 60 and star bands match the persisted preseason seed path.
 */
object RecruitingClassSeed {
    fun demoPool(membershipSeasonIndex: Int, size: Int = 120): List<RecruitProspect> {
        val hsCount = (size * 2) / 3
        val jucoCount = (size - hsCount).coerceAtLeast(0)
        return IncomingClassSeeder.seed(
            enrollingSeasonIndex = membershipSeasonIndex,
            hsCount = hsCount,
            jucoCount = jucoCount,
            seedKey = membershipSeasonIndex.toLong() * 7_919,
        )
    }
}
