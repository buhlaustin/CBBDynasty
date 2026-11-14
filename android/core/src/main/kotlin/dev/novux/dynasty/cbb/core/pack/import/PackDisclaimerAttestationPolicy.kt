package dev.novux.dynasty.cbb.core.pack.import

/**
 * Pack schema `disclaimerAcceptedRequired` blocks import until UI attestation is checked
 * ([MEGA-GDD-097]).
 */
object PackDisclaimerAttestationPolicy {
    const val MEGA_ID: String = "MEGA-GDD-097"

    fun importBlockedWithoutAttestation(
        disclaimerAcceptedRequired: Boolean,
        attestation: PackImportAttestation,
    ): Boolean = disclaimerAcceptedRequired && !attestation.userAcceptedDisclaimer
}