package dev.novux.dynasty.cbb.core.player

import org.junit.Assert.assertEquals
import org.junit.Test

class PlayerRatingsExtensionsTest {
    @Test
    fun shootingOverall_averagesSubs() {
        val ratings = PlayerRatings(
            ovr = 80,
            off = 82,
            def = 78,
            shootingInside = 70,
            shootingMid = 73,
            shootingThree = 77,
        )
        assertEquals(73, ratings.shootingOverall())
    }
}
