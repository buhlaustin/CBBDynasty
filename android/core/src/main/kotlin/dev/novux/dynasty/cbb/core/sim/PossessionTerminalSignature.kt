package dev.novux.dynasty.cbb.core.sim

/** Extracts comparable terminal event labels from possession log lines (MEGA-SIM-180). */
object PossessionTerminalSignature {
    fun terminalLabel(logLine: String): String {
        val afterScSection = logLine.substringAfter("| SC ", missingDelimiterValue = logLine)
        val afterShotClock = afterScSection.substringAfter(" | ", missingDelimiterValue = afterScSection)
        val withoutPlayer = afterShotClock.substringBefore(" | #").trim()
        val firstSegment = withoutPlayer.substringBefore(" | ").trim()
        return firstSegment.ifBlank { "possession" }
    }

    fun distinctTerminals(logLines: List<String>): Set<String> =
        logLines.map(::terminalLabel).toSet()
}
