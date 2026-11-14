package dev.novux.dynasty.cbb.core.carousel

import dev.novux.dynasty.cbb.core.coach.contract.HeadCoachExtensionOfferEngine
import java.util.UUID

/** User-facing carousel cards after league background churn ([MEGA-GDD-119]). */
object CarouselUserApprovalPlanner {
    const val MEGA_ID: String = "MEGA-GDD-119"

    fun pendingCards(
        context: CarouselSimContext,
        background: CarouselLeagueBackgroundSimEngine.CarouselBackgroundSimResult,
    ): List<CarouselApprovalCard> {
        if (context.userUnemployed || background.userRequiresHeadCoachOffer) {
            return listOf(
                CarouselApprovalCatalog.card(
                    id = stableId(context, "hc-offer"),
                    kind = CarouselApprovalKind.HeadCoachOffer,
                ),
            )
        }

        val card = context.userHeadCoach
        val security = context.userSecurity
        if (card != null && security != null) {
            val extension = HeadCoachExtensionOfferEngine.incoming(
                card = card,
                security = security,
                dismissedForSeasonIndex = null,
                currentSeasonIndex = context.seasonIndex,
            )
            if (extension != null) {
                return listOf(
                    CarouselApprovalCatalog.card(
                        id = stableId(context, "hc-ext"),
                        kind = CarouselApprovalKind.ContractExtension,
                    ),
                )
            }
        }

        val assistantRoll = (context.dynastyId.hashCode() + context.seasonIndex * 719).mod(100)
        return if (assistantRoll < 40 && card != null) {
            listOf(
                CarouselApprovalCatalog.card(
                    id = stableId(context, "asst-hire"),
                    kind = CarouselApprovalKind.AssistantHire,
                ),
            )
        } else {
            emptyList()
        }
    }

    private fun stableId(context: CarouselSimContext, suffix: String): String =
        UUID.nameUUIDFromBytes("${context.dynastyId}:${context.seasonIndex}:$suffix".toByteArray())
            .toString()
}
