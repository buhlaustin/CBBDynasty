package dev.novux.dynasty.cbb.core.roster

/** Shell roster tab sections — [planning/game-frame.md#roster-tab]. */
enum class ShellRosterSection {
    Starters,
    Reserves,
    BenchWarmers,
}

data class ShellRosterSectionGroup(
    val section: ShellRosterSection,
    val players: List<RosterPlayer>,
)

object ShellRosterSectionPartition {
    private const val DEEP_BENCH_DEPTH = 20

    fun group(players: List<RosterPlayer>): List<ShellRosterSectionGroup> {
        val buckets = linkedMapOf(
            ShellRosterSection.Starters to mutableListOf<RosterPlayer>(),
            ShellRosterSection.Reserves to mutableListOf<RosterPlayer>(),
            ShellRosterSection.BenchWarmers to mutableListOf<RosterPlayer>(),
        )
        players.forEach { player ->
            buckets.getValue(sectionFor(player)).add(player)
        }
        return ShellRosterSection.entries.mapNotNull { section ->
            val list = buckets.getValue(section).sortedBy { it.depthOrder }
            if (list.isEmpty()) null else ShellRosterSectionGroup(section, list)
        }
    }

    fun sectionFor(player: RosterPlayer): ShellRosterSection {
        if (player.isStarter) return ShellRosterSection.Starters
        val injuredOut = player.injuryActive && player.injuryWeeksRemaining > 0
        if (injuredOut || player.medicalRedshirtSeason) return ShellRosterSection.Reserves
        if (player.depthOrder >= DEEP_BENCH_DEPTH) return ShellRosterSection.BenchWarmers
        return ShellRosterSection.Reserves
    }
}
