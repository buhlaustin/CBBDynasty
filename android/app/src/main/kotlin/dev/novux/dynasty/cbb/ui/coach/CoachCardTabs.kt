package dev.novux.dynasty.cbb.ui.coach

enum class CoachCardTab(val wire: String) {
    Overview("overview"),
    Contract("contract"),
    History("history"),
    HallOfFame("hof"),
    Draft("draft"),
    ;

    companion object {
        val HEAD_COACH_ORDER = listOf(Overview, Contract, History, HallOfFame, Draft)
        val ASSISTANT_ORDER = listOf(Overview, History, Contract)

        fun assistantTabs(hasHeadCoachCareer: Boolean): List<CoachCardTab> =
            if (hasHeadCoachCareer) {
                listOf(Overview, History, Contract, HallOfFame, Draft)
            } else {
                ASSISTANT_ORDER
            }

        fun fromWire(wire: String): CoachCardTab? =
            entries.firstOrNull { it.wire == wire }
    }
}
