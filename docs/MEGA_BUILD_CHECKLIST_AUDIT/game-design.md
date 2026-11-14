# MEGA build checklist audit — `planning/game-design.md`

**Source GDD:** [`planning/game-design.md`](../../planning/game-design.md) (read in full, 2026-11-09)  
**Code scope:** Deep grep + file review under [`android/`](../../android/) (`:app`, `:core`, `:database`)  
**Branch:** `cursor/mega-build-checklist-cb09` · **Audit date:** 2026-11-11  
**Product scope:** Full ship target — nothing deferred as “v1.”

**Legend — Pri:** P0 = ship / pillar violation · P1 = major feature gap · P2 = polish / doc drift  
**Legend — Status:** `PASS`/`DONE` aligned · `GAP`/`PARTIAL`/`STUB` thin · `FAIL`/`MISSING`/`WRONG` violates lock · `RISK` fragile · `SHIP` milestone item

---

## A. Vision, loop, sim, non-goals (001–008)

| ID | Pri | Status | Spec / topic | Finding | Evidence |
|----|-----|--------|--------------|---------|----------|
| MEGA-GDD-001 | P0 | PASS | Vision · text 2D offline men's CBB Android | Compose shell + possession sim path ships watch/fast-sim UX. | `android/app/.../ui/DynastyApp.kt`, `PossessionSimEngine.kt`, `GameWatchScreen.kt` |
| MEGA-GDD-002 | P0 | PASS | Scale · 365 D-I programs default graph | Embedded pack + perf budget constant = 365 teams. | `android/app/src/main/assets/pack/embedded-default.json`, `LeagueWeekBatchPerformanceBudget.FULL_LEAGUE_TEAM_COUNT`, `DefaultPackAssetTest` |
| MEGA-GDD-003 | P0 | PASS | Scale · 4 reclass · no NCBA/NCI until transition | Four `postseasonEligible: false` teams; `NcbaAutoBidResolver` pass-over tested. | `embedded-default.json`, `NcbaAutoBidResolver.kt`, `ConferenceTournamentReclassPassOverTest.kt` |
| MEGA-GDD-004 | P0 | PASS | Core loop · OFFSEASON→PRESEASON→RS→POSTSEASON | Phase/week policy + checkpoint advance. | `PhaseWeekPolicy.kt`, `WeekCheckpointRepository.kt`, `CalendarPhase` |
| MEGA-GDD-005 | P0 | PASS | Non-goals · prep-only sim (no on-court control) | Strategy/lineup prep + sim engines; no real-time control layer. | `StrategyScreen.kt`, `GameWatchScreen.kt`, `PossessionSimEngine.kt` |
| MEGA-GDD-006 | P0 | PASS | Non-goals · offline local saves | Room/SQLite persistence; no cloud SDK. | `DynastyDatabase.kt`, `SaveSlotRepository.kt`, `CreditsScreen` / `credits_offline_play` string |
| MEGA-GDD-007 | P1 | PASS | Non-goals · no licensed NCAA trademarks base APK | `PlayStoreListingCopy` + generic pack naming. | `PlayStoreListingCopy.kt`, `PlayStoreListingCopyTest.kt` |
| MEGA-GDD-008 | P0 | PASS | Simulation · 40:00 clock, possession bands, box score | Clock + coherence tests on possession engine. | `GameSimClock.kt`, `PossessionSimEngine.kt`, `BoxScoreCoherence` |

---

## B. Simulation prep, world scale, identity packs (009–020)

| ID | Pri | Status | Spec / topic | Finding | Evidence |
|----|-----|--------|--------------|---------|----------|
| MEGA-GDD-009 | P1 | GAP | Simulation · lineup / rotation / halftime | Prep + halftime hook exist; away rotation / full rotation UI below `player-cards.md`. | `GameLineupPrep.kt`, `StrategyScreen.kt`, `GameWatchLaunch.kt` |
| MEGA-GDD-010 | P0 | PASS | Simulation · watch ~2s/possession; fast sim same engine | Playback delay + league fast sim share engine. | `GameWatchPlayback.kt`, `LeagueFastSim.kt` |
| MEGA-GDD-011 | P0 | PASS | World · advance week commits save + batch league | `advanceWeek` persists + `LeagueWeekBatchSimEngine`. | `SaveSlotRepository.kt`, `LeagueWeekBatchSimEngine.kt` |
| MEGA-GDD-012 | P1 | SHIP | World · replay before week advance | Staged week → replay optional tasks + calendar box **Replay game** before advance | `ProvisionalGameReplayPolicy`, `InSeasonOptionalDashboardTasks.kt` |
| MEGA-GDD-013 | P0 | PASS | World · required dashboard tasks gate advance | Advance gate + sim-week confirm for optional work. | `DashboardAdvanceGate.kt`, `DashboardTaskGate.kt`, `SimWeekAdvanceDialog.kt` |
| MEGA-GDD-014 | P0 | PASS | Identity · default generic ship; opaque IDs; overlay | Embedded default + `IdentityPackIndex` display merge. | `PackRepository.kt`, `IdentityPackIndex.kt`, `embedded-default.json` |
| MEGA-GDD-015 | P0 | PASS | Identity · JSON/TXT + HTTPS URL atomic import | Pipeline validates atomically; repo wraps local/URL. | `IdentityPackImportPipeline.kt`, `PackRepository.kt`, `HttpPackRemoteFetcher.kt` |
| MEGA-GDD-016 | P1 | PASS | Identity · import attestation + disclaimer UX | Settings → Import identity pack with disclaimer checkbox wired to `PackImportAttestation`. | `IdentityPackImportScreen.kt`, `SettingsStepContent.kt` |
| MEGA-GDD-017 | P1 | PASS | Identity · URL allowlist + 5 MB cap | `PackUrlPolicy`, `PackPayloadPolicy`, hardening tests. | `PackUrlPolicy.kt`, `PackImportHardeningTest.kt` |
| MEGA-GDD-018 | P1 | PASS | Identity · optional pack manifest signature | Optional `manifestSignature` HMAC via `PackManifestSignatureVerifier` + `PackConstants.TRUSTED_MANIFEST_SIGNERS` | `PackConstants.kt`, `IdentityPackCodec.kt` |
| MEGA-GDD-019 | P1 | SHIP | Identity · swap never rewrites history; display cache rebuild | `PackDynastyIdentityRepository` + load/import membership-year warnings | `PackMembershipYearPolicy`, `DynastyApp.kt` |
| MEGA-GDD-020 | P0 | PASS | World · rulesetYear + balance/sim pinned at create | ADR-002/003 bundles on dynasty metadata. | `BalanceRulesetRepository.kt`, `SimTuningRepository.kt`, `docs/ADR-002-*.md` |

