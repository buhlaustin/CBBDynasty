package dev.novux.dynasty.cbb.core.qa

import dev.novux.dynasty.cbb.core.pack.PackMembershipYearPolicy
import java.nio.file.Paths
import kotlin.io.path.readText
import org.junit.Assert.assertTrue
import org.junit.Test

/** [MEGA-GDD-019] */
class PackMembershipYearLoadPathTest {
    @Test
    fun dynastyLoadAndImportSurfaceMembershipYearMismatch() {
        val root = repoRoot()
        val app = root.resolve(APP).readText()
        val repo = root.resolve(REPO).readText()
        val policy = root.resolve(POLICY).readText()
        assertTrue(policy.contains(PackMembershipYearPolicy.MEGA_GDD_ID))
        assertTrue(repo.contains("recordMembershipYearAtSign"))
        assertTrue(app.contains("pack_import_membership_year_warning"))
        assertTrue(app.contains("packDynastyIdentityRepository"))
    }

    private fun repoRoot() =
        generateSequence(Paths.get("").toAbsolutePath()) { it.parent }
            .first { it.resolve("android").resolve("settings.gradle.kts").toFile().exists() }

    companion object {
        private const val APP = "android/app/src/main/kotlin/dev/novux/dynasty/cbb/ui/DynastyApp.kt"
        private const val REPO =
            "android/app/src/main/kotlin/dev/novux/dynasty/cbb/data/pack/PackDynastyIdentityRepository.kt"
        private const val POLICY =
            "android/core/src/main/kotlin/dev/novux/dynasty/cbb/core/pack/PackMembershipYearPolicy.kt"
    }
}