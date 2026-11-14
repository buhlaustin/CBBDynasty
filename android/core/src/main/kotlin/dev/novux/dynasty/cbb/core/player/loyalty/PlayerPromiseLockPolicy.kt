package dev.novux.dynasty.cbb.core.player.loyalty

/** One promise locked at hard commit; no amend UI ([MEGA-COACH-175]). */
object PlayerPromiseLockPolicy {
    const val MEGA_ID: String = "MEGA-COACH-175"
    const val LOCK_ENGINE: String = "RecruitCommitPromiseLockEngine"
    const val LOCKED_REPO: String = "RecruitLockedPromiseRepository"
    const val PICKER: String = "RecruitPromisePickerSheet"
}
