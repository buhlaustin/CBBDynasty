package dev.novux.dynasty.cbb.core.sim

/** 5:00 OT periods until winner — [planning/simulation-engine.md#possession-loop--clock]. */
object GameSimOvertime {
    fun playUntilWinner(
        session: PossessionSimSession,
        onStep: ((PossessionStep) -> Unit)? = null,
    ) {
        while (session.isTiedAtBuzz()) {
            session.beginOvertimePeriod()
            onStep?.invoke(
                PossessionStep(
                    logLine = session.latestLogLine(),
                    haltedAtHalftime = false,
                    chrome = session.currentWatchChrome(),
                ),
            )
            while (session.hasNext()) {
                val step = session.step()
                onStep?.invoke(step)
            }
        }
        require(!session.isTiedAtBuzz()) {
            "Game cannot end tied after overtime"
        }
    }
}
