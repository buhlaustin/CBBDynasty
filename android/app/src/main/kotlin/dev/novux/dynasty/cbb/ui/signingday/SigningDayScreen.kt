package dev.novux.dynasty.cbb.ui.signingday

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.novux.dynasty.cbb.R
import dev.novux.dynasty.cbb.core.roster.PlayerPosition
import dev.novux.dynasty.cbb.core.save.LoadedDynasty
import dev.novux.dynasty.cbb.core.signingday.IncomingSignee
import dev.novux.dynasty.cbb.core.signingday.NationalRankRow
import dev.novux.dynasty.cbb.core.signingday.NationalRecruitingRankings
import dev.novux.dynasty.cbb.core.signingday.SigningDayClassBoardSummaryPolicy
import dev.novux.dynasty.cbb.core.signingday.SigningDayHardSignBlockReason
import dev.novux.dynasty.cbb.core.signingday.SigningDayHardSignUiGate
import dev.novux.dynasty.cbb.core.signingday.SigningDayProgress
import dev.novux.dynasty.cbb.core.signingday.SigningDayUiPolicy
import dev.novux.dynasty.cbb.data.preseason.IncomingClassSeedRepository
import dev.novux.dynasty.cbb.data.nil.NilAnnualPoolRepository
import dev.novux.dynasty.cbb.data.offseason.OffseasonWeekNineRepository
import dev.novux.dynasty.cbb.core.player.loyalty.CoachPromiseType
import dev.novux.dynasty.cbb.core.player.loyalty.PlayerPromiseLockPolicy
import dev.novux.dynasty.cbb.core.player.loyalty.RecruitCommitPromiseLockEngine
import dev.novux.dynasty.cbb.core.recruiting.RecruitOfferPromise
import dev.novux.dynasty.cbb.data.recruiting.RecruitOfferPromiseRepository
import dev.novux.dynasty.cbb.data.recruiting.RecruitingEconomyRepository
import dev.novux.dynasty.cbb.data.signingday.SigningDayRepository
import dev.novux.dynasty.cbb.ui.recruiting.RecruitPromisePickerSheet
import kotlinx.coroutines.launch

enum class SigningDayInitialTab {
    Rankings,
    Positions,
}

