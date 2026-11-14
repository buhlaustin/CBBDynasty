package dev.novux.dynasty.cbb.core.signingday

/**
 * Hard-sign class rows carry persisted NIL offers on each [IncomingSignee] ([MEGA-RECR-098]).
 */
object SigningDayNilSnapshotPolicy {
    const val MEGA_ID: String = "MEGA-RECR-098"

    fun applyNilOffer(signee: IncomingSignee, nilOfferUsd: Int): IncomingSignee =
        signee.copy(nilSignedUsd = nilOfferUsd.coerceAtLeast(0))
}