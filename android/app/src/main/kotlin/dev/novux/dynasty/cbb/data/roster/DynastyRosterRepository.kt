package dev.novux.dynasty.cbb.data.roster

import dev.novux.dynasty.cbb.core.balance.BalanceFatigue
import dev.novux.dynasty.cbb.core.calendar.CalendarPhase
import dev.novux.dynasty.cbb.core.fatigue.FatigueEngine
import dev.novux.dynasty.cbb.core.injuries.GameSimInjuryEvent
import dev.novux.dynasty.cbb.core.injuries.InjuryApplyContext
import dev.novux.dynasty.cbb.core.injuries.InjuryRosterOps
import dev.novux.dynasty.cbb.core.player.stats.PlayerSeasonStatFinalizeGate
import dev.novux.dynasty.cbb.core.injuries.MedicalRedshirtPolicy
import dev.novux.dynasty.cbb.core.lineup.DefaultStarterAlgorithm
import dev.novux.dynasty.cbb.core.lineup.RosterStarterWireResolver
import dev.novux.dynasty.cbb.core.roster.PlayerPosition
import dev.novux.dynasty.cbb.core.offseason.weekone.DraftStockLine
import dev.novux.dynasty.cbb.core.offseason.weekone.PortalWeekOnePartition
import dev.novux.dynasty.cbb.core.offseason.weekone.ProDayEligibility
import dev.novux.dynasty.cbb.core.offseason.weekone.ProDaySimulator
import dev.novux.dynasty.cbb.core.offseason.weekone.WeekOneLeavingActions
import dev.novux.dynasty.cbb.core.offseason.weekone.WeekOnePortalBootstrap
import dev.novux.dynasty.cbb.core.offseason.weekone.WeekTwoDraftResults
import dev.novux.dynasty.cbb.core.offseason.weektwo.NbaDraftResolution
import dev.novux.dynasty.cbb.core.roster.PlayerClassYear
import dev.novux.dynasty.cbb.core.player.DynastyPlayer
import dev.novux.dynasty.cbb.core.player.nil.PlayerNilSeasonDealEngine
import dev.novux.dynasty.cbb.core.roster.RosterPlayer
import dev.novux.dynasty.cbb.data.injuries.PostgameInjuryQueue
import dev.novux.dynasty.cbb.database.DynastyDatabase
import dev.novux.dynasty.cbb.database.roster.toDomain
import dev.novux.dynasty.cbb.database.roster.toEntity

