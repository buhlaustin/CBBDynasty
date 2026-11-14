package dev.novux.dynasty.cbb.ui.player

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.novux.dynasty.cbb.R
import dev.novux.dynasty.cbb.core.player.stats.PlayerCareerAveragesPolicy
import dev.novux.dynasty.cbb.core.player.stats.PlayerCareerTotalsPolicy
import dev.novux.dynasty.cbb.core.player.stats.PlayerCountingStats
import dev.novux.dynasty.cbb.core.player.stats.PlayerRateStats
import dev.novux.dynasty.cbb.core.player.stats.PlayerStatsEngine
import dev.novux.dynasty.cbb.core.player.stats.PlayerStatsSummary

/** Stats tab card 1 — season rate stats through FT% ([MEGA-COACH-150]). */
@Composable
fun PlayerSeasonAveragesCard(
    averages: PlayerRateStats,
    gamesPlayedSeason: Int,
    modifier: Modifier = Modifier,
) {
    PlayerStatsCard(
        modifier = modifier,
        title = stringResource(R.string.player_card_stats_season_avg_title),
        subtitle = stringResource(R.string.player_card_stats_gp_suffix, gamesPlayedSeason),
    ) {
        seasonAverageLines(averages).forEach { StatLine(it) }
    }
}

/** Stats tab card 3 — career rate stats; shooting % blended over career makes/attempts ([MEGA-COACH-152]). */
@Composable
fun PlayerCareerAveragesCard(
    averages: PlayerRateStats,
    careerGamesPlayed: Int,
    modifier: Modifier = Modifier,
) {
    @Suppress("unused")
    val qaId = PlayerCareerAveragesPolicy.MEGA_ID
    PlayerStatsCard(
        modifier = modifier,
        title = stringResource(R.string.player_card_stats_career_avg_title),
        subtitle = stringResource(R.string.player_card_stats_gp_suffix, careerGamesPlayed),
    ) {
        careerAverageLines(averages).forEach { StatLine(it) }
    }
}

/** Stats tab card 2 — season counting totals ([MEGA-COACH-151]). */
@Composable
fun PlayerSeasonTotalsCard(
    totals: PlayerCountingStats,
    modifier: Modifier = Modifier,
) {
    PlayerStatsCard(
        modifier = modifier,
        title = stringResource(R.string.player_card_stats_season_tot_title),
    ) {
        countingTotalLines(totals).forEach { StatLine(it) }
    }
}

/** Stats tab card 4 — career counting totals across all seasons ([MEGA-COACH-153]). */
@Composable
fun PlayerCareerTotalsCard(
    totals: PlayerCountingStats,
    modifier: Modifier = Modifier,
) {
    @Suppress("unused")
    val qaId = PlayerCareerTotalsPolicy.MEGA_ID
    PlayerStatsCard(
        modifier = modifier,
        title = stringResource(R.string.player_card_stats_career_tot_title),
    ) {
        countingTotalLines(totals).forEach { StatLine(it) }
    }
}

@Composable
fun PlayerStatsFourCards(summary: PlayerStatsSummary, modifier: Modifier = Modifier) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        PlayerSeasonAveragesCard(
            averages = summary.seasonAverages,
            gamesPlayedSeason = summary.gamesPlayedSeason,
        )
        PlayerSeasonTotalsCard(totals = summary.seasonTotals)
        PlayerCareerAveragesCard(
            averages = summary.careerAverages,
            careerGamesPlayed = summary.careerGamesPlayed,
        )
        PlayerCareerTotalsCard(totals = summary.careerTotals)
    }
}

@Composable
private fun PlayerStatsCard(
    title: String,
    subtitle: String? = null,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        ),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
            subtitle?.let {
                Text(
                    it,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            content()
        }
    }
}

@Composable
private fun StatLine(text: String) {
    Text(text, style = MaterialTheme.typography.bodyMedium)
}

@Composable
private fun seasonAverageLines(r: PlayerRateStats): List<String> = listOf(
    stringResource(R.string.player_stat_ppg, PlayerStatsEngine.formatPerGame(r.pointsPerGame)),
    stringResource(R.string.player_stat_apg, PlayerStatsEngine.formatPerGame(r.assistsPerGame)),
    stringResource(R.string.player_stat_rpg, PlayerStatsEngine.formatPerGame(r.reboundsPerGame)),
    stringResource(R.string.player_stat_spg, PlayerStatsEngine.formatPerGame(r.stealsPerGame)),
    stringResource(R.string.player_stat_bpg, PlayerStatsEngine.formatPerGame(r.blocksPerGame)),
    stringResource(R.string.player_stat_topg, PlayerStatsEngine.formatPerGame(r.turnoversPerGame)),
    stringResource(R.string.player_stat_fgm_pg, PlayerStatsEngine.formatPerGame(r.fieldGoalsMadePerGame)),
    stringResource(R.string.player_stat_fga_pg, PlayerStatsEngine.formatPerGame(r.fieldGoalsAttemptedPerGame)),
    stringResource(R.string.player_stat_fg_pct, PlayerStatsEngine.formatPct(r.fieldGoalPct)),
    stringResource(R.string.player_stat_3pm_pg, PlayerStatsEngine.formatPerGame(r.threePointsMadePerGame)),
    stringResource(R.string.player_stat_3pa_pg, PlayerStatsEngine.formatPerGame(r.threePointsAttemptedPerGame)),
    stringResource(R.string.player_stat_3p_pct, PlayerStatsEngine.formatPct(r.threePointPct)),
    stringResource(R.string.player_stat_ftm_pg, PlayerStatsEngine.formatPerGame(r.freeThrowsMadePerGame)),
    stringResource(R.string.player_stat_fta_pg, PlayerStatsEngine.formatPerGame(r.freeThrowsAttemptedPerGame)),
    stringResource(R.string.player_stat_ft_pct, PlayerStatsEngine.formatPct(r.freeThrowPct)),
)

@Composable
private fun careerAverageLines(r: PlayerRateStats): List<String> = listOf(
    stringResource(R.string.player_stat_ppg, PlayerStatsEngine.formatPerGame(r.pointsPerGame)),
    stringResource(R.string.player_stat_apg, PlayerStatsEngine.formatPerGame(r.assistsPerGame)),
    stringResource(R.string.player_stat_rpg, PlayerStatsEngine.formatPerGame(r.reboundsPerGame)),
    stringResource(R.string.player_stat_spg, PlayerStatsEngine.formatPerGame(r.stealsPerGame)),
    stringResource(R.string.player_stat_bpg, PlayerStatsEngine.formatPerGame(r.blocksPerGame)),
    stringResource(R.string.player_stat_topg, PlayerStatsEngine.formatPerGame(r.turnoversPerGame)),
    stringResource(R.string.player_stat_fg_pct, PlayerStatsEngine.formatPct(r.fieldGoalPct)),
    stringResource(R.string.player_stat_3p_pct, PlayerStatsEngine.formatPct(r.threePointPct)),
    stringResource(R.string.player_stat_ft_pct, PlayerStatsEngine.formatPct(r.freeThrowPct)),
)

@Composable
private fun countingTotalLines(t: PlayerCountingStats): List<String> = listOf(
    stringResource(R.string.player_stat_tot_points, t.points),
    stringResource(R.string.player_stat_tot_assists, t.assists),
    stringResource(R.string.player_stat_tot_rebounds, t.rebounds),
    stringResource(R.string.player_stat_tot_steals, t.steals),
    stringResource(R.string.player_stat_tot_blocks, t.blocks),
    stringResource(R.string.player_stat_tot_turnovers, t.turnovers),
)
