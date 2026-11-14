package dev.novux.dynasty.cbb.core.sim.tuning

import dev.novux.dynasty.cbb.core.qa.SimEventWeightsPath
import dev.novux.dynasty.cbb.core.sim.PossessionSimEngine
import dev.novux.dynasty.cbb.core.sim.GameSimInput
import dev.novux.dynasty.cbb.core.sim.GameSimTeamInput
import kotlin.io.path.readText
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SimEventWeightsResolverTest {
    @Test
    fun megaSim099_qaAnchor() {
        assertEquals(SimEventWeightsPath.SIM_ID, SimEventWeightsResolver.QA_ID)
    }

    @Test
    fun assetEventWeights_parseAndValidate() {
        val raw = java.nio.file.Path.of(System.getProperty("user.dir")).toAbsolutePath()
            .let { start ->
                generateSequence(start) { it.parent }
                    .first { it.resolve("assets/sim/sim-tuning.json").toFile().exists() }
            }
            .resolve("assets/sim/sim-tuning.json")
            .readText()
        val result = SimTuningCodec.parseAndValidate(raw)
        assertTrue(result is SimTuningLoadResult.Success)
        val tuning = (result as SimTuningLoadResult.Success).tuning
        val weights = SimEventWeightsResolver.resolve(tuning.eventWeights)
        assertEquals(0.42, weights.ratingGapMakeRate.base, 0.0001)
        assertEquals(0.28, weights.offensiveReboundRate, 0.0001)
        assertNull(SimEventWeightsResolver.validate(weights))
    }

    @Test
    fun highMakeRateTuning_increasesScoring() {
        val highMake = buildJsonObject {
            put(
                "ratingGapMakeRate",
                buildJsonObject {
                    put("base", JsonPrimitive(0.62))
                    put("max", JsonPrimitive(0.95))
                },
            )
        }
        val lowMake = buildJsonObject {
            put(
                "ratingGapMakeRate",
                buildJsonObject {
                    put("base", JsonPrimitive(0.22))
                    put("max", JsonPrimitive(0.35))
                },
            )
        }
        val baseTuning = SimTuningCodec.parseAndValidate(
            java.nio.file.Files.readString(
                java.nio.file.Path.of(System.getProperty("user.dir")).toAbsolutePath()
                    .let { start ->
                        generateSequence(start) { it.parent }
                            .first { it.resolve("assets/sim/sim-tuning.json").toFile().exists() }
                    }
                    .resolve("assets/sim/sim-tuning.json"),
            ),
        ).let { (it as SimTuningLoadResult.Success).tuning }
        fun simWith(event: JsonObject) = PossessionSimEngine.simulate(
            GameSimInput(
                home = GameSimTeamInput("h", 80, 70),
                away = GameSimTeamInput("a", 70, 80),
                tuning = baseTuning.copy(eventWeights = event),
                seed = 99L,
            ),
        )
        val high = (0 until 30).sumOf { simWith(highMake).homeBox.points + simWith(highMake).awayBox.points }
        val low = (0 until 30).sumOf { simWith(lowMake).homeBox.points + simWith(lowMake).awayBox.points }
        assertTrue(high > low)
    }
}