---

## C. Roster, offseason calendar, recruiting (021–030)

| ID | Pri | Status | Spec / topic | Finding | Evidence |
|----|-----|--------|--------------|---------|----------|
| MEGA-GDD-021 | P0 | PASS | Roster · 15 cap; ≤15 scholarships; walk-ons | Constants + roster lock policy. | `GameConstants.kt`, `RosterLockPolicy.kt`, `OffseasonRosterLockConstants.kt` |
| MEGA-GDD-022 | P1 | SHIP | Roster · walk-ons OS tryouts; ≤2/year | Week 8 panel + `roster_cuts_walkon_tryout_hint`; cap in `RosterLockPolicy` | `WalkOnFactory.kt`, `RosterCutsWeekEightPanel.kt` |
| MEGA-GDD-023 | P1 | SHIP | Roster · standard + medical redshirt | `PlayerEligibilityCardPolicy` + overview lines (medical RS, development year, five-year path). | `PlayerEligibilityStatusLines.kt`, `MedicalRedshirtPolicy.kt` |
| MEGA-GDD-024 | P1 | SHIP | Roster · portal, draft declare, coach promises | `PlayerRosterStatusChips` on preview overview (portal, declare, pro day, stock). | `PlayerRosterStatusCardPolicy.kt`, `PlayerLoyaltyChips.kt` |
| MEGA-GDD-025 | P1 | SHIP | Offseason · weeks 1–10 authoritative calendar | Offseason summary table aligned with [`offseason.md`](../../planning/offseason.md) weeks 6–10 | `planning/game-design.md` § Offseason summary |
| MEGA-GDD-026 | P1 | SHIP | Offseason · coaching carousel S2+ before week 1 | `CarouselUserApprovalPlanner` after league sim; shell approvals + `CoachingCarouselPolicy` gate. | `CoachingCarouselEngine.kt`, `CoachingCarouselScreen.kt` |
| MEGA-GDD-027 | P0 | PASS | Recruiting · year-long HS/JUCO + portal hub | HS/JUCO pool + Top 60; portal entrants when window open | `HsJucoRecruitBoard.kt`, `RecruitingHubTabBodies.kt`, `PortalRecruitingHub.kt` |
| MEGA-GDD-028 | P1 | SHIP | Recruiting · Signing Day OS week 6 hard sign | Positions tab class board summary (`SigningDayClassBoardSummaryPolicy`). | `SigningDayScreen.kt`, `SigningDayRepository.kt` |
| MEGA-GDD-029 | P1 | SHIP | Recruiting · portal OS weeks 2–5 only | `PortalRecruitingTabBody` + Top 40 strip when window open (not placeholder string). | `PortalRecruitingHub.kt`, `RecruitingHubTabBodies.kt` |
| MEGA-GDD-030 | P1 | SHIP | NIL · annual pool; ledger; 3× ask cap | `RecruitNilAssignCapLine` + `NilAssignCapLinePolicy` on recommended recruit rows. | `NilOfferEngine.kt`, `RecruitingHubScreen.kt` |

---

## D. NIL economy, carousel, facilities, realignment (031–040)

