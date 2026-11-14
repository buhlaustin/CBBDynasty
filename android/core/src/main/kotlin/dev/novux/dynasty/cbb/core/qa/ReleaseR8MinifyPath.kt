package dev.novux.dynasty.cbb.core.qa

/**
 * Release R8 / minify gate ([MEGA-QA-151]).
 */
object ReleaseR8MinifyPath {
    const val QA_ID: String = "MEGA-QA-151"

    const val GRADLE_FILE: String = "android/app/build.gradle.kts"

    const val BUNDLE_TASK: String = ":app:bundleRelease"

    const val MAPPING_OUTPUT: String = "android/app/build/outputs/mapping/release/mapping.txt"

    const val PROGUARD_RULES: String = "android/app/proguard-rules.pro"

    val REQUIRED_GRADLE_FLAGS: List<String> = listOf(
        "isMinifyEnabled = true",
        "isShrinkResources = true",
    )
}
