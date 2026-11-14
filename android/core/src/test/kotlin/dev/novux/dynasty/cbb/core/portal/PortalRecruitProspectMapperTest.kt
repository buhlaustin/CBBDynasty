package dev.novux.dynasty.cbb.core.portal

import dev.novux.dynasty.cbb.core.recruiting.RecruitPoolKind
import dev.novux.dynasty.cbb.core.roster.PlayerClassYear
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PortalRecruitProspectMapperTest {
    @Test
    fun maps_entrant_to_portal_prospect() {
        val entrant = PortalEntrant(
            entrantId = "e1",
            priorProgramId = "team-oh-state",
            classYear = PlayerClassYear.Sophomore,
            hiddenRating = 88,
            stars = 4,
            nationalTop40Rank = 3,
            eligibilityClock = PortalEligibilityClock(clockStartSeasonIndex = 1, seasonsOfCompetitionUsed = 1),
        )
        val prospect = PortalRecruitProspectMapper.toRecruitProspect(entrant, "Ohio State")
        assertEquals("e1", prospect.prospectId)
        assertEquals(4, prospect.stars)
        assertEquals(RecruitPoolKind.Portal, prospect.poolKind)
        assertTrue(prospect.displayName.contains("Ohio State"))
        assertEquals(3, prospect.nationalHsRank)
    }
}
