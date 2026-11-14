package dev.novux.dynasty.cbb.core.pack

sealed class PackValidationError {
    data class MalformedJson(val detail: String) : PackValidationError()

    data class UnsupportedPackVersion(val version: Int) : PackValidationError()

    data class InvalidMembershipYear(val year: String) : PackValidationError()

    data class EmptyCollection(val field: String) : PackValidationError()

    data class DuplicateId(val field: String, val id: String) : PackValidationError()

    data class BlankId(val field: String) : PackValidationError()

    data class UnknownConference(val teamId: String, val conferenceId: String) : PackValidationError()

    data class UnknownTournamentTemplate(val conferenceId: String, val templateId: String) : PackValidationError()

    data class InvalidTemplate(
        val templateId: String,
        val detail: String,
    ) : PackValidationError()

    data class InvalidConference(
        val conferenceId: String,
        val detail: String,
    ) : PackValidationError()

    data class InvalidTeam(
        val teamId: String,
        val detail: String,
    ) : PackValidationError()

    data class InvalidManifestSignature(val detail: String) : PackValidationError()
}

sealed class PackLoadResult {
    data class Success(val pack: IdentityPack) : PackLoadResult()

    data class Rejected(val error: PackValidationError) : PackLoadResult()
}
