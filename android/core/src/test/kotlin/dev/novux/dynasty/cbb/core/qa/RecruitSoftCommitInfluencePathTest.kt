package dev.novux.dynasty.cbb.core.qa

import java.nio.file.Paths
import kotlin.io.path.readText
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RecruitSoftCommitInfluencePathTest {
    @Test
    fun resolverGatesOnInfluencePolicy() {
        assertEquals("MEGA-RECR-029", RecruitSoftCommitInfluencePath.RECR_ID)
        val root = generateSequence(Paths.get("").toAbsolutePath()) { it.parent }
            .first { it.resolve("android").resolve("settings.gradle.kts").toFile().exists() }
        val resolver = root.resolve(RecruitSoftCommitInfluencePath.RESOLVER_SOURCE).readText()
        val policy = root.resolve(RecruitSoftCommitInfluencePath.POLICY_SOURCE).readText()
        assertTrue(resolver.contains("incomingSoftCommits"))
        assertTrue(resolver.contains(RecruitSoftCommitInfluencePath.QUALIFIES_METHOD))
        assertTrue(policy.contains("REQUIRED_USER_RANK"))
        assertTrue(policy.contains("MIN_CLOSENESS"))
    }
}
