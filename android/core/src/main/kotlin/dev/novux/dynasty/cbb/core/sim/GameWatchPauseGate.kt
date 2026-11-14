package dev.novux.dynasty.cbb.core.sim

import kotlin.coroutines.resume
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.suspendCancellableCoroutine

/**
 * Coach mid-game **pause** for watched sim — blocks between possession lines without
 * resetting [PossessionSimSession] state ([planning/simulation-engine.md] playback).
 */
class GameWatchPauseGate {
    @Volatile
    var paused: Boolean = false
        private set

    private val lock = Any()
    private var resumeSignal: CancellableContinuation<Unit>? = null

    fun pause() {
        paused = true
    }

    fun resume() {
        paused = false
        synchronized(lock) {
            resumeSignal?.resume(Unit)
            resumeSignal = null
        }
    }

    suspend fun awaitUnpaused() {
        while (paused) {
            suspendCancellableCoroutine { cont ->
                synchronized(lock) {
                    if (!paused) {
                        cont.resume(Unit)
                    } else {
                        resumeSignal = cont
                    }
                }
            }
        }
    }
}