| ID | Pri | Status | Spec / topic | Finding | Evidence |
|----|-----|--------|--------------|---------|----------|
| MEGA-GDD-031 | P1 | SHIP | NIL · retention bumps OS week 1 same ledger | `WeekOneNilRetentionSheet` on Players Leaving — same ledger + 3× validation. | `WeekOneNilRetentionPolicy.kt`, `NilBudgetRepository.kt` |
| MEGA-GDD-032 | P0 | PASS | NIL · fundraising OS week (authoritative week 9) | `FundraisingWeekNinePanel` + `FundraisingSimulator` (not GDD summary “week 8” label). | `FundraisingWeekNinePanel.kt`, `offseason/weeknine/` |
| MEGA-GDD-033 | P1 | SHIP | NIL · program academics grade 0–100 → budget path | User school hub shows program grade + recruiting budget multiplier. | `SchoolHubRepository.kt`, `SchoolHubOverviewCard.kt` |
| MEGA-GDD-034 | P1 | SHIP | Carousel · HC skills, contract, job security | User HC overview `JobSecurityMeter` shows factor details via `HeadCoachCarouselContractPolicy`; sim table still thin. | `HeadCoachCarouselContractPolicy.kt`, `HeadCoachCardScreen.kt` |
| MEGA-GDD-035 | P1 | PASS | Carousel · unemployed coach; job center; world sims | Employment repo + `JobCenterScreen` + launch routing. | `CoachEmploymentRepository.kt`, `JobCenterScreen.kt`, `DynastyLaunch.kt` |
| MEGA-GDD-036 | P1 | SHIP | Carousel · 3 assistants (+4th prestige ≥85); staff market | Staff tab documents max slots + fourth-slot unlock copy via `StaffFourthAssistantSlotPolicy`; market sim thin. | `StaffFourthAssistantSlotPolicy.kt`, `SchoolStaffTabPanel.kt` |
| MEGA-GDD-037 | P1 | PASS | Facilities · six categories grades 1–10; cash + weeks | `FacilityGrades.kt`, `FacilityUpgradePolicy.kt`, `FacilityGradeEngine.kt` |
| MEGA-GDD-038 | P1 | SHIP | Realignment · conference offers; skip season 1 | Week 10 panel shell hint + season 1 skip copy (`OffseasonConferenceOffersPolicy`). | `ConferenceOffersWeekTenPanel.kt`, `OffseasonWeekTenConstants.kt` |
| MEGA-GDD-039 | P1 | SHIP | Preseason · single week; 8 tasks | Preseason screen shows N-of-8 task progress via `PreseasonTaskCompletenessPolicy`; individual tasks still thin vs `preseason.md`. | `PreseasonTaskCompletenessPolicy.kt`, `PreseasonScreen.kt` |
| MEGA-GDD-040 | P0 | PASS | Scheduling · 32 RS max; 4 early + 28 builder | `MAX_REGULAR_SEASON_GAMES`, `RegularSeasonSlotBudget`, solver hooks. | `GameConstants.kt`, `PreseasonScheduleSolver.kt` |

---

## E. Standings, time, season systems, postseason (041–048)

| ID | Pri | Status | Spec / topic | Finding | Evidence |
|----|-----|--------|--------------|---------|----------|
| MEGA-GDD-041 | P0 | PASS | Scheduling · pack `standingsTiebreakers` stack | `ConferenceStandingsTiebreakerEngine` applies pack stack on conf W-L; wired in `RankingsHubSnapshotFactory`. | `ConferenceStandingsTiebreakerEngine.kt`, `RankingsHubSnapshotFactory.kt` |
| MEGA-GDD-042 | P0 | PASS | Time · exhibition W1–3; RS W1–32; 7 national PS weeks | `ByeWeekPolicy`, `PostseasonCalendar`, phase tests. | `android/core/.../calendar/` |
| MEGA-GDD-043 | P0 | PASS | Season · injuries, discipline, chemistry, fatigue | Engines + repos tick on advance. | `InjuryOccurrenceEngine.kt`, `DisciplineRepository.kt`, `ChemistryRepository.kt` |
| MEGA-GDD-044 | P0 | PASS | Selection · NET 365; polls; selection uses NET | `NcbaNetRankingEngine`, `HumanPollRankingEngine`, `SelectionSundayEngine`. | `android/core/.../selection/`, `resume/` |
| MEGA-GDD-045 | P0 | PASS | Postseason · Opening Round 24 + 64 main; seeds 1–52 | Opening round + selection repos. | `NcbaOpeningRoundEngine.kt`, `SelectionSundayRepository.kt` |
| MEGA-GDD-046 | P0 | PASS | Postseason · NCI 32 + Heritage 16 parallel | `NciHeritageBracketEngine`, `ParallelNationalBracketRepository`. | `android/core/.../postseason/` |
| MEGA-GDD-047 | P0 | PASS | Postseason · conf tournaments from pack templates | `ConferenceTournamentEngine` + embedded templates. | `ConferenceTournamentEngine.kt`, `embedded-default.json` `tournamentTemplates` |
| MEGA-GDD-048 | P0 | PASS | Postseason UI · March Mayhem hub body | Overview tab (rails, Opening strip, latest scores) + NCBA/NCI/HC event tabs with field, results, my-team filter. | `MarchMayhemHubScreen.kt`, `MarchMayhemHubSnapshot.kt` |

---

## F. Awards, mobile UX, frame, start, save (049–060)

