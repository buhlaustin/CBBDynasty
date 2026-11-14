package dev.novux.dynasty.cbb.core.recruiting

import dev.novux.dynasty.cbb.core.balance.BalanceLoadResult
import dev.novux.dynasty.cbb.core.balance.BalanceRecruitingRecommendedParser
import dev.novux.dynasty.cbb.core.balance.BalanceRulesetCodec
import dev.novux.dynasty.cbb.core.roster.PlayerPosition
import dev.novux.dynasty.cbb.core.signingday.SigningDaySoftCommitInputs
import dev.novux.dynasty.cbb.core.signingday.SigningDaySoftCommitResolver
import dev.novux.dynasty.cbb.core.recruiting.RecruitingRefundableAction
import dev.novux.dynasty.cbb.core.recruiting.RecruitingWeekReservation
import java.nio.file.Path
import kotlin.io.path.readText
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RecruitSoftCommitPolicyTest {
    private val recommended = loadRecommended()

    @Test
    fun offerAlone_doesNotQualifyWithoutRankOne() {
        val board = ProspectInfluenceBoard(
            closeness = 85,
            userProgramId = "user-1",
            userRank = 2,
            leaders = emptyList(),
        )
        assertFalse(RecruitSoftCommitPolicy.qualifies(board, hasScholarshipOffer = true))
    }

    @Test
    fun rankOneWithoutCloseness_doesNotQualify() {
        val board = ProspectInfluenceBoard(
            closeness = 50,
            userProgramId = "user-1",
            userRank = 1,
            leaders = emptyList(),
        )
        assertFalse(RecruitSoftCommitPolicy.qualifies(board, hasScholarshipOffer = true))
    }

    @Test
    fun rankOneAndCloseness_withOffer_qualifies() {
        val board = ProspectInfluenceBoard(
            closeness = RecruitSoftCommitPolicy.MIN_CLOSENESS,
            userProgramId = "user-1",
            userRank = RecruitSoftCommitPolicy.REQUIRED_USER_RANK,
            leaders = emptyList(),
        )
        assertTrue(RecruitSoftCommitPolicy.qualifies(board, hasScholarshipOffer = true))
    }

    @Test
    fun resolver_excludesLowPrestigeOfferWithoutLead() {
        val prospect = RecruitProspect(
            prospectId = "hs-1-1",
            displayName = "Elite HS",
            stars = 5,
            position = PlayerPosition.ShootingGuard,
            homeState = "TX",
            nilAskUsd = 400_000,
            scoutLetterOverall = "A",
            hiddenRating = 95,
        )
        val inputs = SigningDaySoftCommitInputs(
            userSchool = SchoolRecruitingContext(
                programPrestige = 25,
                schoolHomeState = "ME",
                nilPoolAnnualUsd = 200_000,
            ),
            userProgramId = "user-dynasty",
            recommended = recommended,
        )
        val reservation = RecruitingWeekReservation(
            reservationId = "r1",
            prospectId = prospect.prospectId,
            action = RecruitingRefundableAction.ScholarshipOffer,
            pointsReserved = 150,
        )
        val signees = SigningDaySoftCommitResolver.incomingSoftCommits(
            listOf(prospect),
            listOf(reservation),
            inputs,
        )
        assertTrue(signees.isEmpty())
    }

    @Test
    fun resolver_includesWhenUserLeadsInfluenceBoard() {
        val prospect = RecruitProspect(
            prospectId = "hs-1-2",
            displayName = "Local Target",
            stars = 3,
            position = PlayerPosition.PowerForward,
            homeState = "OH",
            nilAskUsd = 40_000,
            scoutLetterOverall = "B",
            hiddenRating = 62,
        )
        val inputs = SigningDaySoftCommitInputs(
            userSchool = SchoolRecruitingContext(
                programPrestige = 88,
                schoolHomeState = "OH",
                pipelineStates = setOf("OH", "KY", "IN"),
                nilPoolAnnualUsd = 1_500_000,
            ),
            userProgramId = "user-dynasty",
            recommended = recommended,
            spendDeltaByProspect = mapOf(prospect.prospectId to 25),
        )
        val reservation = RecruitingWeekReservation(
            reservationId = "r2",
            prospectId = prospect.prospectId,
            action = RecruitingRefundableAction.ScholarshipOffer,
            pointsReserved = 150,
        )
        val signees = SigningDaySoftCommitResolver.incomingSoftCommits(
            listOf(prospect),
            listOf(reservation),
            inputs,
        )
        assertTrue(signees.single().prospectId == prospect.prospectId)
    }

    private fun loadRecommended() =
        BalanceRecruitingRecommendedParser.parse(loadRecruitingJson())

    private fun loadRecruitingJson(): kotlinx.serialization.json.JsonElement {
        var dir = Path.of(System.getProperty("user.dir")).toAbsolutePath()
        while (dir != null && !dir.resolve("assets/balance/balance-2026.json").toFile().exists()) {
            dir = dir.parent
        }
        val raw = dir!!.resolve("assets/balance/balance-2026.json").readText()
        val ruleset = (BalanceRulesetCodec.parseAndValidate(raw) as BalanceLoadResult.Success).ruleset
        return ruleset.recruiting
    }
}
