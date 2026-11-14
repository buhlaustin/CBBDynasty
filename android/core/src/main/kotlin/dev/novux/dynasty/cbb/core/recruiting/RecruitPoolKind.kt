package dev.novux.dynasty.cbb.core.recruiting

enum class RecruitPoolKind(val wire: String) {
    HighSchool("hs"),
    Juco("juco"),
    Portal("portal"),
    ;

    companion object {
        fun fromWire(value: String): RecruitPoolKind? = entries.firstOrNull { it.wire == value }
    }
}
