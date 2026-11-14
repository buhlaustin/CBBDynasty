package dev.novux.dynasty.cbb.core.sim

import dev.novux.dynasty.cbb.core.qa.PossessionSubPhasePath
import dev.novux.dynasty.cbb.core.strategy.ResolvedSchemeEffects
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class PossessionSubPhaseEngineTest {
  @Test
  fun megaSim089_shotTerminalIncludesDriveKickShotChain() {
    assertEquals(PossessionSubPhasePath.SIM_ID, "MEGA-SIM-089")
    val effects = ResolvedSchemeEffects(
      tripPaceScale = 1.0,
      threePointAttemptRate = 0.38,
      turnoverChance = 0.01,
      makeRateAdjust = 0.2,
      offensiveRatingDelta = 0,
      defensiveRatingDelta = 0,
    )
    val random = kotlin.random.Random(77)
    var sawShotChain = false
    repeat(80) {
      val result = PossessionSubPhaseEngine.resolve(
        offense = TeamBoxScore(teamId = "home"),
        offRating = 80,
        defRating = 70,
        effects = effects,
        random = random,
      )
      val chain = result.subPhaseChain
      assertNotNull(chain)
      if (chain!!.startsWith("drive→kick→shot")) {
        sawShotChain = true
      }
    }
    assertTrue(sawShotChain)
  }

  @Test
  fun earlyDriveTurnoverTagsChain() {
    val effects = ResolvedSchemeEffects(
      tripPaceScale = 1.0,
      threePointAttemptRate = 0.38,
      turnoverChance = 0.90,
      makeRateAdjust = 0.0,
      offensiveRatingDelta = 0,
      defensiveRatingDelta = 0,
    )
    var driveToChain: String? = null
    for (seed in 0 until 64) {
      val result = PossessionSubPhaseEngine.resolve(
        offense = TeamBoxScore(teamId = "home"),
        offRating = 70,
        defRating = 70,
        effects = effects,
        random = kotlin.random.Random(seed.toLong()),
      )
      if (result.subPhaseChain?.startsWith("drive→TO") == true) {
        driveToChain = result.subPhaseChain
        break
      }
    }
    assertNotNull(driveToChain)
    assertTrue(driveToChain!!.startsWith("drive→TO"))
  }
}
