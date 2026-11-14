package dev.novux.dynasty.cbb.data.signingday

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dev.novux.dynasty.cbb.data.preseason.IncomingClassSeedRepository
import dev.novux.dynasty.cbb.core.recruiting.RecruitingWeekReservation
import dev.novux.dynasty.cbb.core.roster.PlayerPosition
import dev.novux.dynasty.cbb.core.signingday.IncomingSignee
import dev.novux.dynasty.cbb.core.signingday.SigningDayHardSignEngine
import dev.novux.dynasty.cbb.core.signingday.SigningDayNilSnapshotPolicy
import dev.novux.dynasty.cbb.core.signingday.SigningDayHardSignResult
import dev.novux.dynasty.cbb.core.signingday.SigningDayProgress
import dev.novux.dynasty.cbb.core.balance.BalanceRecruitingRecommendedParser
import dev.novux.dynasty.cbb.core.recruiting.SchoolRecruitingContext
import dev.novux.dynasty.cbb.core.save.LoadedDynasty
import dev.novux.dynasty.cbb.core.signingday.SigningDaySoftCommitInputs
import dev.novux.dynasty.cbb.core.signingday.SigningDaySoftCommitResolver
import dev.novux.dynasty.cbb.data.nil.NilAnnualPoolRepository
import dev.novux.dynasty.cbb.data.recruiting.RecruitingEconomyRepository
import dev.novux.dynasty.cbb.data.nil.NilBudgetRepository
import dev.novux.dynasty.cbb.core.player.loyalty.RecruitCommitPromiseLockEngine
import dev.novux.dynasty.cbb.data.recruiting.RecruitLockedPromiseRepository
import dev.novux.dynasty.cbb.data.recruiting.RecruitOfferPromiseRepository
import dev.novux.dynasty.cbb.data.recruiting.RecruitingReservationRepository
import dev.novux.dynasty.cbb.data.roster.DynastyRosterRepository
import kotlinx.coroutines.flow.first

private val Context.signingDayStore: DataStore<Preferences> by preferencesDataStore(name = "signing_day")

