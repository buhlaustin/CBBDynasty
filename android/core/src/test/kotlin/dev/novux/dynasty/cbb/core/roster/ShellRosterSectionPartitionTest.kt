package dev.novux.dynasty.cbb.core.roster

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ShellRosterSectionPartitionTest {
    private fun player(
        id: String,
        starter: Boolean,
        depth: Int,
        injured: Boolean = false,
    ) = RosterPlayer(
        playerId = id,
        dynastyId = "d1",
        position = PlayerPosition.ShootingGuard,
        classYear = PlayerClassYear.Sophomore,
        isStarter = starter,
        depthOrder = depth,
        inTransferPortal = false,
        injuryActive = injured,
        injuryWeeksRemaining = if (injured) 2 else 0,
    )

    @Test
    fun partitions_starters_reserves_and_bench() {
        val grouped = ShellRosterSectionPartition.group(
            listOf(
                player("s1", starter = true, depth = 1),
                player("r1", starter = false, depth = 6),
                player("b1", starter = false, depth = 22),
            ),
        )
        assertEquals(3, grouped.size)
        assertEquals(listOf("s1"), grouped[0].players.map { it.playerId })
        assertEquals(ShellRosterSection.Starters, grouped[0].section)
        assertEquals(listOf("r1"), grouped[1].players.map { it.playerId })
        assertEquals(listOf("b1"), grouped[2].players.map { it.playerId })
    }

    @Test
    fun injured_deep_bench_stays_in_reserves() {
        val injured = player("inj", starter = false, depth = 25, injured = true)
        assertEquals(ShellRosterSection.Reserves, ShellRosterSectionPartition.sectionFor(injured))
        val grouped = ShellRosterSectionPartition.group(listOf(injured))
        assertTrue(grouped.single().section == ShellRosterSection.Reserves)
    }
}
