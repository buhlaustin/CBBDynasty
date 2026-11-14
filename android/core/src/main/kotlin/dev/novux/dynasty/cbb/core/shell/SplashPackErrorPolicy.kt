package dev.novux.dynasty.cbb.core.shell

/** Stay on splash with recoverable pack/index failure ([MEGA-SHELL-012]). */
object SplashPackErrorPolicy {
    const val MEGA_ID: String = "MEGA-SHELL-012"

    fun shouldOfferRetry(loadFailed: Boolean): Boolean = loadFailed
}