class DynastyRosterRepository(
    database: DynastyDatabase,
    private val postgameInjuryQueue: PostgameInjuryQueue = PostgameInjuryQueue(),
) {
    private val dao = database.dynastyRosterDao()

    suspend fun listPlayers(dynastyId: String): List<RosterPlayer> =
        dao.listForDynasty(dynastyId).map { it.toDomain() }

    suspend fun listDynastyPlayers(dynastyId: String): List<DynastyPlayer> =
        listPlayers(dynastyId).map { it.toDynastyPlayer() }

    suspend fun prepareWeekOneLeaving(dynastyId: String): List<RosterPlayer> {
        var players = listPlayers(dynastyId)
        val bootstrapped = WeekOnePortalBootstrap.ensureOnePortalEntrantIfEmpty(players)
        if (bootstrapped != players) {
            bootstrapped.forEach { dao.upsert(it.toEntity()) }
            players = bootstrapped
        }
        val withDraft = ensureSeniorProDayEligibility(players)
        if (withDraft != players) {
            withDraft.filter { it.classYear == PlayerClassYear.Senior }.forEach { dao.upsert(it.toEntity()) }
            players = withDraft
        }
        return players
    }

    private fun ensureSeniorProDayEligibility(players: List<RosterPlayer>): List<RosterPlayer> =
        players.map { player ->
            if (player.classYear == PlayerClassYear.Senior && !player.inTransferPortal) {
                player.copy(declaredForDraft = true)
            } else {
                player
            }
        }

    suspend fun runProDay(dynastyId: String, playerId: String): List<RosterPlayer> {
        val player = listPlayers(dynastyId).first { it.playerId == playerId }
        dao.upsert(ProDaySimulator.run(player).toEntity())
        return listPlayers(dynastyId)
    }

    suspend fun simAllProDays(dynastyId: String): List<RosterPlayer> {
        val players = listPlayers(dynastyId)
        val eligible = players.filter { ProDayEligibility.eligible(it) && !it.proDayCompleted }
        ProDaySimulator.simAll(eligible).forEach { dao.upsert(it.toEntity()) }
        return listPlayers(dynastyId)
    }

    fun weekTwoDraftLines(players: List<RosterPlayer>): List<DraftStockLine> =
        WeekTwoDraftResults.fromRoster(players)

    suspend fun cutPlayer(dynastyId: String, playerId: String) {
        dao.deletePlayer(dynastyId, playerId)
    }

    suspend fun upsertPlayer(player: RosterPlayer) {
        dao.upsert(player.toEntity())
    }

    suspend fun applyNilRetentionBump(dynastyId: String, playerId: String, newUsdPerYear: Long): List<RosterPlayer> {
        val player = listPlayers(dynastyId).first { it.playerId == playerId }
        val updated = PlayerNilSeasonDealEngine.applyRetentionBump(player, newUsdPerYear)
        dao.upsert(updated.toEntity())
        return listPlayers(dynastyId)
    }

    suspend fun clearNilDealRaisedFlags(dynastyId: String) {
        listPlayers(dynastyId).forEach { player ->
            if (player.nilDealRaisedThisSeason) {
                dao.upsert(player.copy(nilDealRaisedThisSeason = false).toEntity())
            }
        }
    }

    suspend fun applyNbaDraftResolution(dynastyId: String, resolution: NbaDraftResolution) {
        resolution.playersToRemove.forEach { playerId ->
            dao.deletePlayer(dynastyId, playerId)
        }
        resolution.playersToUpdate.forEach { dao.upsert(it.toEntity()) }
    }

    suspend fun encourageTransfer(dynastyId: String, playerId: String): List<RosterPlayer> {
        val player = listPlayers(dynastyId).first { it.playerId == playerId }
        dao.upsert(WeekOneLeavingActions.encourageTransfer(player).toEntity())
        return listPlayers(dynastyId)
    }

    suspend fun convinceStay(dynastyId: String, playerId: String, stayed: Boolean): List<RosterPlayer> {
        val player = listPlayers(dynastyId).first { it.playerId == playerId }
        dao.upsert(WeekOneLeavingActions.convinceStay(player, stayed).toEntity())
        return listPlayers(dynastyId)
    }

    fun rosterTab(players: List<RosterPlayer>): List<RosterPlayer> =
        PortalWeekOnePartition.onRosterTab(players)

    fun transferTab(players: List<RosterPlayer>): List<RosterPlayer> =
        PortalWeekOnePartition.onTransferTab(players)

    suspend fun normalizeDefaultLineup(dynastyId: String): List<RosterPlayer> {
        val players = listPlayers(dynastyId)
        val starters = DefaultStarterAlgorithm.pickStarters(players)
        val adjusted = DefaultStarterAlgorithm.applyToRoster(players, starters)
        adjusted.forEach { dao.upsert(it.toEntity()) }
        return adjusted
    }

    suspend fun tickInjuriesForWeekAdvance(dynastyId: String, phaseCommitted: CalendarPhase): List<RosterPlayer> {
        val ticked = InjuryRosterOps.tickWeekAdvance(phaseCommitted, listPlayers(dynastyId))
        ticked.players.forEach { dao.upsert(it.toEntity()) }
        return ticked.players
    }

    suspend fun tickFatigueForWeekAdvance(
        dynastyId: String,
        phaseCommitted: CalendarPhase,
        teamPlayedGame: Boolean,
        tuning: BalanceFatigue,
        userGamesThisWeek: Int = 1,
        offensiveScheme: dev.novux.dynasty.cbb.core.strategy.OffensiveSchemePreset =
            dev.novux.dynasty.cbb.core.strategy.OffensiveSchemePreset.Balanced,
        defensiveScheme: dev.novux.dynasty.cbb.core.strategy.DefensiveSchemePreset =
            dev.novux.dynasty.cbb.core.strategy.DefensiveSchemePreset.ManToMan,
    ): List<RosterPlayer> {
        var players = listPlayers(dynastyId)
        if (teamPlayedGame) {
            players = FatigueEngine.applyEstimatedGameDrain(
                players,
                tuning,
                offensiveScheme = offensiveScheme,
                defensiveScheme = defensiveScheme,
                userGamesThisWeek = userGamesThisWeek,
            )
        }
        players = FatigueEngine.applyWeeklyRecovery(players, phaseCommitted, teamPlayedGame, tuning)
        players.forEach { dao.upsert(it.toEntity()) }
        return players
    }

    suspend fun applyGameInjuries(
        dynastyId: String,
        events: List<GameSimInjuryEvent>,
        context: InjuryApplyContext,
    ): List<RosterPlayer> {
        val updated = InjuryRosterOps.applyGameInjuries(listPlayers(dynastyId), events, context)
        updated.forEach { dao.upsert(it.toEntity()) }
        if (events.isNotEmpty()) {
            postgameInjuryQueue.enqueue(dynastyId, events)
        }
        return updated
    }

    fun peekPostgameInjuries(dynastyId: String) = postgameInjuryQueue.peek(dynastyId)

    fun clearPostgameInjuries(dynastyId: String) = postgameInjuryQueue.clear(dynastyId)

    suspend fun recordGamesPlayedAfterCommittedWeek(
        dynastyId: String,
        teamPlayedGame: Boolean,
    ): List<RosterPlayer> {
        val players = listPlayers(dynastyId)
        val recipients = PlayerSeasonStatFinalizeGate.playersReceivingGamePlayed(players, teamPlayedGame)
        if (recipients.isEmpty()) return players
        val recipientIds = recipients.map { it.playerId }.toSet()
        val updated = players.map { player ->
            if (player.playerId in recipientIds) {
                player.copy(rsGamesPlayedSeason = player.rsGamesPlayedSeason + 1)
            } else {
                player
            }
        }
        updated.filter { it.playerId in recipientIds }.forEach { dao.upsert(it.toEntity()) }
        return updated
    }

    suspend fun applyMedicalRedshirt(dynastyId: String, playerId: String): List<RosterPlayer> {
        val player = listPlayers(dynastyId).first { it.playerId == playerId }
        val next = MedicalRedshirtPolicy.apply(player)
        dao.upsert(next.toEntity())
        return listPlayers(dynastyId)
    }

    suspend fun applyStarterLineup(dynastyId: String, starters: Map<PlayerPosition, String>): List<RosterPlayer> {
        val players = listPlayers(dynastyId)
        RosterStarterWireResolver.applyToRoster(players, starters).forEach { dao.upsert(it.toEntity()) }
        return listPlayers(dynastyId)
    }
}
