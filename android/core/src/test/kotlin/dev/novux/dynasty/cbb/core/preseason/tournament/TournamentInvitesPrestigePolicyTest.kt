package dev.novux.dynasty.cbb.core.preseason.tournament

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TournamentInvitesPrestigePolicyTest {
    @Test
    fun tier_boundaries() {
        assertEquals(TournamentInvitesPrestigeTier.Low, TournamentInvitesPrestigePolicy.tier(29))
        assertEquals(TournamentInvitesPrestigeTier.Mid, TournamentInvitesPrestigePolicy.tier(30))
        assertEquals(TournamentInvitesPrestigeTier.Mid, TournamentInvitesPrestigePolicy.tier(54))
        assertEquals(TournamentInvitesPrestigeTier.High, TournamentInvitesPrestigePolicy.tier(55))
    }

    @Test
    fun highTierCanRollMultipleInvites() {
        assertTrue(TournamentInvitesPrestigePolicy.offersMultipleInvites(70))
        assertFalse(TournamentInvitesPrestigePolicy.offersMultipleInvites(40))
    }

    @Test
    fun megaFrameId() {
        assertEquals("MEGA-FRAME-074", TournamentInvitesPrestigePolicy.MEGA_FRAME_ID)
    }
}
