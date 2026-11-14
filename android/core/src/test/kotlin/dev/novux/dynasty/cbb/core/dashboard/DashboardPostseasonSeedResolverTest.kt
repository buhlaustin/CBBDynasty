package dev.novux.dynasty.cbb.core.dashboard

import dev.novux.dynasty.cbb.core.selection.NationalTournamentInvite
import dev.novux.dynasty.cbb.core.selection.NcbaBidType
import dev.novux.dynasty.cbb.core.selection.NcbaSelectionEntry
import dev.novux.dynasty.cbb.core.selection.SelectionSundaySnapshot
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class DashboardPostseasonSeedResolverTest {
    @Test
    fun prefersNcbaWhenPresent() {
        val snapshot = SelectionSundaySnapshot(
            seasonIndex = 1,
            ncbaField = listOf(
                NcbaSelectionEntry("u", NcbaBidType.DirectAtLarge, 5, 5.0, 12),
            ),
            nciField = listOf(NationalTournamentInvite("u", "nci", 3)),
            heritageField = emptyList(),
        )
        assertEquals("NCBA #12", DashboardPostseasonSeedResolver.label("u", snapshot))
    }

    @Test
    fun nciWhenNotInNcba() {
        val snapshot = SelectionSundaySnapshot(
            seasonIndex = 1,
            ncbaField = emptyList(),
            nciField = listOf(NationalTournamentInvite("u", "nci", 4)),
            heritageField = emptyList(),
        )
        assertEquals("NCI #4", DashboardPostseasonSeedResolver.label("u", snapshot))
    }

    @Test
    fun heritageWhenOnlyInvite() {
        val snapshot = SelectionSundaySnapshot(
            seasonIndex = 1,
            ncbaField = emptyList(),
            nciField = emptyList(),
            heritageField = listOf(NationalTournamentInvite("u", "hc", 2)),
        )
        assertEquals("HC #2", DashboardPostseasonSeedResolver.label("u", snapshot))
    }

    @Test
    fun noneWhenNotInvited() {
        val snapshot = SelectionSundaySnapshot(
            seasonIndex = 1,
            ncbaField = emptyList(),
            nciField = emptyList(),
            heritageField = emptyList(),
        )
        assertNull(DashboardPostseasonSeedResolver.label("u", snapshot))
    }
}