class SigningDayRepository(
    private val context: Context,
    private val reservationRepository: RecruitingReservationRepository,
    private val nilBudgetRepository: NilBudgetRepository,
    private val incomingClassSeedRepository: IncomingClassSeedRepository,
    private val recruitOfferPromiseRepository: RecruitOfferPromiseRepository,
    private val recruitLockedPromiseRepository: RecruitLockedPromiseRepository,
    private val recruitingEconomyRepository: RecruitingEconomyRepository,
    private val dynastyRosterRepository: DynastyRosterRepository,
    private val nilAnnualPoolRepository: NilAnnualPoolRepository,
) {
    suspend fun softCommitInputsFor(dynasty: LoadedDynasty): SigningDaySoftCommitInputs = softCommitInputs(dynasty)

    private suspend fun softCommitInputs(dynasty: LoadedDynasty): SigningDaySoftCommitInputs {
        val school = nilAnnualPoolRepository.userSchoolRecruitingContext(dynasty)
            ?: SchoolRecruitingContext(
                programPrestige = recruitingEconomyRepository.resolveUserProgramPrestige(dynasty),
                schoolHomeState = "OH",
                nilPoolAnnualUsd = NilAnnualPoolRepository.DEFAULT_FALLBACK_USD.toInt(),
            )
        return SigningDaySoftCommitInputs(
            userSchool = school,
            userProgramId = "user-${dynasty.dynastyId}",
            recommended = BalanceRecruitingRecommendedParser.parse(dynasty.balanceRuleset.recruiting),
            spendDeltaByProspect = recruitingEconomyRepository.loadInterestSpendDeltaByProspect(dynasty),
        )
    }
    suspend fun loadProgress(dynastyId: String, cycleSeasonIndex: Int): SigningDayProgress {
        val raw = context.signingDayStore.data.first()[progressKey(dynastyId)] ?: return SigningDayProgress(cycleSeasonIndex)
        return decodeProgress(raw, cycleSeasonIndex)
    }

    suspend fun saveProgress(dynastyId: String, progress: SigningDayProgress) {
        context.signingDayStore.edit { prefs ->
            prefs[progressKey(dynastyId)] = encodeProgress(progress)
        }
    }

    suspend fun markRankingsViewed(dynastyId: String, cycleSeasonIndex: Int) {
        val progress = loadProgress(dynastyId, cycleSeasonIndex).copy(rankingsViewed = true)
        saveProgress(dynastyId, progress)
    }

    suspend fun loadPositionOverrides(dynastyId: String): Map<String, PlayerPosition> {
        val raw = context.signingDayStore.data.first()[positionsKey(dynastyId)] ?: return emptyMap()
        return decodePositions(raw)
    }

    suspend fun savePositionOverride(dynastyId: String, prospectId: String, position: PlayerPosition) {
        val next = loadPositionOverrides(dynastyId) + (prospectId to position)
        context.signingDayStore.edit { prefs ->
            prefs[positionsKey(dynastyId)] = encodePositions(next)
        }
    }

    suspend fun confirmPositions(dynastyId: String, cycleSeasonIndex: Int) {
        val progress = loadProgress(dynastyId, cycleSeasonIndex).copy(positionsConfirmed = true)
        saveProgress(dynastyId, progress)
    }

    suspend fun loadIncomingClass(
        dynasty: LoadedDynasty,
        reservations: List<RecruitingWeekReservation> = emptyList(),
    ): List<IncomingSignee> {
        val activeReservations =
            reservations.ifEmpty { reservationRepository.loadActive(dynasty.dynastyId) }
        val pool = incomingClassSeedRepository.ensurePool(dynasty.dynastyId, dynasty.metadata.seasonIndex)
        val base = SigningDaySoftCommitResolver.incomingSoftCommits(
            pool,
            activeReservations,
            softCommitInputs(dynasty),
        )
        val overrides = loadPositionOverrides(dynasty.dynastyId)
        return base.map { signee ->
            val override = overrides[signee.prospectId]
            val nilSigned = nilBudgetRepository.offerUsd(
                dynasty.dynastyId,
                dynasty.metadata.seasonIndex,
                signee.prospectId,
            ).toInt()
            val withNil = SigningDayNilSnapshotPolicy.applyNilOffer(signee, nilSigned)
            if (override != null) withNil.copy(position = override) else withNil
        }
    }

    suspend fun hardSign(
        dynasty: LoadedDynasty,
        poolAnnualUsd: Long,
    ): SigningDayHardSignResult {
        val dynastyId = dynasty.dynastyId
        val seasonIndex = dynasty.metadata.seasonIndex
        val progress = loadProgress(dynastyId, seasonIndex)
        val signees = loadIncomingClass(dynasty)
        val offerPromises = recruitOfferPromiseRepository.loadAll(dynastyId, seasonIndex)
        val result = SigningDayHardSignEngine.finalizeClass(progress, signees, offerPromises)
        val locked = signees.map { signee ->
            RecruitCommitPromiseLockEngine.lockFromOffer(
                offerPromises.getValue(signee.prospectId),
                lockedAtSeasonIndex = seasonIndex + 1,
            )
        }
        recruitLockedPromiseRepository.saveAll(dynastyId, locked)
        val snapshots = nilBudgetRepository.signingSnapshots(
            dynastyId,
            seasonIndex,
            signees.map { it.prospectId to it.nilAskUsd },
            poolAnnualUsd,
        )
        nilBudgetRepository.commitSignedDeals(dynastyId, seasonIndex, snapshots)
        val signedWithNil = result.signed.map { signee ->
            val snap = snapshots.firstOrNull { it.prospectId == signee.prospectId }
            if (snap != null) signee.copy(nilSignedUsd = snap.nilSignedUsd) else signee
        }
        saveProgress(dynastyId, result.progress)
        saveSignedClass(dynastyId, signedWithNil)
        dynastyRosterRepository.enrollIncomingSignees(dynastyId, signedWithNil)
        return result.copy(signed = signedWithNil)
    }

    suspend fun loadSignedClass(dynastyId: String): List<IncomingSignee> {
        val raw = context.signingDayStore.data.first()[signedKey(dynastyId)] ?: return emptyList()
        return decodeSigned(raw)
    }

    private suspend fun saveSignedClass(dynastyId: String, signed: List<IncomingSignee>) {
        context.signingDayStore.edit { prefs ->
            prefs[signedKey(dynastyId)] = encodeSigned(signed)
        }
    }

    private fun progressKey(dynastyId: String) = stringPreferencesKey("signing_progress_$dynastyId")
    private fun positionsKey(dynastyId: String) = stringPreferencesKey("signing_positions_$dynastyId")
    private fun signedKey(dynastyId: String) = stringPreferencesKey("signing_signed_$dynastyId")

    private fun encodeProgress(p: SigningDayProgress): String =
        "${p.cycleSeasonIndex}|${p.rankingsViewed}|${p.positionsConfirmed}|${p.hardSignComplete}"

    private fun decodeProgress(raw: String, expectedSeason: Int): SigningDayProgress {
        val parts = raw.split("|")
        require(parts.size == 4)
        val season = parts[0].toInt()
        if (season != expectedSeason) return SigningDayProgress(expectedSeason)
        return SigningDayProgress(
            cycleSeasonIndex = season,
            rankingsViewed = parts[1].toBooleanStrict(),
            positionsConfirmed = parts[2].toBooleanStrict(),
            hardSignComplete = parts[3].toBooleanStrict(),
        )
    }

    private fun encodePositions(map: Map<String, PlayerPosition>): String =
        map.entries.joinToString(";") { "${it.key}:${it.value.wire}" }

    private fun decodePositions(raw: String): Map<String, PlayerPosition> =
        raw.split(";").filter { it.isNotBlank() }.mapNotNull { token ->
            val idx = token.indexOf(':')
            if (idx <= 0) return@mapNotNull null
            val wire = token.substring(idx + 1)
            val pos = PlayerPosition.fromWire(wire) ?: return@mapNotNull null
            token.substring(0, idx) to pos
        }.toMap()

    private fun encodeSigned(signed: List<IncomingSignee>): String =
        signed.joinToString(";") { s ->
            listOf(
                s.prospectId,
                s.displayName,
                s.stars,
                s.position.wire,
                s.homeState,
                s.nilAskUsd,
                s.nilSignedUsd,
                s.nationalHsRank?.toString().orEmpty(),
                s.scoutLetterOverall,
            ).joinToString("|")
        }

    private fun decodeSigned(raw: String): List<IncomingSignee> =
        raw.split(";").filter { it.isNotBlank() }.map { token ->
            val p = token.split("|")
            when (p.size) {
                8 -> IncomingSignee(
                    prospectId = p[0],
                    displayName = p[1],
                    stars = p[2].toInt(),
                    position = PlayerPosition.fromWire(p[3])!!,
                    homeState = p[4],
                    nilAskUsd = p[5].toInt(),
                    nationalHsRank = p[6].takeIf { it.isNotBlank() }?.toInt(),
                    scoutLetterOverall = p[7],
                )
                9 -> IncomingSignee(
                    prospectId = p[0],
                    displayName = p[1],
                    stars = p[2].toInt(),
                    position = PlayerPosition.fromWire(p[3])!!,
                    homeState = p[4],
                    nilAskUsd = p[5].toInt(),
                    nilSignedUsd = p[6].toInt(),
                    nationalHsRank = p[7].takeIf { it.isNotBlank() }?.toInt(),
                    scoutLetterOverall = p[8],
                )
                else -> error("invalid signed class row")
            }
        }
}