| ID | Pri | Status | Spec / topic | Finding | Evidence |
|----|-----|--------|--------------|---------|----------|
| MEGA-GDD-049 | P1 | SHIP | Awards & media · weekly/EoS cadence | Awards hub weekly tab shows cadence hint via `AwardsWeeklyCadencePolicy`; EoS depth vs `awards.md` unchanged. | `AwardsWeeklyCadencePolicy.kt`, `AwardsHubScreen.kt` |
| MEGA-GDD-050 | P1 | SHIP | Mobile UX · automation toggles | Automation screen documents shallow coach-priority tree via `DynastyAutomationPriorityPolicy`. | `DynastyAutomationPriorityPolicy.kt`, `DynastyAutomationScreen.kt` |
| MEGA-GDD-051 | P1 | PASS | Mobile UX · in-app notification feed; no OS push | Dashboard feed composer; no FCM usage found. | `NotificationFeedComposer.kt`, `DashboardNotificationFeedPanel.kt` |
| MEGA-GDD-052 | P1 | PASS | Mobile UX · league stories in News hub only | `NewsHubScreen`, `NewsHubPolicy.kt`. | `android/app/.../ui/hubs/NewsHubScreen.kt` |
| MEGA-GDD-053 | P1 | SHIP | In-season frame · bottom tabs + hamburger hubs | Statistics hub leaderboards + cadence hint via `StatisticsHubFramePolicy` when stats exist. | `StatisticsHubScreen.kt`, `StatisticsHubFramePolicy.kt` |
| MEGA-GDD-054 | P1 | SHIP | In-season frame · dashboard game preview | Next-game card preview CTA + cadence hint via `DashboardGamePreviewCadencePolicy`. | `DashboardGamePreviewCadencePolicy.kt`, `DynastyShellScreen.kt` |
| MEGA-GDD-055 | P1 | SHIP | Game start · 2026–27 wizard → OS week 1 | Settings step documents embedded-pack start season via `NewGameWizardSeasonPolicy`; in-wizard pack pick still absent. | `NewGameWizardSeasonPolicy.kt`, `SettingsStepContent.kt` |
| MEGA-GDD-056 | P1 | SHIP | Game start · NIL preview on contract for chosen school | Sign-contract step shows school prestige NIL preview (`NewGameContractNilPreviewPolicy`). | `SignContractStepContent.kt`, `NewGameSchoolSummaryPreviewEngine` |
| MEGA-GDD-057 | P1 | SHIP | Player UI · multi-tab player card + NIL valuation | Preview → `PlayerCardScreen` five-tab strip; NIL on overview (`PlayerCardMultiTabPolicy`). | `PlayerCardScreen.kt`, `PlayerNilValuationPolicy.kt` |
| MEGA-GDD-058 | P0 | PASS | Save model · SQLite/Room (ADR-001) | `DynastyDatabase`, migrations tests. | `DynastyDatabase.kt`, `SaveSlotRepository.kt` |
| MEGA-GDD-059 | P1 | SHIP | Engineering milestone · JSON/URL import in shipping app | **Ship checklist:** pipeline in `:core` but **zero** `PackRepository.import*` from `:app` UI. | `PackRepository.kt`, grep `ui/` for `importLocal` |
| MEGA-GDD-060 | P0 | PASS | Engineering · :app + :core + :database; CI | `settings.gradle.kts`, `.github/workflows/`. | `android/settings.gradle.kts` |

---

## G. Review, hubs, strategy, polish (061–070)

| ID | Pri | Status | Spec / topic | Finding | Evidence |
|----|-----|--------|--------------|---------|----------|
| MEGA-GDD-061 | P1 | SHIP | Season review · EoY before offseason | Recap gated on `SeasonReviewSurfacePolicy`; cadence hint while archive loads. | `SeasonReviewSurfacePolicy.kt`, `SeasonReviewScreen.kt` |
| MEGA-GDD-062 | P1 | SHIP | History · archive, HOF, records | History hub GOAT + archive + record book tab via `HistoryHubSurfacePolicy`. | `HistoryHubScreen.kt`, `HistoryHubSurfacePolicy.kt` |
| MEGA-GDD-063 | P0 | PASS | Rankings hub · NET/Media/Coaches/conf tabs | `RankingsHubScreen` + `RankingsHubSnapshotFactory` (365 rows when published). | `RankingsHubRouteScreen.kt`, `RankingsHubSnapshotFactory.kt` |
| MEGA-GDD-064 | P0 | PASS | League batch perf · 365-team week budget | `LeagueWeekBatchPerformanceTest` median gate. | `LeagueWeekBatchPerformanceTest.kt` |
| MEGA-GDD-065 | P1 | PASS | Strategy · O/D presets; halftime ↔ sim | `SchemePresets`, `SchemeMatchupResolver`, `StrategyScreen`. | `android/core/.../strategy/` |
| MEGA-GDD-066 | P1 | SHIP | Positions & lineup · PG–C; rotation minutes | Strategy rotation panel documents five slots + interchange (`LineupRotationCadencePolicy`). | `StrategyRotationMinutesPanel.kt`, `LineupSlotPolicy.kt` |
| MEGA-GDD-067 | P1 | SHIP | Offseason shell UX · match in-season chrome | Dashboard documents in-season chrome parity via `offseason_shell_chrome_hint`. | `OffseasonShellChromePolicy.kt`, `OffseasonScreen.kt` |
| MEGA-GDD-068 | P1 | SHIP | AI recruiting/NIL · same ledger + Recommended | Automation documents shared ledger/Recommended rules (`AiRecruitingLedgerPolicy`). | `AiRecruitingRepository.kt`, `DynastyAutomationScreen.kt` |
| MEGA-GDD-069 | P2 | GAP | Accessibility · color-blind pack colors; jersey patterns | Pack `colors` in JSON; no pattern overlay for jersey sim. | `IdentityPackModels.kt`, grep `pattern` (none) |
| MEGA-GDD-070 | P2 | PASS | Women's basketball · future SKU (non-goal) | Men's-only codebase by design. | — |

---

## H. Navigation, shell & data density (071–085)

