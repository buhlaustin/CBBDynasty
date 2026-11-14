package dev.novux.dynasty.cbb.core.qa

import dev.novux.dynasty.cbb.core.balance.BalanceDisciplineTuning
import dev.novux.dynasty.cbb.core.chemistry.ChemistryWeeklyTickInputPolicy
import dev.novux.dynasty.cbb.core.discipline.DisciplineAcademicWindowPolicy
import dev.novux.dynasty.cbb.core.discipline.DisciplineConductFlavorPolicy
import dev.novux.dynasty.cbb.core.fatigue.FatiguePressBackToBackPolicy
import dev.novux.dynasty.cbb.core.notification.NotificationFeedOffseasonPhasePolicy
import dev.novux.dynasty.cbb.core.shell.LaunchResumeSkipStudioPolicy
import dev.novux.dynasty.cbb.core.sim.SimChemistryClutchPolicy
import dev.novux.dynasty.cbb.core.sim.SimFatigueRotationPolicy
import java.nio.file.Paths
import kotlin.io.path.readText
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MegaBatch22PathTest {
    @Test
    fun disciplineChemistryFatigueBatchWired() {
        assertEquals("MEGA-SHELL-056", NotificationFeedOffseasonPhasePolicy.MEGA_ID)
        assertEquals("MEGA-SHELL-071", DisciplineConductFlavorPolicy.MEGA_ID)
        assertEquals("MEGA-SHELL-072", DisciplineAcademicWindowPolicy.MEGA_ID)
        assertEquals("MEGA-SHELL-077", SimChemistryClutchPolicy.MEGA_ID)
        assertEquals("MEGA-SHELL-078", ChemistryWeeklyTickInputPolicy.MEGA_ID)
        assertEquals("MEGA-SHELL-082", SimFatigueRotationPolicy.SHELL_MEGA_ID)
        assertEquals("MEGA-SHELL-083", FatiguePressBackToBackPolicy.MEGA_ID)
        assertEquals("MEGA-SHELL-085", LaunchResumeSkipStudioPolicy.MEGA_ID)
        assertTrue(
            DisciplineAcademicWindowPolicy.matchesSpecDefault(
                BalanceDisciplineTuning(),
            ),
        )
        assertTrue(LaunchResumeSkipStudioPolicy.shouldBypassStudioBeat(skipStudioIntroSetting = true, studioBeatConsumed = false))
        val engine = repoRoot().resolve(
            "android/core/src/main/kotlin/dev/novux/dynasty/cbb/core/discipline/DisciplineOccurrenceEngine.kt",
        ).readText()
        assertTrue(engine.contains("DisciplineConductFlavorPolicy"))
        val session = repoRoot().resolve(
            "android/core/src/main/kotlin/dev/novux/dynasty/cbb/core/sim/PossessionSimSession.kt",
        ).readText()
        assertTrue(session.contains("SimChemistryClutchPolicy"))
        val save = repoRoot().resolve(
            "android/app/src/main/kotlin/dev/novux/dynasty/cbb/data/save/SaveSlotRepository.kt",
        ).readText()
        assertTrue(save.contains("ChemistryWeeklyTickInputPolicy"))
        assertTrue(save.contains("resolveSchemesForFatigueTick"))
        val app = repoRoot().resolve(
            "android/app/src/main/kotlin/dev/novux/dynasty/cbb/ui/DynastyApp.kt",
        ).readText()
        assertTrue(app.contains("LaunchResumeSkipStudioPolicy"))
        assertTrue(app.contains("studioBeatConsumed"))
    }

    private fun repoRoot() =
        generateSequence(Paths.get("").toAbsolutePath()) { it.parent }
            .first { it.resolve("android").resolve("settings.gradle.kts").toFile().exists() }
}