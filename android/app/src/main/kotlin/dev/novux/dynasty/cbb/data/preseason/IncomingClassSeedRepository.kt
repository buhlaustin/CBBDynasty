package dev.novux.dynasty.cbb.data.preseason

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dev.novux.dynasty.cbb.core.preseason.IncomingClassSeedRunPhase
import dev.novux.dynasty.cbb.core.preseason.Top60NewsHeadline
import dev.novux.dynasty.cbb.core.preseason.Top60NewsPublisher
import dev.novux.dynasty.cbb.core.recruiting.IncomingClassSeeder
import dev.novux.dynasty.cbb.core.recruiting.RecruitPoolKind
import dev.novux.dynasty.cbb.core.recruiting.RecruitPriority
import dev.novux.dynasty.cbb.core.recruiting.RecruitProspect
import dev.novux.dynasty.cbb.core.roster.PlayerPosition
import dev.novux.dynasty.cbb.core.signingday.NationalRecruitingRankings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext

private val Context.incomingClassSeedStore: DataStore<Preferences> by preferencesDataStore(
    name = "incoming_class_seed",
)

class IncomingClassSeedRepository(private val context: Context) {
    suspend fun isSeeded(dynastyId: String, seasonIndex: Int): Boolean = withContext(Dispatchers.IO) {
        context.incomingClassSeedStore.data.first()[seededKey(dynastyId, seasonIndex)] == true
    }

    suspend fun loadPool(dynastyId: String, seasonIndex: Int): List<RecruitProspect>? =
        withContext(Dispatchers.IO) {
            if (!isSeededOnStore(dynastyId, seasonIndex)) return@withContext null
            val raw = context.incomingClassSeedStore.data.first()[poolKey(dynastyId, seasonIndex)]
                ?: return@withContext null
            withContext(Dispatchers.Default) { decodePool(raw) }
        }

    /** Loads the persisted enrolling class or generates and persists it when missing. */
    suspend fun ensurePool(dynastyId: String, seasonIndex: Int): List<RecruitProspect> =
        withContext(Dispatchers.IO) {
            loadPoolOnIo(dynastyId, seasonIndex)?.let { return@withContext it }
            runSeedOnIo(dynastyId, seasonIndex)
            checkNotNull(loadPoolOnIo(dynastyId, seasonIndex))
        }

    suspend fun loadHeadline(dynastyId: String, seasonIndex: Int): Top60NewsHeadline? =
        withContext(Dispatchers.IO) {
            val pool = ensurePool(dynastyId, seasonIndex)
            val headline = context.incomingClassSeedStore.data.first()[headlineKey(dynastyId, seasonIndex)]
                ?: withContext(Dispatchers.Default) {
                    Top60NewsPublisher.publish(seasonIndex, pool).headline
                }
            Top60NewsHeadline(
                seasonIndex = seasonIndex,
                headline = headline,
                top60 = NationalRecruitingRankings.top60FromPool(pool),
            )
        }

    suspend fun runSeed(
        dynastyId: String,
        seasonIndex: Int,
        onPhase: ((IncomingClassSeedRunPhase) -> Unit)? = null,
    ): Top60NewsHeadline = withContext(Dispatchers.IO) {
        runSeedOnIo(dynastyId, seasonIndex, onPhase)
    }

    private suspend fun runSeedOnIo(
        dynastyId: String,
        seasonIndex: Int,
        onPhase: ((IncomingClassSeedRunPhase) -> Unit)? = null,
    ): Top60NewsHeadline {
        val pool = withContext(Dispatchers.Default) {
            IncomingClassSeeder.seed(
                enrollingSeasonIndex = seasonIndex,
                seedKey = dynastyId.hashCode().toLong(),
                onPhase = onPhase,
            )
        }
        onPhase?.invoke(IncomingClassSeedRunPhase.PublishingTop60Headline)
        val news = withContext(Dispatchers.Default) {
            Top60NewsPublisher.publish(seasonIndex, pool)
        }
        onPhase?.invoke(IncomingClassSeedRunPhase.SavingClassPool)
        val encoded = withContext(Dispatchers.Default) { encodePool(pool) }
        context.incomingClassSeedStore.edit { prefs ->
            prefs[seededKey(dynastyId, seasonIndex)] = true
            prefs[poolKey(dynastyId, seasonIndex)] = encoded
            prefs[headlineKey(dynastyId, seasonIndex)] = news.headline
        }
        onPhase?.invoke(IncomingClassSeedRunPhase.Done)
        return news
    }

    private suspend fun isSeededOnStore(dynastyId: String, seasonIndex: Int): Boolean =
        context.incomingClassSeedStore.data.first()[seededKey(dynastyId, seasonIndex)] == true

    private suspend fun loadPoolOnIo(dynastyId: String, seasonIndex: Int): List<RecruitProspect>? {
        if (!isSeededOnStore(dynastyId, seasonIndex)) return null
        val raw = context.incomingClassSeedStore.data.first()[poolKey(dynastyId, seasonIndex)] ?: return null
        return withContext(Dispatchers.Default) { decodePool(raw) }
    }

    private fun seededKey(dynastyId: String, seasonIndex: Int) =
        booleanPreferencesKey("seeded_${dynastyId}_$seasonIndex")

    private fun poolKey(dynastyId: String, seasonIndex: Int) =
        stringPreferencesKey("pool_${dynastyId}_$seasonIndex")

    private fun headlineKey(dynastyId: String, seasonIndex: Int) =
        stringPreferencesKey("headline_${dynastyId}_$seasonIndex")

    private fun encodePool(pool: List<RecruitProspect>): String =
        pool.joinToString("\n") { p ->
            buildList {
                add(p.prospectId)
                add(p.displayName.escapeField())
                add(p.stars)
                add(p.position.wire)
                add(p.homeState)
                add(p.nationalHsRank?.toString().orEmpty())
                add(p.nilAskUsd)
                add(p.scoutLetterOverall.escapeField())
                add(p.topPriorities.joinToString(".") { it.name })
                add(p.poolKind.wire)
                add(p.hiddenRating)
            }.joinToString("|")
        }

    private fun decodePool(raw: String): List<RecruitProspect> =
        raw.lineSequence().mapNotNull { line ->
            val parts = line.split("|")
            if (parts.size < 11) return@mapNotNull null
            val position = PlayerPosition.fromWire(parts[3]) ?: return@mapNotNull null
            val rank = parts[5].toIntOrNull()
            val priorities = parts[8].split(".").mapNotNull { name ->
                if (name.isBlank()) null else runCatching { RecruitPriority.valueOf(name) }.getOrNull()
            }
            val kind = RecruitPoolKind.fromWire(parts[9]) ?: RecruitPoolKind.HighSchool
            RecruitProspect(
                prospectId = parts[0],
                displayName = parts[1].unescapeField(),
                stars = parts[2].toIntOrNull() ?: 2,
                position = position,
                homeState = parts[4],
                nationalHsRank = rank,
                nilAskUsd = parts[6].toIntOrNull() ?: 0,
                scoutLetterOverall = parts[7].unescapeField(),
                topPriorities = priorities,
                poolKind = kind,
                hiddenRating = parts[10].toIntOrNull() ?: 0,
            )
        }.toList()

    private fun String.escapeField(): String = replace("|", "%7C").replace("\n", " ")
    private fun String.unescapeField(): String = replace("%7C", "|")
}