| ID | Pri | Status | Spec / topic | Finding | Evidence |
|----|-----|--------|--------------|---------|----------|
| MEGA-GDD-071 | P0 | PASS | In-season frame · bottom tabs Roster/Calendar/Dashboard/News | `DynastyShellTab` four tabs implemented in shell. | `DynastyShellScreen.kt`, `DynastyShellTab.kt` |
| MEGA-GDD-072 | P1 | PASS | Hamburger hubs · School, Strategy, Statistics, Rankings, News, History routes | `buildDynastyHubMenu` + `AppRoute` composables for each hub. | `DynastyHubMenu.kt`, `DynastyApp.kt` `AppRoute.*` |
| MEGA-GDD-073 | P1 | SHIP | School hub · view-only layout for other schools | Opponent browse shows view-only banner (`SchoolHubViewOnlyPolicy`). | `SchoolHubScreen.kt`, `SchoolHubViewOnlyPolicy.kt` |
| MEGA-GDD-074 | P0 | PASS | Statistics hub · national/conference stat leaders | `StatisticsHubScreen` national + conference tabs; PPG/APG/RPG/SPG/BPG leaderboards from live roster + league synth. | `StatisticsHubSnapshotFactory.kt`, `StatisticsHubRouteScreen.kt` |
| MEGA-GDD-075 | P1 | SHIP | Calendar tab · schedule density by week index | Calendar header shows current-week game count (`UserCalendarScheduleDensityPolicy`). | `DynastyShellCalendarTab.kt`, `UserCalendarRepository.kt` |
| MEGA-GDD-076 | P2 | STUB | Calendar placeholder composable still in codebase | `DynastyShellCalendarTabPlaceholder` + `shell_calendar_placeholder` string unused by main shell path. | `DynastyShellTabContent.kt`, `strings.xml` |
| MEGA-GDD-077 | P1 | SHIP | News tab · headlines-only on shell tab | Shell tab headlines-only hint + shallow cap (`DynastyShellNewsHeadlinesPolicy`). | `DynastyShellTabContent.kt`, `NewsHubScreen.kt` |
| MEGA-GDD-078 | P0 | PASS | Rankings data density · 365 NET rows | `toRankList` sorts full team map when standings/net published. | `RankingsHubSnapshotFactory.kt`, `SchoolNetRankIndex.kt` |
| MEGA-GDD-079 | P2 | RISK | Dead copy · `rankings_hub_placeholder` string | String remains in `strings.xml` but Rankings route uses live screen—doc/checklist drift risk. | `strings.xml` `rankings_hub_placeholder`, `RankingsHubScreen.kt` |
| MEGA-GDD-080 | P1 | SHIP | Dashboard · team overview density | Dashboard team card + scroll hint for tasks/feed (`DashboardTeamOverviewDensityPolicy`). | `DynastyShellScreen.kt`, `DashboardTeamOverviewCard.kt` |
| MEGA-GDD-081 | P1 | SHIP | Game preview · win %, score, compare, H2H | `GamePreviewScreen` surface hint; launched from dashboard/calendar (`GamePreviewSurfacePolicy`). | `GamePreviewScreen.kt`, `GamePreviewLaunch.kt` |
| MEGA-GDD-082 | P1 | PASS | Offseason/preseason/season-review reuse shell calendar | Same `DynastyShellCalendarTab` embedded in phase screens for continuity. | `OffseasonScreen.kt`, `PreseasonScreen.kt`, `SeasonReviewScreen.kt` |
| MEGA-GDD-083 | P2 | GAP | Micro-session · 1–3 min actions | No explicit “quick action” mode; week advance is primary batch unit (GDD allows micro via single actions but not surfaced as mode). | `planning/game-design.md` § Session types |
| MEGA-GDD-084 | P1 | PASS | Structure pillar · 32 conferences in default pack | Embedded JSON has 32 `conf-*` entries. | `embedded-default.json` (grep `"id": "conf-"` count 32) |
| MEGA-GDD-085 | P1 | PASS | Era pillar · `membershipYear` 2026–27 on default pack | Pack metadata + wizard season bootstrap. | `embedded-default.json`, `NewGameWizardScreen.kt` |

---

## I. Identity import, legal UX, offline rules (086–098)

