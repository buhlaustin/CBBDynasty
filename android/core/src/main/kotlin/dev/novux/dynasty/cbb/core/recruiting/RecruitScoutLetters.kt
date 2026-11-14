package dev.novux.dynasty.cbb.core.recruiting

/**
 * Letter-grade scout rows on recruit sheets ([MEGA-RECR-013], [planning/recruiting-engine.md]).
 */
data class RecruitScoutLetters(
    val overall: String,
    val offense: String,
    val defense: String,
    val shooting: String,
    val handling: String,
    val athleticism: String,
    val potential: String,
) {
    fun asRows(): List<Pair<RecruitScoutLetterCategory, String>> = listOf(
        RecruitScoutLetterCategory.Overall to overall,
        RecruitScoutLetterCategory.Offense to offense,
        RecruitScoutLetterCategory.Defense to defense,
        RecruitScoutLetterCategory.Shooting to shooting,
        RecruitScoutLetterCategory.Handling to handling,
        RecruitScoutLetterCategory.Athleticism to athleticism,
        RecruitScoutLetterCategory.Potential to potential,
    )
}

enum class RecruitScoutLetterCategory {
    Overall,
    Offense,
    Defense,
    Shooting,
    Handling,
    Athleticism,
    Potential,
}
