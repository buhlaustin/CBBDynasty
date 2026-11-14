package dev.novux.dynasty.cbb.data.carousel

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dev.novux.dynasty.cbb.core.carousel.CarouselApprovalCatalog
import dev.novux.dynasty.cbb.core.carousel.CarouselApprovalKind
import dev.novux.dynasty.cbb.core.carousel.CarouselSimContext
import dev.novux.dynasty.cbb.core.carousel.CarouselStatus
import dev.novux.dynasty.cbb.core.carousel.CoachingCarouselEngine
import dev.novux.dynasty.cbb.core.carousel.CoachingCarouselState
import kotlinx.coroutines.flow.first

private val Context.carouselDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "dynasty_carousel",
)

class CoachingCarouselRepository(
    private val context: Context,
    private val simContextProvider: suspend (dynastyId: String, seasonIndex: Int) -> CarouselSimContext,
) {
    suspend fun loadOrCreate(dynastyId: String, seasonIndex: Int): CoachingCarouselState {
        val prefs = context.carouselDataStore.data.first()
        val raw = prefs[prefKey(dynastyId)]
        if (raw == null) {
            val initial = CoachingCarouselEngine.initialState(seasonIndex)
            persist(dynastyId, seasonIndex, initial)
            return initial
        }
        val decoded = decode(raw)
        if (decoded.seasonIndex != seasonIndex) {
            val initial = CoachingCarouselEngine.initialState(seasonIndex)
            persist(dynastyId, seasonIndex, initial)
            return initial
        }
        return decoded.state
    }

    suspend fun save(dynastyId: String, seasonIndex: Int, state: CoachingCarouselState) {
        persist(dynastyId, seasonIndex, state)
    }

    suspend fun runBackgroundSim(
        dynastyId: String,
        seasonIndex: Int,
        state: CoachingCarouselState,
    ): CoachingCarouselState {
        val context = simContextProvider(dynastyId, seasonIndex)
        return CoachingCarouselEngine.runBackgroundSim(state, context)
    }

    private suspend fun persist(dynastyId: String, seasonIndex: Int, state: CoachingCarouselState) {
        val encoded = encode(seasonIndex, state)
        context.carouselDataStore.edit { prefs ->
            prefs[prefKey(dynastyId)] = encoded
        }
    }

    private fun prefKey(dynastyId: String) = stringPreferencesKey("carousel_$dynastyId")

    private fun encode(seasonIndex: Int, state: CoachingCarouselState): String {
        val pending = state.pendingApprovals.joinToString(",") { "${it.id}:${it.kind.name}" }
        val summary = state.backgroundSimSummary?.let {
            listOf(it.aiFired, it.aiExtended, it.aiHired, if (it.userFired) 1 else 0, if (it.userPoachImmunity) 1 else 0)
                .joinToString(",")
        }.orEmpty()
        return "$seasonIndex|${state.status.name}|${state.backgroundSimGeneration}|$pending|$summary"
    }

    private fun decode(raw: String): DecodedCarousel {
        val parts = raw.split("|", limit = 5)
        require(parts.size >= 4) { "invalid carousel snapshot" }
        val seasonIndex = parts[0].toInt()
        val status = CarouselStatus.valueOf(parts[1])
        val generation = parts[2].toInt()
        val pending = parts[3].split(",").filter { it.isNotBlank() }.map { token ->
            val bits = token.split(":")
            require(bits.size == 2)
            CarouselApprovalCatalog.card(bits[0], CarouselApprovalKind.valueOf(bits[1]))
        }
        val summary = parts.getOrNull(4)
            ?.takeIf { it.isNotBlank() }
            ?.split(",")
            ?.map { it.toInt() }
            ?.takeIf { it.size == 5 }
            ?.let {
                dev.novux.dynasty.cbb.core.carousel.CarouselLeagueBackgroundSimEngine.CarouselBackgroundSimSummary(
                    aiFired = it[0],
                    aiExtended = it[1],
                    aiHired = it[2],
                    userFired = it[3] == 1,
                    userPoachImmunity = it[4] == 1,
                )
            }
        val state = CoachingCarouselState(status, pending, generation, summary)
        return DecodedCarousel(seasonIndex, state)
    }

    private data class DecodedCarousel(
        val seasonIndex: Int,
        val state: CoachingCarouselState,
    )
}
