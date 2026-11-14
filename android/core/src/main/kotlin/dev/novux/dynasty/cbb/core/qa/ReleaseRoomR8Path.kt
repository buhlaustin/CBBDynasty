package dev.novux.dynasty.cbb.core.qa

/**
 * Room entities / DAOs survive R8 on release ([MEGA-QA-153]).
 */
object ReleaseRoomR8Path {
    const val QA_ID: String = "MEGA-QA-153"

    const val PROGUARD_RULES: String = "android/app/proguard-rules.pro"

    const val TARGET_ADVANCE_WEEKS: Int = 3

    val CRITICAL_ENTITIES: List<String> = listOf(
        "dev.novux.dynasty.cbb.database.save.SaveSlotEntity",
        "dev.novux.dynasty.cbb.database.dynasty.DynastyMetadataEntity",
        "dev.novux.dynasty.cbb.database.calendar.DynastyCalendarEntity",
        "dev.novux.dynasty.cbb.database.calendar.ProvisionalGameEntity",
    )

    const val SMOKE_TEST_CLASS: String =
        "dev.novux.dynasty.cbb.database.qa.ReleaseRoomR8SmokeTest"
}
