package dev.novux.dynasty.cbb.database.roster

import dev.novux.dynasty.cbb.core.player.stats.PlayerCountingStats

fun PlayerSeasonStatsEntity.toTotals(): PlayerCountingStats = PlayerCountingStats(
    points = points,
    assists = assists,
    rebounds = rebounds,
    steals = steals,
    blocks = blocks,
    turnovers = turnovers,
    fieldGoalsMade = fieldGoalsMade,
    fieldGoalsAttempted = fieldGoalsAttempted,
    threePointsMade = threePointsMade,
    threePointsAttempted = threePointsAttempted,
    freeThrowsMade = freeThrowsMade,
    freeThrowsAttempted = freeThrowsAttempted,
)