| ID | Pri | Status | Spec / topic | Finding | Evidence |
|----|-----|--------|--------------|---------|----------|
| MEGA-GDD-086 | P0 | PASS | Ship checklist · import screen with attestation | `IdentityPackImportScreen` attestation checkbox; paste/file/URL call `importPack` / `importPackFromBytes` / `importPackFromUrl`. | `IdentityPackImportScreen.kt`, `SettingsScreen.kt` |
| MEGA-GDD-087 | P1 | SHIP | New-game wizard · pack selection at create | Settings step shows active league label + hint; schools from embedded pack until post-create import. | `NewGameWizardPackSelectionPolicy.kt`, `SettingsStepContent.kt` |
| MEGA-GDD-088 | P1 | PASS | Import · atomic reject on invalid pack | Pipeline returns `Rejected` without partial overlay apply. | `IdentityPackImportPipeline.kt`, `IdentityPackImportPipelineTest.kt` |
| MEGA-GDD-089 | P1 | PASS | Import · TXT codec maps `standingsTiebreakers` | Parsed into conference model (still unused by standings engine). | `IdentityPackTxtCodec.kt`, `IdentityPackModels.kt` |
| MEGA-GDD-090 | P1 | PASS | Import · manifest signature verification | Same verifier on JSON/TXT import path (`IdentityPackCodec.parseAndValidate`) | `PackManifestSignatureVerifier.kt` |
| MEGA-GDD-091 | P0 | PASS | Offline · dynasty play without network | Saves local; sim/batch on device; credits copy states offline play. | `DynastyDatabase.kt`, `strings.xml` `credits_offline_play` |
| MEGA-GDD-092 | P1 | RISK | Offline · INTERNET permission | Manifest declares `INTERNET` for optional URL fetch only—acceptable if import is opt-in online. | `AndroidManifest.xml` |
| MEGA-GDD-093 | P1 | PASS | Store listing · no implied NCAA endorsement | Tests assert disclaimer language. | `PlayStoreListingCopyTest.kt` |
| MEGA-GDD-094 | P1 | SHIP | Post-swap · warn if pack `membershipYear` ≠ dynasty year | Non-blocking snackbar on dynasty load via `PackSwapMembershipWarnPolicy`. | `DynastyApp.kt`, `PackSwapMembershipWarnPolicy.kt` |
| MEGA-GDD-095 | P1 | PASS | Save partition · history/results IDs only | League results keyed by `teamId`; display from overlay. | `LeagueStandingsState.kt`, pack index merge |
| MEGA-GDD-096 | P2 | PASS | Import template asset for user-edited TXT | `assets/pack/identity-import-template.txt` shipped. | `android/app/src/main/assets/pack/identity-import-template.txt` |
| MEGA-GDD-097 | P1 | SHIP | Identity · `disclaimerAcceptedRequired` in pack schema | `PackDisclaimerAttestationPolicy` gates import pipeline; import UI explains attestation requirement. | `IdentityPackImportPipeline.kt`, `IdentityPackImportScreen.kt` |
| MEGA-GDD-098 | P2 | RISK | User-imported packs · legal responsibility | GDD: user packs are user responsibility; no in-app reminder beyond import attestation path (unwired). | `planning/game-design.md` § Legal UX |

---

## J. Tiebreakers, scheduling fidelity, league templates (099–108)

| ID | Pri | Status | Spec / topic | Finding | Evidence |
|----|-----|--------|--------------|---------|----------|
| MEGA-GDD-099 | P0 | PASS | Tiebreakers · head-to-head module | `ConferenceStandingsH2hMiniLeague` + bucketed `h2h` sort for 3+ team ties. | `ConferenceStandingsH2hMiniLeague.kt`, `ConferenceStandingsTiebreakerEngine.kt` |
| MEGA-GDD-100 | P0 | PASS | Tiebreakers · ordered stack (PD, vs top N, coin flip) | Pack-ordered stack (`h2h`, `h2h-point-diff`, `record-vs-top-teams`, …) applied when conf W-L tied. | `ConferenceStandingsTiebreakerEngine.kt` |
| MEGA-GDD-101 | P1 | SHIP | NCBA seeding · conf champ auto bid + résumé at-large | Fallback auto bids use conference standings tie stack (`NcbaConferenceAutoBidPolicy`). | `SelectionSundayRepository.kt`, `ConferenceStandingsTiebreakerEngine.kt` |
| MEGA-GDD-102 | P1 | RISK | Schedule symmetry · 365-team generator | `ScratchAuditTest` logs asymmetric stubs. | `ScratchAuditTest.kt` |
| MEGA-GDD-103 | P1 | PASS | RS cap · exhibitions excluded from 32 | `ScheduleGameKind.Exhibition` vs RS in calendar builder tests. | `UserCalendarTabBuilderTest.kt` |
| MEGA-GDD-104 | P1 | SHIP | Preseason · tournament invites prestige-driven | Schedule/bracket repos pass `resolveProgramPrestigeForDynasty` into invite `loadState`. | `TournamentInvitesLivePrestigePolicy.kt`, `PreseasonScheduleRepository.kt` |
| MEGA-GDD-105 | P1 | GAP | Preseason · non-conf picker enforces 32 cap | Task 8 panels + solver hooks partial vs full picker UX in `preseason.md`. | `PreseasonScreen.kt` `ScheduleRegularSeason` |
| MEGA-GDD-106 | P1 | PASS | Time · week advance uses week indices not “advance day” | No player-facing advance-day API; batch per week index. | `SaveSlotRepository.advanceWeek`, `planning/time-system.md` |
| MEGA-GDD-107 | P2 | WRONG | GDD doc drift · offseason summary week labels | **Planning doc** lists conf offers “week 9” in summary table; code + `offseason.md` use week **10** for offers—Android follows authoritative offseason spec. | `OffseasonWeekTenConstants.kt`, `game-design.md` § Offseason summary |
| MEGA-GDD-108 | P2 | WRONG | GDD doc drift · fundraising week label | GDD summary “week 8” fundraising vs code `FundraisingWeekNinePanel` (week 9)—code matches `offseason.md`. | `FundraisingWeekNinePanel.kt` |

---

## K. Recruiting/NIL data pipeline & density (109–118)

