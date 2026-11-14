package dev.novux.dynasty.cbb.core.qa

/**
 * Google Play **update over prior open-test build** ([MEGA-QA-136]).
 */
object PlayUpdateOverPriorPath {
    const val QA_ID: String = "MEGA-QA-136"

    /**
     * Last version code published to open testing; current `versionCode` in `app/build.gradle.kts`
     * must be strictly greater (`android/play_open_test_baseline_version_code.txt`).
     */
    const val BASELINE_FILE: String = "android/play_open_test_baseline_version_code.txt"

    data class UpdateStep(val id: String, val action: String, val passWhen: String)

    val OPEN_TESTING_UPDATE_FLOW: List<UpdateStep> = listOf(
        UpdateStep(
            "prior_build_installed",
            "Device has prior open-test build with occupied save",
            "At least one dynasty in progress",
        ),
        UpdateStep(
            "play_update",
            "Play Store → Update to new versionCode",
            "Update installs without INSTALL_PARSE_FAILED",
        ),
        UpdateStep(
            "migration",
            "First launch after update",
            "No migration crash; Room reaches current schema",
        ),
        UpdateStep(
            "continue",
            "Home → Continue (or Load slot)",
            "Dynasty loads; calendar phase/week coherent",
        ),
    )

    val CI_AUTOMATION: List<String> = listOf(
        "DynastyDatabaseMigrationChainTest",
        "DynastyDatabaseMigrationRobolectricTest",
        "DynastyDatabaseMigrationRobolectricTest.playUpdateOccupiedSlot_fullMigrationChain_preservesDynastyIdForContinue",
        "SaveLoadAfterUpdateFixtureTest",
        "verify_play_update_version_bump.sh",
    )
}
