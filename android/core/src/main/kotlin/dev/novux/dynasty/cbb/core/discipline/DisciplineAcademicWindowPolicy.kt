package dev.novux.dynasty.cbb.core.discipline

import dev.novux.dynasty.cbb.core.balance.BalanceDisciplineTuning

/** Academic incident window — RS weeks 18–22 per balance spec ([MEGA-SHELL-072]). */
object DisciplineAcademicWindowPolicy {
    const val MEGA_ID: String = "MEGA-SHELL-072"

    val specDefaultWeeks: List<Int> = listOf(18, 19, 20, 21, 22)

    fun matchesSpecDefault(tuning: BalanceDisciplineTuning): Boolean =
        tuning.occurrence.academicWindowWeeks == specDefaultWeeks
}