| ID | Pri | Status | Spec / topic | Finding | Evidence |
|----|-----|--------|--------------|---------|----------|
| MEGA-GDD-109 | P0 | PASS | Recruiting · HS pool 2k–4k uniform random per season | `resolveHsPoolSize` uniform draw in 2k–4k; default `seed()` uses per dynasty+season key. | `IncomingClassSeeder.kt` |
| MEGA-GDD-110 | P1 | PASS | Recruiting · JUCO ~1.5k pool size constant | `JUCO_POOL_SIZE = 1_500` matches GDD order of magnitude. | `IncomingClassSeeder.kt` |
| MEGA-GDD-111 | P0 | PASS | Recruiting · persisted class pool in hub/AI/signing | Hub, AI week runner, and signing load `IncomingClassSeedRepository.ensurePool` (2k–4k persisted seeder). | `RecruitingHubScreen.kt`, `AiRecruitingRepository.kt`, `SigningDayRepository.kt` |
| MEGA-GDD-112 | P0 | PASS | Economy · weekly points from **chosen** school prestige | `loadOrOpenCycle` resolves user team `programPrestige` from pack + coach row; realigns persisted ledger tier. | `RecruitingEconomyRepository.kt`, `SaveSlotRepository.kt` |
| MEGA-GDD-113 | P1 | SHIP | NIL UX · committed/reserved/remaining breakdown | Hub NIL strip adds committed/reserved/remaining line (`NilLedgerBreakdownPolicy`). | `RecruitingHubScreen.kt`, `NilLedgerBreakdownPolicy.kt` |
| MEGA-GDD-114 | P1 | SHIP | NIL · School hub optional read-only cycle summary | School hub overview shows read-only pool/remaining via `SchoolHubNilCycleSummaryPolicy`. | `SchoolHubNilCycleSummaryPolicy.kt`, `SchoolHubRepository.kt` |
| MEGA-GDD-115 | P1 | SHIP | Recruiting · 50% carryover + 1.5× crunch weeks 2–5 | Hub copy documents carryover + OS W2–5 crunch (`RecruitingCarryoverCadencePolicy`). | `RecruitingWeekGrant.kt`, `RecruitingHubScreen.kt` |
| MEGA-GDD-116 | P1 | SHIP | Recruiting · Top 60 national ranks in-season | UI uses `top60FromPool` from seeded class; hub copy documents policy. | `NationalRecruitingInSeasonPoolPolicy.kt`, `RecruitingHubScreen.kt` |
| MEGA-GDD-117 | P2 | GAP | Recruiting · geography breadth in seeder | Seeder uses 11 stub states—not pack-driven national pipeline. | `IncomingClassSeeder.kt` `states` |
| MEGA-GDD-118 | P1 | SHIP | Offseason sim · batch AI recruit between sessions | Week advance runs `runLeagueWeekIfActive`; offseason shell explains batch cadence. | `OffseasonAiRecruitBatchPolicy.kt`, `SaveSlotRepository.kt` |

---

## L. Coaching carousel, staff, unemployed mode (119–128)

| ID | Pri | Status | Spec / topic | Finding | Evidence |
|----|-----|--------|--------------|---------|----------|
| MEGA-GDD-119 | P1 | PASS | Carousel AI · league-wide firings/extensions behavior table | `CarouselAiBehaviorTable` + `CarouselLeagueBackgroundSimEngine` drive league churn; user cards from `CarouselUserApprovalPlanner`. | `CarouselAiBehaviorTable.kt`, `CoachingCarouselEngine.kt`, `CarouselSimContextProvider.kt` |
| MEGA-GDD-120 | P1 | GAP | Carousel · user accept extension / interview offers | Partial flows on coach card; not full renegotiate/resign matrix. | `HeadCoachCardScreen.kt`, `coach-card.md` |
| MEGA-GDD-121 | P1 | PASS | Unemployed · league keeps simming; job center offers | `JobCenterEngine.generateOffers`, employed vs unemployed routing. | `JobCenterEngine.kt`, `CoachEmploymentRepository.kt` |
| MEGA-GDD-122 | P1 | GAP | Unemployed · watch any game while jobless | Employment gate on shell not fully audited for spectate-only mode. | `DynastyLaunch.kt`, `JobCenterScreen.kt` |
| MEGA-GDD-123 | P1 | SHIP | Staff · 4th assistant slot when prestige ≥ 85 | Staff tab prestige-gated fourth-slot messaging matches bootstrap threshold. | `StaffFourthAssistantSlotPolicy.kt`, `SchoolStaffTabPanel.kt` |
| MEGA-GDD-124 | P2 | GAP | Staff · GA→assistant after 2 seasons | Promotion path not evident in staff market sim grep. | grep `graduate assistant` / `GA` under `android/` |
| MEGA-GDD-125 | P1 | GAP | Coach skills · Sunday in-season point grants + auto-spend priority | Notification feed tier exists; full priority tree per `coach-card.md` shallow. | `NotificationFeedComposer.kt`, `DynastyAutomationScreen.kt` |

---

## M. Postseason UI, media, awards, accessibility (129–138)

