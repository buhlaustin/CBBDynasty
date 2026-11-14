package dev.novux.dynasty.cbb.ui.hubs

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import dev.novux.dynasty.cbb.core.rankings.RankingsHubSnapshot
import dev.novux.dynasty.cbb.data.rankings.RankingsHubRepository
import dev.novux.dynasty.cbb.ui.rankings.RankingsHubScreen

@Composable
fun RankingsHubRouteScreen(
    dynastyId: String,
    seasonIndex: Int,
    rankingsHubRepository: RankingsHubRepository,
    onBack: () -> Unit,
) {
    var snapshot by remember { mutableStateOf<RankingsHubSnapshot?>(null) }
    LaunchedEffect(dynastyId, seasonIndex) {
        snapshot = rankingsHubRepository.load(dynastyId, seasonIndex)
    }
    when (val data = snapshot) {
        null -> Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        else -> RankingsHubScreen(
            snapshot = data,
            onBack = onBack,
            onPublishPreseason = null,
        )
    }
}
