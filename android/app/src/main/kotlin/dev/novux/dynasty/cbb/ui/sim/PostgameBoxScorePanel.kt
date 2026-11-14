package dev.novux.dynasty.cbb.ui.sim

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.novux.dynasty.cbb.R
import dev.novux.dynasty.cbb.core.roster.RosterPlayer
import dev.novux.dynasty.cbb.core.sim.GameSimResult
import dev.novux.dynasty.cbb.core.sim.PlayerBoxScore
import dev.novux.dynasty.cbb.core.sim.TeamBoxScore
import dev.novux.dynasty.cbb.core.injuries.GameSimInjuryEvent
import dev.novux.dynasty.cbb.core.sim.PlayerBoxScoreMinutesLabel
import dev.novux.dynasty.cbb.core.sim.TeamBoxScoreDisplay

@Composable
fun PostgameBoxScorePanel(
    result: GameSimResult,
    homeName: String,
    awayName: String,
    homeRosterPlayers: List<RosterPlayer>,
) {
    var tab by remember { mutableIntStateOf(0) }
  val nameById = remember(homeRosterPlayers) {
    homeRosterPlayers.associate { it.playerId to it.displayLabel() }
  }
  Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
    Text(
      text = stringResource(R.string.game_watch_postgame_box_title),
      style = MaterialTheme.typography.titleMedium,
      fontWeight = FontWeight.SemiBold,
    )
    TabRow(selectedTabIndex = tab) {
      Tab(
        selected = tab == 0,
        onClick = { tab = 0 },
        text = { Text(stringResource(R.string.game_watch_box_tab_team)) },
      )
      Tab(
        selected = tab == 1,
        onClick = { tab = 1 },
        text = { Text(stringResource(R.string.game_watch_box_tab_players)) },
      )
    }
    when (tab) {
      0 -> TeamBoxScoreTab(
        homeName = homeName,
        awayName = awayName,
        home = TeamBoxScoreDisplay.from(result.homeBox, result.homePlayerBox),
        away = TeamBoxScoreDisplay.from(result.awayBox, result.awayPlayerBox),
        homeScore = result.homeScore,
        awayScore = result.awayScore,
      )
      1 -> PlayerBoxScoreTab(
        homeName = homeName,
        awayName = awayName,
        homeLines = result.homePlayerBox,
        awayLines = result.awayPlayerBox,
        homeInjuryIds = PlayerBoxScoreMinutesLabel.midGameInjuredIds(result.homeInjuries),
        awayInjuryIds = PlayerBoxScoreMinutesLabel.midGameInjuredIds(result.awayInjuries),
        nameById = nameById,
      )
    }
  }
}

@Composable
private fun TeamBoxScoreTab(
    homeName: String,
    awayName: String,
    home: TeamBoxScoreDisplay,
    away: TeamBoxScoreDisplay,
    homeScore: Int,
    awayScore: Int,
) {
  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.spacedBy(8.dp),
  ) {
    TeamBoxCard(
      modifier = Modifier.weight(1f),
      teamName = homeName,
      score = homeScore,
      display = home,
    )
    TeamBoxCard(
      modifier = Modifier.weight(1f),
      teamName = awayName,
      score = awayScore,
      display = away,
    )
  }
}

@Composable
private fun TeamBoxCard(
    teamName: String,
    score: Int,
    display: TeamBoxScoreDisplay,
    modifier: Modifier = Modifier,
) {
  val box = display.box
  Card(
    modifier = modifier,
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
  ) {
    Column(
      modifier = Modifier.padding(12.dp),
      verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
      Text(teamName, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
      Text(stringResource(R.string.game_watch_box_team_score, score), style = MaterialTheme.typography.bodyLarge)
      Text(
        stringResource(
          R.string.game_watch_box_team_shooting,
          box.fieldGoalsMade,
          box.fieldGoalsAttempted,
          box.threePointsMade,
          box.threePointsAttempted,
          box.freeThrowsMade,
          box.freeThrowsAttempted,
        ),
        style = MaterialTheme.typography.bodySmall,
      )
      Text(
        stringResource(
          R.string.game_watch_box_team_pct,
          display.fieldGoalPct,
          display.threePointPct,
          display.freeThrowPct,
        ),
        style = MaterialTheme.typography.bodySmall,
      )
      Text(
        stringResource(
          R.string.game_watch_box_team_rebounds,
          display.offensiveRebounds,
          display.defensiveRebounds,
        ),
        style = MaterialTheme.typography.bodySmall,
      )
      Text(
        stringResource(R.string.game_watch_box_team_misc, box.turnovers, box.personalFouls, box.possessions),
        style = MaterialTheme.typography.bodySmall,
      )
    }
  }
}

@Composable
private fun PlayerBoxScoreTab(
    homeName: String,
    awayName: String,
    homeLines: List<PlayerBoxScore>,
    awayLines: List<PlayerBoxScore>,
    homeInjuryIds: Set<String>,
    awayInjuryIds: Set<String>,
    nameById: Map<String, String>,
) {
  Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
    if (homeLines.isNotEmpty()) {
      PlayerBoxTable(teamLabel = homeName, lines = homeLines, injuryIds = homeInjuryIds, nameById = nameById)
    }
    if (awayLines.isNotEmpty()) {
      PlayerBoxTable(teamLabel = awayName, lines = awayLines, injuryIds = awayInjuryIds, nameById = nameById)
    }
    if (homeLines.isEmpty() && awayLines.isEmpty()) {
      Text(
        stringResource(R.string.game_watch_box_players_unavailable),
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
      )
    }
  }
}

@Composable
private fun PlayerBoxTable(
    teamLabel: String,
    lines: List<PlayerBoxScore>,
    injuryIds: Set<String>,
    nameById: Map<String, String>,
) {
  val scroll = rememberScrollState()
  Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
    Text(teamLabel, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.labelLarge)
    Text(
      text = stringResource(R.string.game_watch_box_player_header),
      style = MaterialTheme.typography.labelSmall,
      fontFamily = FontFamily.Monospace,
      modifier = Modifier.horizontalScroll(scroll),
    )
    lines.sortedByDescending { it.points }.forEach { line ->
      Text(
        text = formatPlayerLine(
          line,
          nameById[line.playerId] ?: line.playerId,
          injuryIds,
        ),
        style = MaterialTheme.typography.bodySmall,
        fontFamily = FontFamily.Monospace,
        modifier = Modifier
          .fillMaxWidth()
          .horizontalScroll(scroll),
      )
    }
  }
}

private fun formatPlayerLine(
    line: PlayerBoxScore,
    label: String,
    midGameInjuryIds: Set<String>,
): String {
    val name = label.take(12).padEnd(12)
  val min = PlayerBoxScoreMinutesLabel.format(line, midGameInjuryIds).padStart(2)
  val pts = line.points.toString().padStart(2)
  val fg = "${line.fieldGoalsMade}-${line.fieldGoalsAttempted}".padStart(5)
  val three = "${line.threePointsMade}-${line.threePointsAttempted}".padStart(5)
  val ft = "${line.freeThrowsMade}-${line.freeThrowsAttempted}".padStart(4)
  val reb = line.rebounds.toString().padStart(2)
  val ast = line.assists.toString().padStart(2)
  val stl = line.steals.toString().padStart(2)
  val blk = line.blocks.toString().padStart(2)
  val to = line.turnovers.toString().padStart(2)
  val pf = line.personalFouls.toString().padStart(2)
  return "$name $min $pts $fg $three $ft $reb $ast $stl $blk $to $pf"
}
