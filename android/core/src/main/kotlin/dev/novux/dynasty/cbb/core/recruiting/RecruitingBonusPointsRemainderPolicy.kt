package dev.novux.dynasty.cbb.core.recruiting

/**
 * Bonus points spend **any partial remainder** ≤ weekly balance — not an implicit full dump
 * ([planning/recruiting-engine.md#bonus-points], MEGA-RECR-140 / RECR-023).
 */
object RecruitingBonusPointsRemainderPolicy {
    const val QA_ID: String = "MEGA-RECR-140"

    fun coerceUserAmount(raw: Int, pointsAvailable: Int): Int? {
        if (pointsAvailable <= 0) return null
        if (raw <= 0 || raw > pointsAvailable) return null
        return raw
    }

    fun parseUserInput(text: String, pointsAvailable: Int): Int? {
        val trimmed = text.trim()
        if (trimmed.isEmpty()) return null
        val parsed = trimmed.toIntOrNull() ?: return null
        return coerceUserAmount(parsed, pointsAvailable)
    }
}
