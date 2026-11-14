package dev.novux.dynasty.cbb.core.recruiting

/**
 * Incoming class seed must not block the UI thread ([MEGA-RECR-131], panel [MEGA-RECR-161]).
 * Implementation: `IncomingClassSeedRepository` — IO for DataStore; Default for seed/encode/decode;
 * `IncomingClassSeedPanel` — explicit `withContext(Dispatchers.IO)` around repository calls.
 */
object IncomingClassSeedIoPolicy {
    const val QA_ID: String = "MEGA-RECR-131"

    const val PANEL_QA_ID: String = "MEGA-RECR-161"

    const val IO_DISPATCHER: String = "Dispatchers.IO"

    const val CPU_DISPATCHER: String = "Dispatchers.Default"

    const val REPOSITORY_CLASS: String =
        "dev.novux.dynasty.cbb.data.preseason.IncomingClassSeedRepository"

    val CPU_BOUND_OPERATIONS: List<String> = listOf(
        "IncomingClassSeeder.seed",
        "encodePool",
        "decodePool",
        "Top60NewsPublisher.publish",
    )
}
