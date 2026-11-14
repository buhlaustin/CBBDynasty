package dev.novux.dynasty.cbb.core.qa

import java.nio.file.Paths
import kotlin.io.path.readText
import org.junit.Assert.assertTrue
import org.junit.Test

class RsScheduleCapPathTest {
    @Test
    fun leagueSymmetricEngineAndAuditTagged() {
        val root = generateSequence(Paths.get("").toAbsolutePath()) { it.parent }
            .first { it.resolve("android").resolve("settings.gradle.kts").toFile().exists() }
        assertTrue(root.resolve(RsScheduleCapPath.SYMMETRIC_ENGINE).readText().contains(RsScheduleCapPath.MEGA_ID))
        assertTrue(root.resolve(RsScheduleCapPath.INTEGRITY_AUDIT).readText().contains(RsScheduleCapPath.MEGA_ID))
        assertTrue(root.resolve(RsScheduleCapPath.SOLVER).readText().contains("ScheduleWeekAllocator"))
    }
}
