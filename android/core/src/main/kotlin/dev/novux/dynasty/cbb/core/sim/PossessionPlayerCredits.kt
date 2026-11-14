package dev.novux.dynasty.cbb.core.sim

/** On-floor players credited on this possession trip ([MEGA-SIM-097]). */
data class PossessionPlayerCredits(
    val scorerId: String? = null,
    val assistId: String? = null,
    val rebounderId: String? = null,
    val freeThrowShooterId: String? = null,
    val turnoverHandlerId: String? = null,
)