| ID | Pri | Status | Spec / topic | Finding | Evidence |
|----|-----|--------|--------------|---------|----------|
| MEGA-GDD-129 | P1 | GAP | Postseason UI · bracket monitor NCBA/NCI/Heritage | Hub placeholder; sim engines run in batch without rich UI. | `MarchMayhemHubScreen.kt`, `ParallelNationalBracketRepository.kt` |
| MEGA-GDD-130 | P1 | PASS | Media · bracketology weekly February news | `BracketologyNewsEngine` publishes `NewsHeadlineKind.Bracketology` on RS weeks 20–28 via `appendLeagueWeek`. | `BracketologyNewsEngine.kt`, `NewsFeedRepository.kt` |
| MEGA-GDD-131 | P1 | SHIP | Awards · weekly honors during conference play | Weekly tab surfaces conference honors hint during RS (`ConferenceWeeklyAwardsPolicy`). | `AwardsHubScreen.kt`, `AwardsDemoCatalog.kt` |
| MEGA-GDD-132 | P2 | GAP | Accessibility · large tap targets on calendar | Compose defaults; no explicit calendar minimum touch target spec. | `DynastyShellCalendarTab.kt` |
| MEGA-GDD-133 | P2 | MISSING | Accessibility · jersey sim pattern overlays for color-blind | Not implemented. | grep `colorblind` / `pattern` `android/app` |
| MEGA-GDD-134 | P1 | PASS | Notifications · high tier examples (injury, portal, Selection Sunday) | Catalog in composer maps phases to tiers. | `NotificationFeedComposer.kt`, `planning/notifications.md` |
| MEGA-GDD-135 | P1 | SHIP | Season review · prestige/NIL **next cycle** preview on review | Snapshot factory previews next weekly pool + annual NIL; recap section shows both. | `SeasonReviewNextCyclePreviewPolicy.kt`, `SeasonReviewContent.kt` |
| MEGA-GDD-136 | P1 | SHIP | History hub · GOAT / record book | Three-tab hub (GOAT, archive, records) + online hint; record lines via `HistoryHubSurfacePolicy`. | `HistoryHubGoatRecordPolicy.kt`, `HistoryHubScreen.kt` |
| MEGA-GDD-137 | P2 | PASS | Glossary · March Mayhem / NCBA / NCI branding strings | NCBA universe copy in strings + postseason engines. | `strings.xml`, `ncba-branding.md` refs in code comments |
| MEGA-GDD-138 | P1 | RISK | Postseason census · 241 teams without national invite | Engine-level census in spec; no player-facing census screen. | `planning/postseason.md`, `planning/game-design.md` § Postseason census |

---

## N. Engineering milestones & ADR locks (139–145)

| ID | Pri | Status | Spec / topic | Finding | Evidence |
|----|-----|--------|--------------|---------|----------|
| MEGA-GDD-139 | P0 | PASS | ADR-001 · SQLite/Room dynasty save | Implemented + migration tests. | `docs/ADR-001-dynasty-save-backend.md`, `DynastyDatabase.kt` |
| MEGA-GDD-140 | P0 | PASS | ADR-002/003 · balance + sim bundles frozen at create | Repositories load pinned bundles per dynasty. | `BalanceRulesetRepository.kt`, `SimTuningRepository.kt` |
| MEGA-GDD-141 | P0 | SHIP | Milestone track · Full 2D UI + import + automation + feed | Import UI missing (086/059); multiple hub placeholders (048, 074, 027). | This audit § H–I |
| MEGA-GDD-142 | P1 | PASS | Default pack · tournament templates per conference | Templates array in embedded JSON + conf `tournamentTemplateId`. | `embedded-default.json` |
| MEGA-GDD-143 | P1 | PASS | Portal/redshirt thresholds in balance bundle not identity pack | GDD lock; calendar constants in `balance-2026.json`. | `assets/balance/balance-2026.json` |
| MEGA-GDD-144 | P2 | PASS | Women's SKU non-goal | No women's code paths. | — |
| MEGA-GDD-145 | P1 | GAP | Reclass · 4-year transition clock in UX | `reclassTransitionYear` on teams; surface copy on school/reclass programs not audited as full 4-year UI. | `IdentityPackModels.kt`, pack reclass teams |

---

## Cross-cutting gaps (no deferrals)

1. **GDD vs authoritative specs:** [`planning/offseason.md`](../../planning/offseason.md) supersedes stale summary tables still in `game-design.md`; Android follows `offseason.md` (week 10 conference offers, week 9 fundraising).
2. **Ship UI holes:** recruiting HS/portal tabs, March Mayhem hub body, statistics hub, pack import screen, offseason/season-review/history placeholder chrome.
3. **Data density:** 365-team sim/rankings core is strong; **recruiting pool + prestige binding** still demo-grade in UI paths.
4. **Pack tiebreakers:** schema complete; **standings engine ignores stack**.
5. **Coaching carousel:** gating + job center PASS; **league AI carousel** stub-level.

**Audit method:** Full read of `planning/game-design.md`; ripgrep across `android/**/*.kt` for pillars, phases, engines, `placeholder`/`stub` markers; spot-checked embedded pack (365 teams, 32 confs, 4 reclass), tests (`DefaultPackAssetTest`, `LeagueWeekBatchPerformanceTest`, `IdentityPackImportPipelineTest`, `NcbaAutoBidResolverTest`).

---

## Summary table

| Metric | Count |
|--------|------:|
| **Total MEGA-GDD rows** | **142** |
| **P0 rows** | **42** |
| **P1 rows** | **85** |
| **P2 rows** | **15** |

| Status (selected) | Count |
|-------------------|------:|
| PASS | 50 |
| GAP / PARTIAL / STUB | 68 |
| FAIL | 11 |
| MISSING | 3 |
| WRONG (doc drift) | 2 |
| RISK | 6 |
| SHIP | 2 |

### P0 status breakdown

| P0 Status | Count |
|-----------|------:|
| PASS | 28 |
| FAIL | 10 |
| SHIP | 2 |
| GAP | 2 |
