package dev.novux.dynasty.cbb.core.player.nil

/**
 * Program NIL valuation on player Overview ([MEGA-COACH-064]) — annual USD committed at signing.
 */
object PlayerNilValuationPolicy {
    const val QA_ID: String = "MEGA-COACH-064"

    const val IDENTITY_FIELD: String = "PlayerIdentity.nilUsdPerYear"

    const val OVERVIEW_COMPOSABLE: String = "PlayerNilValuationLine"
}
