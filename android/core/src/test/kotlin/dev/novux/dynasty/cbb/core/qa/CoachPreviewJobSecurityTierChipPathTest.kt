package dev.novux.dynasty.cbb.core.qa

import dev.novux.dynasty.cbb.core.coach.security.JobSecurityPreviewTierChipPolicy
import java.nio.file.Paths
import kotlin.io.path.readText
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CoachPreviewJobSecurityTierChipPathTest {
    @Test
    fun userHcPreviewUsesTierChipNotFullMeter() {
        val root = generateSequence(Paths.get("").toAbsolutePath()) { it.parent }
            .first { it.resolve("android").resolve("settings.gradle.kts").toFile().exists() }
        val chip = root.resolve(CoachPreviewJobSecurityTierChipPath.CHIP).readText()
        assertTrue(chip.contains(CoachPreviewJobSecurityTierChipPath.MEGA_ID))
        assertTrue(chip.contains(JobSecurityPreviewTierChipPolicy.COMPOSABLE))
        val preview = root.resolve(CoachPreviewJobSecurityTierChipPath.PREVIEW).readText()
        assertTrue(preview.contains("MEGA-COACH-188"))
        assertTrue(preview.contains("JOB_SECURITY_CHIP_QA"))
        assertTrue(preview.contains("JobSecurityTierChip"))
        assertFalse(preview.contains("JobSecurityMeter(it)"))
    }
}