/** Signing Day — promises lock at hard sign, no amend ([MEGA-COACH-175]). */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SigningDayScreen(
    loaded: LoadedDynasty,
    signingDayRepository: SigningDayRepository,
    recruitingEconomyRepository: RecruitingEconomyRepository,
    nilAnnualPoolRepository: NilAnnualPoolRepository,
    weekNineRepository: OffseasonWeekNineRepository? = null,
    incomingClassSeedRepository: IncomingClassSeedRepository,
    recruitOfferPromiseRepository: RecruitOfferPromiseRepository,
    initialTab: SigningDayInitialTab = SigningDayInitialTab.Rankings,
    onBack: () -> Unit,
) {
    var tabIndex by remember(initialTab) {
        mutableIntStateOf(
            if (initialTab == SigningDayInitialTab.Positions) 1
            else SigningDayUiPolicy.DEFAULT_TAB_INDEX,
        )
    }
    var progress by remember { mutableStateOf<SigningDayProgress?>(null) }
    var incoming by remember { mutableStateOf<List<IncomingSignee>>(emptyList()) }
    var rankings by remember { mutableStateOf<List<NationalRankRow>>(emptyList()) }
    var offerPromises by remember { mutableStateOf<Map<String, RecruitOfferPromise>>(emptyMap()) }
    var promisePickerTarget by remember { mutableStateOf<Pair<String, String>?>(null) }
    val scope = rememberCoroutineScope()

    suspend fun refresh() {
        progress = signingDayRepository.loadProgress(loaded.dynastyId, loaded.metadata.seasonIndex)
        incoming = signingDayRepository.loadIncomingClass(loaded)
        offerPromises = recruitOfferPromiseRepository.loadAll(
            loaded.dynastyId,
            loaded.metadata.seasonIndex,
        )
    }

    LaunchedEffect(loaded.dynastyId, loaded.metadata.seasonIndex) {
        val pool = incomingClassSeedRepository.ensurePool(loaded.dynastyId, loaded.metadata.seasonIndex)
        rankings = NationalRecruitingRankings.top60FromPool(pool)
        refresh()
        if (tabIndex == 0) {
            signingDayRepository.markRankingsViewed(loaded.dynastyId, loaded.metadata.seasonIndex)
            progress = signingDayRepository.loadProgress(loaded.dynastyId, loaded.metadata.seasonIndex)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 28.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = stringResource(R.string.signing_day_title),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = stringResource(R.string.signing_day_subtitle),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = stringResource(R.string.signing_day_required_dashboard_hint),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        PrimaryTabRow(selectedTabIndex = tabIndex) {
            Tab(
                selected = tabIndex == 0,
                onClick = {
                    tabIndex = 0
                    scope.launch {
                        signingDayRepository.markRankingsViewed(loaded.dynastyId, loaded.metadata.seasonIndex)
                        refresh()
                    }
                },
                text = { Text(stringResource(R.string.signing_day_tab_rankings)) },
            )
            Tab(
                selected = tabIndex == 1,
                onClick = { tabIndex = 1 },
                text = { Text(stringResource(R.string.signing_day_tab_positions)) },
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            when (tabIndex) {
                0 -> RankingsBody(rankings)
                else -> PositionsBody(
                signees = incoming,
                progress = progress,
                offerPromises = offerPromises,
                onSetPromise = { signee -> promisePickerTarget = signee.prospectId to signee.displayName },
                onCyclePosition = { signee ->
                    scope.launch {
                        val next = PlayerPosition.entries[
                            (PlayerPosition.entries.indexOf(signee.position) + 1) % PlayerPosition.entries.size
                        ]
                        signingDayRepository.savePositionOverride(loaded.dynastyId, signee.prospectId, next)
                        refresh()
                    }
                },
                onConfirmPositions = {
                    scope.launch {
                        signingDayRepository.confirmPositions(loaded.dynastyId, loaded.metadata.seasonIndex)
                        refresh()
                    }
                },
                onHardSign = {
                    scope.launch {
                        val boost = weekNineRepository?.nilPoolBoostUsd(
                            loaded.dynastyId,
                            loaded.metadata.seasonIndex,
                        ) ?: 0
                        val pool = nilAnnualPoolRepository.effectivePoolUsd(loaded, boost)
                        signingDayRepository.hardSign(loaded, pool)
                        recruitingEconomyRepository.closeCycleAfterHardSign(loaded.dynastyId)
                        refresh()
                    }
                },
                )
            }
        }
        promisePickerTarget?.let { (prospectId, displayName) ->
            RecruitPromisePickerSheet(
                prospectDisplayName = displayName,
                onDismiss = { promisePickerTarget = null },
                onConfirm = { promiseType ->
                    scope.launch {
                        val stayTopN = if (promiseType == CoachPromiseType.StayPledgeTopN) {
                            dev.novux.dynasty.cbb.core.player.loyalty.CoachPromise.DEFAULT_STAY_TOP_N
                        } else {
                            null
                        }
                        recruitOfferPromiseRepository.save(
                            loaded.dynastyId,
                            loaded.metadata.seasonIndex,
                            RecruitOfferPromise(
                                prospectId = prospectId,
                                promiseType = promiseType,
                                stayTopN = stayTopN,
                            ),
                        )
                        refresh()
                        promisePickerTarget = null
                    }
                },
            )
        }
        OutlinedButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(R.string.settings_back))
        }
    }
}

@Composable
private fun SigningDayGatingHints(
    progress: SigningDayProgress?,
    allPromisesSet: Boolean,
) {
    val reasons = SigningDayHardSignUiGate.blockingReasons(progress, allPromisesSet)
    val actionable = reasons.filter {
        it != SigningDayHardSignBlockReason.AlreadySigned &&
            it != SigningDayHardSignBlockReason.PromisesMissing
    }
    if (progress?.hardSignComplete == true) return
    actionable.forEach { reason ->
        val text = when (reason) {
            SigningDayHardSignBlockReason.ProgressLoading ->
                stringResource(R.string.signing_day_gate_loading)
            SigningDayHardSignBlockReason.RankingsNotViewed ->
                stringResource(R.string.signing_day_gate_rankings)
            SigningDayHardSignBlockReason.PositionsNotConfirmed ->
                stringResource(R.string.signing_day_gate_positions)
            else -> return@forEach
        }
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.primary,
        )
    }
}

