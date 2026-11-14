package dev.novux.dynasty.cbb.core.recruiting

import dev.novux.dynasty.cbb.core.player.loyalty.CoachPromise
import dev.novux.dynasty.cbb.core.player.loyalty.CoachPromiseType

/** Coach promise chosen at scholarship offer ([MEGA-COACH-087], [planning/player-loyalty-promises.md]). */
data class RecruitOfferPromise(
    val prospectId: String,
    val promiseType: CoachPromiseType,
    val stayTopN: Int? = null,
) {
    fun displayLabel(): String = CoachPromise(
        type = promiseType,
        stayTopN = stayTopN,
        lockedAtSeasonIndex = null,
    ).displayLabel()
}

object RecruitOfferPromisePolicy {
    const val QA_ID: String = "MEGA-COACH-087"

    const val PICKER_COMPOSABLE: String = "RecruitPromisePickerSheet"

    val OFFERABLE_TYPES: List<CoachPromiseType> = listOf(
        CoachPromiseType.MinutesStarter,
        CoachPromiseType.MinutesRotation,
        CoachPromiseType.MinutesDevelop,
        CoachPromiseType.RoleStarter,
        CoachPromiseType.RoleSixthMan,
        CoachPromiseType.RoleRedshirt,
        CoachPromiseType.StayPledgeLottery,
        CoachPromiseType.StayPledgeTopN,
    )
}
