package dev.novux.dynasty.cbb.core.dashboard

import dev.novux.dynasty.cbb.core.player.loyalty.CoachPromiseType
import dev.novux.dynasty.cbb.core.player.loyalty.PromiseStatus
import dev.novux.dynasty.cbb.core.roster.RosterPlayer

/** Optional dashboard nudge when an active promise is at risk ([MEGA-COACH-170]). */
object PromiseAtRiskOptionalDashboardTasks {
    fun build(players: List<RosterPlayer>): List<DashboardTask> =
        players.mapNotNull { player ->
            if (player.promiseStatusWire != PromiseStatus.AtRisk.wire) return@mapNotNull null
            val typeWire = player.promiseTypeWire ?: return@mapNotNull null
            val type = CoachPromiseType.fromWire(typeWire) ?: return@mapNotNull null
            val label = CoachPromiseTypeLabel.short(type, player.promiseStayTopN)
            DashboardTask(
                id = DashboardTaskId.PromiseAtRisk,
                kind = DashboardTaskKind.Optional,
                deepLink = DashboardDeepLink.Strategy,
                stringFormatArgs = listOf(player.displayLabel(), label),
            )
        }

    private object CoachPromiseTypeLabel {
        fun short(type: CoachPromiseType, stayTopN: Int?): String = when (type) {
            CoachPromiseType.MinutesStarter -> "Starter minutes"
            CoachPromiseType.MinutesRotation -> "Rotation minutes"
            CoachPromiseType.MinutesDevelop -> "Development minutes"
            CoachPromiseType.RoleStarter -> "Starter role"
            CoachPromiseType.RoleSixthMan -> "Sixth man role"
            CoachPromiseType.RoleRedshirt -> "Redshirt year"
            CoachPromiseType.StayPledgeLottery -> "Stay unless lottery"
            CoachPromiseType.StayPledgeTopN -> "Stay unless top-${stayTopN ?: 14} mock"
        }
    }
}