@Composable
private fun RankingsBody(rows: List<NationalRankRow>) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        item {
            Text(
                text = stringResource(R.string.signing_day_recruit_rankings_heading),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(bottom = 4.dp),
            )
        }
        items(rows, key = { it.prospect.prospectId }) { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = stringResource(
                        R.string.signing_day_rank_line,
                        row.rank,
                        row.prospect.displayName,
                        row.prospect.stars,
                        row.prospect.position.label,
                    ),
                    style = MaterialTheme.typography.bodyLarge,
                )
            }
        }
    }
}

@Composable
private fun PositionsBody(
    signees: List<IncomingSignee>,
    progress: SigningDayProgress?,
    offerPromises: Map<String, RecruitOfferPromise>,
    onSetPromise: (IncomingSignee) -> Unit,
    onCyclePosition: (IncomingSignee) -> Unit,
    onConfirmPositions: () -> Unit,
    onHardSign: () -> Unit,
) {
    @Suppress("unused")
    val qaId = PlayerPromiseLockPolicy.MEGA_ID
    val allPromisesSet = RecruitCommitPromiseLockEngine.allSigneesHaveOffer(
        signees.map { it.prospectId },
        offerPromises,
    )
    Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        @Suppress("unused")
        val qaBoard = SigningDayClassBoardSummaryPolicy.MEGA_ID
        if (signees.isNotEmpty()) {
            val counts = SigningDayClassBoardSummaryPolicy.orderedCounts(signees)
            val breakdown = counts.joinToString(" · ") { (pos, n) -> "${pos.label}: $n" }
            Text(
                text = stringResource(
                    R.string.signing_day_board_summary,
                    signees.size,
                    breakdown,
                ),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
            )
        }
        Text(
            text = stringResource(R.string.signing_day_position_editor_hint),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = stringResource(R.string.signing_day_promise_locked_note),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        if (signees.isNotEmpty() && !allPromisesSet) {
            Text(
                text = stringResource(R.string.signing_day_promise_missing_hint),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
            )
        }
        if (signees.isEmpty()) {
            Text(
                text = stringResource(R.string.signing_day_no_commits),
                style = MaterialTheme.typography.bodyLarge,
            )
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(signees, key = { it.prospectId }) { signee ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(signee.displayName, fontWeight = FontWeight.SemiBold)
                            Text(
                                stringResource(
                                    R.string.signing_day_signee_meta,
                                    signee.stars,
                                    signee.position.label,
                                ),
                                style = MaterialTheme.typography.bodySmall,
                            )
                            val promise = offerPromises[signee.prospectId]
                            if (promise != null) {
                                Text(
                                    stringResource(
                                        R.string.signing_day_promise_line,
                                        promise.displayLabel(),
                                    ),
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Medium,
                                )
                            }
                        }
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            if (offerPromises[signee.prospectId] == null) {
                                OutlinedButton(onClick = { onSetPromise(signee) }) {
                                    Text(stringResource(R.string.signing_day_promise_set))
                                }
                            }
                            OutlinedButton(onClick = { onCyclePosition(signee) }) {
                                Text(stringResource(R.string.signing_day_cycle_position))
                            }
                        }
                    }
                }
            }
        }
        val canConfirm = SigningDayHardSignUiGate.canConfirmPositions(progress)
        val canHardSign = SigningDayHardSignUiGate.canHardSign(progress, allPromisesSet)
        SigningDayGatingHints(
            progress = progress,
            allPromisesSet = allPromisesSet,
        )
        Button(
            onClick = onConfirmPositions,
            modifier = Modifier.fillMaxWidth(),
            enabled = canConfirm,
        ) {
            Text(stringResource(R.string.signing_day_confirm_positions))
        }
        if (progress?.hardSignComplete == true) {
            Text(
                text = stringResource(R.string.signing_day_cycle_close_hint),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Button(
            onClick = onHardSign,
            modifier = Modifier.fillMaxWidth(),
            enabled = canHardSign,
        ) {
            Text(
                if (progress?.hardSignComplete == true) {
                    stringResource(R.string.signing_day_hard_sign_done)
                } else {
                    stringResource(R.string.signing_day_hard_sign)
                },
            )
        }
    }
}
