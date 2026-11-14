package dev.novux.dynasty.cbb.core.qa

import dev.novux.dynasty.cbb.core.balance.BalanceLoadResult
import dev.novux.dynasty.cbb.core.balance.BalanceRulesetCodec
import dev.novux.dynasty.cbb.core.calendar.checkpoint.ArchivedPlayerBoxScore
import dev.novux.dynasty.cbb.core.calendar.checkpoint.ArchivedTeamBoxScore
import dev.novux.dynasty.cbb.core.calendar.checkpoint.ProvisionalGameBoxArchive
import dev.novux.dynasty.cbb.core.calendar.checkpoint.ProvisionalGameBoxCodec
import dev.novux.dynasty.cbb.core.league.LeagueStandingsState
import dev.novux.dynasty.cbb.core.league.LeagueTeamRecord
import dev.novux.dynasty.cbb.core.sim.tuning.SimTuningCodec
import dev.novux.dynasty.cbb.core.sim.tuning.SimTuningLoadResult
import java.nio.file.Path
import kotlin.io.path.readText
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * JVM smoke for serialization types shipped in release (R8 keep rules in `proguard-rules.pro`).
 */
class ReleaseSerializationR8SmokeTest {
    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun provisionalBoxArchive_roundTrip_matchesNewGameSimPersistPath() {
        val archive = ProvisionalGameBoxArchive(
            homeBox = sampleTeam("home"),
            awayBox = sampleTeam("away"),
            homePlayerBox = listOf(samplePlayer("p1")),
            awayPlayerBox = emptyList(),
            periodsPlayed = 2,
            overtimePeriods = 1,
        )
        val wire = ProvisionalGameBoxCodec.encode(archive)
        val decoded = ProvisionalGameBoxCodec.decode(wire)
        assertNotNull(decoded)
        assertEquals(archive, decoded)
        assertNotNull(ProvisionalGameBoxArchive.serializer())
    }

    @Test
    fun embeddedBalanceAsset_decodesWithoutSerializerNotFound() {
        val raw = repoRoot().resolve("assets/balance/balance-2026.json").readText()
        val result = BalanceRulesetCodec.parseAndValidate(raw)
        assertTrue(result is BalanceLoadResult.Success)
    }

    @Test
    fun embeddedSimTuningAsset_decodesWithoutSerializerNotFound() {
        val raw = repoRoot().resolve("assets/sim/sim-tuning.json").readText()
        val result = SimTuningCodec.parseAndValidate(raw)
        assertTrue(result is SimTuningLoadResult.Success)
    }

    @Test
    fun leagueStandingsState_roundTrip_matchesLoadPersistPath() {
        val state = LeagueStandingsState(
            seasonIndex = 1,
            records = mapOf(
                "t1" to LeagueTeamRecord(teamId = "t1", wins = 10, losses = 5),
            ),
            lastCommittedRsWeek = 4,
        )
        val wire = json.encodeToString(state)
        val back = json.decodeFromString<LeagueStandingsState>(wire)
        assertEquals(state, back)
    }

  private fun sampleTeam(teamId: String) = ArchivedTeamBoxScore(
        teamId = teamId,
        points = 70,
        fieldGoalsMade = 25,
        fieldGoalsAttempted = 50,
        threePointsMade = 8,
        threePointsAttempted = 20,
        freeThrowsMade = 12,
        freeThrowsAttempted = 15,
        turnovers = 10,
        possessions = 65,
        personalFouls = 14,
    )

    private fun samplePlayer(playerId: String) = ArchivedPlayerBoxScore(
        playerId = playerId,
        minutesSeconds = 1200,
        points = 15,
        fieldGoalsMade = 5,
        fieldGoalsAttempted = 10,
        threePointsMade = 2,
        threePointsAttempted = 4,
        freeThrowsMade = 3,
        freeThrowsAttempted = 4,
        offensiveRebounds = 1,
        defensiveRebounds = 3,
        assists = 2,
        steals = 1,
        blocks = 0,
        turnovers = 1,
        personalFouls = 2,
    )

    private fun repoRoot(): Path {
        var dir = Path.of(System.getProperty("user.dir")).toAbsolutePath()
        while (dir != null && !dir.resolve("assets/balance/balance-2026.json").toFile().exists()) {
            dir = dir.parent
        }
        return dir ?: error("Could not find repo assets from cwd")
    }
}
