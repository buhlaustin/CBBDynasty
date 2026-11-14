package dev.novux.dynasty.cbb.core.signingday

/**
 * Hard sign closes the recruiting cycle for the season ([MEGA-RECR-051]).
 */
object SigningDayCycleClosePolicy {
    const val MEGA_ID: String = "MEGA-RECR-051"

    fun cycleClosedAfterHardSign(progress: SigningDayProgress?): Boolean =
        progress?.hardSignComplete == true
}