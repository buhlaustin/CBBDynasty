package dev.novux.dynasty.cbb.core.qa

/**
 * Release AAB signing integrity before Play upload ([MEGA-QA-137]).
 * Prevents `INSTALL_PARSE_FAILED` from unsigned or corrupt bundles.
 */
object PlaySignedAabIntegrityPath {
    const val QA_ID: String = "MEGA-QA-137"

    const val VERIFY_SCRIPT: String = "scripts/verify_play_aab_signing.sh"

    const val CI_WORKFLOW: String = "android-release-universal-smoke.yml"

    const val PLAY_BUNDLE_TASK: String = ":app:bundleRelease"

    data class Check(val id: String, val tool: String, val failureSignal: String)

    val AUTOMATED_CHECKS: List<Check> = listOf(
        Check("bundletool_validate", "bundletool validate --bundle", "Invalid bundle manifest"),
        Check("jarsigner_verify", "jarsigner -verify -strict", "Unsigned or tampered AAB"),
        Check("apksigner_verify", "apksigner verify", "APK signing block invalid"),
    )

    const val HUMAN_PLAY_CONSOLE: String =
        "Play Console → App integrity → Play App Signing enabled; upload key matches CI secrets"
}
