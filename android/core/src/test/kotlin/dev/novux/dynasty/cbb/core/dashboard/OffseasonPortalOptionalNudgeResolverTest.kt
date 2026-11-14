package dev.novux.dynasty.cbb.core.dashboard

import dev.novux.dynasty.cbb.core.pack.IdentityPack
import dev.novux.dynasty.cbb.core.pack.IdentityPackCodec
import dev.novux.dynasty.cbb.core.pack.IdentityPackIndex
import dev.novux.dynasty.cbb.core.pack.PackLoadResult
import dev.novux.dynasty.cbb.core.portal.PortalEligibilityClock
import dev.novux.dynasty.cbb.core.portal.PortalEntrant
import dev.novux.dynasty.cbb.core.roster.PlayerClassYear
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class OffseasonPortalOptionalNudgeResolverTest {
    private val pack: IdentityPack
        get() {
            val json = javaClass.classLoader.getResourceAsStream("pack/minimal-valid.json")!!
                .bufferedReader().readText()
            return (IdentityPackCodec.parseAndValidate(json) as PackLoadResult.Success).pack
        }

    @Test
    fun pipelineSpotlight_prefersHomeStateMatch() {
        val index = IdentityPackIndex.from(pack)
        val team = index.allTeamDisplays().first()
        val entrant = PortalEntrant(
            entrantId = "e1",
            priorProgramId = team.teamId,
            classYear = PlayerClassYear.Junior,
            hiddenRating = 80,
            stars = 4,
            nationalTop40Rank = 5,
            eligibilityClock = PortalEligibilityClock(clockStartSeasonIndex = 1, seasonsOfCompetitionUsed = 1),
        )
        val spotlight = OffseasonPortalOptionalNudgeResolver.pipelineSpotlight(
            index,
            listOf(entrant),
            userPipelineStates = setOf(team.homeState),
        )
        assertNotNull(spotlight)
        assertEquals(team.displayName, spotlight!!.priorSchoolDisplayName)
    }
}
