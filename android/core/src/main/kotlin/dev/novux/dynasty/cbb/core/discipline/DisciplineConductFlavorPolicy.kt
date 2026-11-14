package dev.novux.dynasty.cbb.core.discipline

import dev.novux.dynasty.cbb.core.balance.BalanceConductTemplate

/** Conduct incident copy comes from balance templates ([MEGA-SHELL-071]). */
object DisciplineConductFlavorPolicy {
    const val MEGA_ID: String = "MEGA-SHELL-071"

    fun headline(template: BalanceConductTemplate): String = "Team discipline — ${template.label}"

    fun flavorBody(templateId: String, playerDisplayName: String): String =
        when (templateId) {
            "arrest" -> "$playerDisplayName was arrested last night."
            "campus_conduct" -> "$playerDisplayName was involved in an on-campus incident."
            "team_rules" -> "$playerDisplayName violated team rules."
            else -> "$playerDisplayName is under conduct review."
        }
}