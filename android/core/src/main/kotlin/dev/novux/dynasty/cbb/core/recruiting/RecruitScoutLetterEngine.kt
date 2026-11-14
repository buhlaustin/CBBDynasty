package dev.novux.dynasty.cbb.core.recruiting

import dev.novux.dynasty.cbb.core.player.scout.ScoutDepth
import dev.novux.dynasty.cbb.core.player.scout.ScoutLetterScale
import kotlin.random.Random

/**
 * Derives A–F category letters from hidden 0–99 ratings without exposing integers in UI.
 */
object RecruitScoutLetterEngine {
    const val MEGA_ID: String = "MEGA-RECR-013"

    fun fromProspect(prospect: RecruitProspect, depth: ScoutDepth = ScoutDepth.Light): RecruitScoutLetters =
        fromHiddenRating(prospect.hiddenRating, prospect.prospectId, depth)

    fun fromHiddenRating(hiddenRating: Int, prospectId: String, depth: ScoutDepth = ScoutDepth.Light): RecruitScoutLetters {
        val fuzz = depth.fuzzSteps
        val random = Random(prospectId.hashCode().toLong() * 31_337)
        val ovr = hiddenRating.coerceIn(0, 99)
        val off = (ovr + random.nextInt(-10, 11)).coerceIn(0, 99)
        val def = (ovr + random.nextInt(-12, 13)).coerceIn(0, 99)
        val shooting = (ovr + random.nextInt(-14, 15)).coerceIn(0, 99)
        val handling = (ovr + random.nextInt(-11, 12)).coerceIn(0, 99)
        val athletic = (ovr + random.nextInt(-9, 10)).coerceIn(0, 99)
        val potential = (ovr + random.nextInt(-6, 19)).coerceIn(0, 99)
        return RecruitScoutLetters(
            overall = ScoutLetterScale.fromRating(ovr, fuzz),
            offense = ScoutLetterScale.fromRating(off, fuzz),
            defense = ScoutLetterScale.fromRating(def, fuzz),
            shooting = ScoutLetterScale.fromRating(shooting, fuzz),
            handling = ScoutLetterScale.fromRating(handling, fuzz),
            athleticism = ScoutLetterScale.fromRating(athletic, fuzz),
            potential = ScoutLetterScale.fromRating(potential, fuzz + 1),
        )
    }
}
