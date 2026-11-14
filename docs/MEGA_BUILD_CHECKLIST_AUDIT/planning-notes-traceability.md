# Planning notes traceability (`planning-notes.md` → Android)

**Sources (read in full):** [`planning/planning-notes.md`](../../planning/planning-notes.md), [`planning/README.md`](../../planning/README.md)  
**Method:** Map every **Done** / **Resolved** / **design lock** row in `planning-notes.md`, documentation rules, app-update policy, and **Gameplan (locked)** bullets to Android evidence via [`docs/MEGA_BUILD_CHECKLIST_AUDIT/`](../MEGA_BUILD_CHECKLIST_AUDIT/) shards (2026-11-11 static audits).  
**Row IDs:** `MEGA-NOTES-NNN` (topic traceability) · `MEGA-NOTES-META-NNN` (planning file inventory)

**Legend — Status:** `PASS`/`SHIP` aligned · `PARTIAL` incomplete · `GAP`/`FAIL` missing or wrong · `RULE` prose-only engineering rule · `META` file inventory  
**Legend — Pri:** P0 ship-blocker · P1 core loop · P2 depth/polish · P3 meta/doc

---

## A. Documentation rules & build sync

| ID | Pri | Status | Spec quote | Finding vs Android | Evidence |
|----|-----|--------|------------|-------------------|----------|
| MEGA-NOTES-001 | P3 | RULE | “Planning and player-facing copy describe **the game we are building**, not a staged MVP.” | Product strings avoid “v1 ships later”; technical `balanceSchemaVersion` / `simSchemaVersion` remain in save codec. | `DynastySaveCodec.kt`, `strings.xml` grep |
| MEGA-NOTES-002 | P3 | RULE | “Do **not** write ‘v1’, ‘not in v1’, ‘v2 feature’…” | Planning shards audited without MVP deferrals; Android still has `placeholder`/`stub` UX (honest gaps, not scope deferrals). | `MEGA_BUILD_CHECKLIST.md` policy; `recruiting_placeholder_*` strings |
| MEGA-NOTES-003 | P2 | RULE | “Every **Done** topic below must map to **MEGA-\*** rows… all `DONE`/`PASS`” | Tracker marks many topics **Done** while shards record widespread `GAP`/`FAIL` (e.g. recruiting demo pool). | `planning-notes.md` § Settled topics vs `MEGA-RECR-005`, `MEGA-COACH-031` |
| MEGA-NOTES-004 | P2 | RULE | “Update planning, MEGA shard, and phase table in the **same commit** when a topic truly ships.” | This file + 8 shards exist; `BUILD_CHECKLIST.md` phase `[x]` still overstates device truth per MEGA executive summary. | `docs/MEGA_BUILD_CHECKLIST.md` § Executive summary |
| MEGA-NOTES-005 | P0 | RULE | **Load after update** — occupied slots open; calendar coherent | Room migrations + `DynastySaveValidator` path wired; corruption repair checklist 2.5 not exhaustively emulator-tested. | `android/database/migrations`, `ADR-001` |
| MEGA-NOTES-006 | P0 | RULE | **Room changes** — schema bump ships **Migration** | Migrations present per `dynastySchemaVersion`; no silent DROP on occupied slots located in codebase. | `DynastyDatabase.kt`, migration classes |
| MEGA-NOTES-007 | P0 | RULE | **Tuning / balance** — pinned balance + sim revisions at create | Save metadata stores pinned bundle ids per ADR-002/003; week advance does not re-roll NET mid-career. | `DynastyMetadata`, `SaveSlotRepository` |
| MEGA-NOTES-008 | P1 | RULE | **Shipped asset retention** — APK keeps JSON/loader for referenced schema **N** | Embedded `balance-2026.json` + pack asset bundled; multi-version loader mapping not proven for ancient N. | `assets/balance/`, `app/src/main/assets/` |
| MEGA-NOTES-009 | P1 | RULE | **Forward-only features** — new columns default safely | Room additive migrations pattern; new systems attach without rewriting history in typical paths. | Migration review in `:database` |
| MEGA-NOTES-010 | P2 | RULE | **No player copy** — migrations silent | No “save version upgraded” dialog located in UI layer. | grep `version upgraded` in `android/app` |
| MEGA-NOTES-011 | P2 | PASS | **CI (target):** migration tests + load fixture across version bump | `DynastyDatabaseMigrationChainTest`, `DynastyDatabaseMigrationRobolectricTest`, `SaveLoadAfterUpdateFixtureTest`; CI runs `:database:test`. | `android/core/src/test`, `android/database/src/test`, `.github/workflows/core-test.yml` |

---

## B. Core, shell & time (Done topics)

| ID | Pri | Status | Spec quote | Finding vs Android | Evidence |
|----|-----|--------|------------|-------------------|----------|
| MEGA-NOTES-012 | P1 | PARTIAL | Dynasty shell — “one chrome, dashboard task gate, week advance only” | Four-tab `DynastyShellChrome` reused across phases; carousel/signing routes still leave shell. | `MEGA-FRAME-001`, `MEGA-FRAME-007`; `DynastyShellChrome.kt` |
| MEGA-NOTES-013 | P1 | SHIP | Dynasty shell — week advance only (no advance day) | No advance-day control in UI or repository API. | `MEGA-FRAME-008`; `SaveSlotRepository.advanceWeek` |
| MEGA-NOTES-014 | P1 | PARTIAL | Dynasty shell — dashboard task gate | Required tasks block advance; optional cards / badges uneven vs spec. | `MEGA-FRAME-020`–`024`; `DashboardAdvanceGate` |
| MEGA-NOTES-015 | P2 | SHIP | App shell — studio / splash **Done** | Studio intro + splash + Tap to Play route exists before dynasty. | `MEGA-APP-*` shard; `SplashScreen.kt`, `StudioIntroScreen.kt` |
| MEGA-NOTES-016 | P1 | PARTIAL | Game start / new dynasty **Done** | Wizard: coach, school, contract preview largely wired; prestige binding to recruiting weak at create. | `MEGA-RECR-015`; `NewGameWizardScreen.kt` |
| MEGA-NOTES-017 | P1 | SHIP | Time system (weeks/byes) **Done** | `CalendarWeekLabel` encodes exhibition, RS 1–32, conf tourney, postseason 1–7, EoS, offseason 1–10. | `MEGA-FRAME-009`–`014`; `CalendarWeekLabel.kt` |
| MEGA-NOTES-018 | P2 | GAP | Time system — NCBA round names on calendar | Postseason calendar rows lack Opening Round / Super 16 copy from `ncba-branding`. | `MEGA-FRAME-016`; `DynastyShellCalendarTab.kt` |
| MEGA-NOTES-019 | P1 | PARTIAL | Offseason calendar **Done** | `OffseasonScreen` + week headings 1–10; dashboard required cards uneven by week. | `MEGA-FRAME-028`–`030`; `OffseasonScreen.kt` |
| MEGA-NOTES-020 | P1 | PARTIAL | Preseason setup **Done** | Eight required tasks + Start season gate; deep links from task cards thin. | `MEGA-FRAME-025`–`027`; `PreseasonScreen.kt` |
| MEGA-NOTES-021 | P1 | SHIP | End of Season week **Done** | Required recap acknowledge + Enter offseason gate wired per `season-review.md`. | `MEGA-FRAME-033`; `SeasonReviewScreen.kt` |
| MEGA-NOTES-022 | P1 | PARTIAL | Dashboard tasks (master index) **Done** | Gate logic exists for in-season/preseason/offseason subsets; many optional/discipline flows partial. | `dashboard-tasks.md` shard; `DashboardTaskGate.kt` |
| MEGA-NOTES-023 | P1 | PARTIAL | Notifications **Done** | In-app feed composer; not always on offseason/preseason dashboard. | `MEGA-GDD-134`; `NotificationFeedComposer.kt`, `MEGA-FRAME-037` |
| MEGA-NOTES-024 | P1 | PARTIAL | News hub (portal) **Done** | News tab + headlines route; depth vs `news-hub.md` story tiers partial. | `news-shell-branding.md` shard; `NewsHubScreen.kt` |
| MEGA-NOTES-025 | P2 | SHIP | Shell note — no separate offseason/preseason **layout** docs | Single `game-frame.md` chrome; phase content swaps inside shell. | `planning-notes.md` § Gameplan; shell screens share chrome |
| MEGA-NOTES-026 | P2 | SHIP | Dashboard team overview / NET / postseason rows | `DashboardTeamOverviewCard` on shell dashboard. | `MEGA-FRAME-034`–`036`; `game-frame.md` |

---

## C. Coach, strategy & players (Done topics)

| ID | Pri | Status | Spec quote | Finding vs Android | Evidence |
|----|-----|--------|------------|-------------------|----------|
| MEGA-NOTES-027 | P1 | PARTIAL | O/D schemes + halftime **Done** | Strategy hub + halftime sheet exist; rotation minutes / gameplan depth gaps remain. | `MEGA-SIM-*`; `StrategyHubScreen.kt` |
| MEGA-NOTES-028 | P1 | PARTIAL | Positions / default lineup **Done** | Lineup editor + positions wired; interchange UX vs spec partial. | `sim-strategy-lineup-injuries.md`; `LineupScreen.kt` |
| MEGA-NOTES-029 | P0 | PASS | Coach card + School Staff **Done** | HC + assistant tab shells, staff preview flow, `CoachPortraitSlot` on card header and staff rows | `HeadCoachCardScreen.kt`, `AssistantCoachCardScreen.kt`, `SchoolStaffTabPanel.kt` |
| MEGA-NOTES-030 | P1 | PARTIAL | Archetypes + draft #1–60 **Done** | Archetype tags in model/seeders; draft projection UI not fully tabbed on player card. | `coach-player-ui.md`; `PlayerArchetype.kt` |
| MEGA-NOTES-031 | P1 | PARTIAL | Loyalty & promises **Done** | Promise/declare flows in core; portal bias + UI sheets partial. | `player-loyalty-promises` shard; `LoyaltyEngine.kt` |
| MEGA-NOTES-032 | P1 | PARTIAL | Injuries & medical redshirt **Done** | Injury weeks + auto lineup hook; medical redshirt sheet coverage partial. | `injuries.md` shard; `InjuryRepository.kt` |
| MEGA-NOTES-033 | P1 | PARTIAL | Discipline (conduct + academic) **Done** | Discipline routes + dashboard hook; end-to-end required task sheet partial. | `MEGA-FRAME-032`; `DisciplineScreen.kt` |
| MEGA-NOTES-034 | P1 | PARTIAL | Chemistry & fatigue **Done** | Chemistry/fatigue engines in core; player-facing indicators thin. | `news-shell-branding` / in-season shard; `ChemistryEngine.kt` |
| MEGA-NOTES-035 | P0 | PASS | Player card tabs (companion Done: coach/player UI) | Full-screen `PlayerCardScreen` with Overview/Ratings/Stats/Games/History + preview → View more | `PlayerCardTabs.kt`, `PlayerCardScreen.kt`, `DynastyShellScreen.kt` |

---

## D. Recruiting & economy (Done / Resolved topics)

| ID | Pri | Status | Spec quote | Finding vs Android | Evidence |
|----|-----|--------|------------|-------------------|----------|
| MEGA-NOTES-036 | P0 | PASS | Recruiting engine **Done** | Hub, AI week, signing, and sim-week auto-maintain use `IncomingClassSeedRepository.ensurePool` (2k–4k seeder). | `MEGA-RECR-005` PASS; GDD-111 |
| MEGA-NOTES-037 | P0 | PASS | Recruit interest model **Done** — weights in JSON | Per-prospect `interestDelta` from balance applied on DM/call/house/bonus/offer/visit; hub shows `liveInterest`. | `RecruitingInterestSpendRepository`, `balance-2026.json` |
| MEGA-NOTES-038 | P2 | PASS | Recruiting interest open questions **Resolved** | Resolved prose in `recruiting-engine.md`; Android implements subset (Recommended row, influence board partial). | `recruiting-engine.md` § Resolved decisions |
| MEGA-NOTES-039 | P1 | PASS | Recruiting point actions **Done** | Costs from balance; per-target point ledger via `recordProspectSpend` ([MEGA-QA-068]). | `RecruitingProspectPointSpendLedger` |
| MEGA-NOTES-040 | P1 | GAP | Recruiting dashboard tasks **Done** (triggers) | In-season optional recruiting tasks not emitted from `DashboardTaskGate`. | `MEGA-RECR-042`; `DashboardTaskGate.inSeason` |
| MEGA-NOTES-041 | P1 | PARTIAL | AI recruiting **Done** — parity; **hidden** from UI | AI uses same persisted class pool as user; parity vs user interest spend not fully audited. | `AiRecruitingRepository.kt` |
| MEGA-NOTES-042 | P0 | PASS | Transfer portal **Done** | Portal tab: Top 40 strip + Recommended rows with shared points/NIL/actions (OS weeks 2–5). | `PortalRecruitProspectMapper`, `RecruitingHubScreen.kt` |
| MEGA-NOTES-043 | P1 | PARTIAL | Portal dashboard tasks **Done** | Offseason portal tasks in spec; wiring partial vs recruiting signing handoff. | `recruiting-transfer-start.md` § Portal |
| MEGA-NOTES-044 | P1 | PARTIAL | NIL annual pool **Done** | Collective pool in balance + game-design shard; program pool binding at dynasty create partial. | `MEGA-GDD-*` NIL rows; `balance-2026.json` |
| MEGA-NOTES-045 | P1 | PARTIAL | NIL Budget / deal depth **Done** — **3× ask** cap | Budget workflow in core/docs; full deal depth UI partial. | `game-design.md` § NIL Budget; `NilBudgetRepository.kt` |
| MEGA-NOTES-046 | P1 | GAP | Recruit rankings UI — Top 60 full list | National rankings built from demo pool order, not dynasty seed Top 60. | `MEGA-RECR-008`; `NationalRecruitingRankings.kt` |
| MEGA-NOTES-047 | P1 | PASS | RS schedule cap — **32** games; **28** builder + 4-game event | `LeagueScheduleSymmetricEngine` + `LeagueScheduleIntegrityAudit`; `ScheduleWeekAllocator` prevents user double-books. | `LeagueScheduleSymmetricEngine.kt`, `PreseasonScheduleSolver.kt` |

---

## E. Postseason, history & world (Done topics)

| ID | Pri | Status | Spec quote | Finding vs Android | Evidence |
|----|-----|--------|------------|-------------------|----------|
| MEGA-NOTES-048 | P1 | PARTIAL | Postseason selection **Done** | `SelectionSundayEngine` + field sizes; census UX thin. | `postseason-awards-history.md`; `SelectionSundayEngine.kt` |
| MEGA-NOTES-049 | P1 | PARTIAL | Selection engine (poll + seed) **Done** | NET/polls update in league batch; tiebreaker stack not applied to standings input. | `MEGA-WORLD-050`; `NcbaNetRankingEngine.kt` |
| MEGA-NOTES-050 | P1 | PARTIAL | Postseason UI (March Mayhem) **Done** | March Mayhem hub routes exist; round branding partially hardcoded vs pack keys. | `MEGA-WORLD-019`, `035`; `PostseasonHubScreen.kt` |
| MEGA-NOTES-051 | P1 | SHIP | NCBA / NCI / early events **Done** (data) | Early event catalog + universe names in core; NCBA strings in resources. | `MEGA-WORLD-034`, `033`; `EarlySeasonEventCatalog.kt` |
| MEGA-NOTES-052 | P1 | PARTIAL | GOAT / History depth **Done** | History hub screens + record book scaffolding; depth vs `history-hub.md` partial. | `postseason-awards-history.md`; `HistoryHubScreen.kt` |
| MEGA-NOTES-053 | P1 | PARTIAL | Season win contribution (GOAT input) **Done** — hidden | Win contribution summed in sim core; not exposed in UI (matches spec). | `simulation-engine.md` § season-win-contribution; sim shard |
| MEGA-NOTES-054 | P1 | PARTIAL | Player awards (**Team MVP**, **NCBA MVP**, All-*) **Done** | Awards hub + NCBA labels; simple UI per spec; conference honors partial. | `awards.md` shard; `AwardsHubScreen.kt` |
| MEGA-NOTES-055 | P1 | SHIP | League branding — **365** programs; reclass postseason rule | Default pack 365 teams; 4 reclass `postseasonEligible: false`. | `MEGA-WORLD-001`, `043`; `DefaultPackAssetTest` |
| MEGA-NOTES-056 | P2 | GAP | League — reclass transition year rollover | Pack stores `reclassTransitionYear`; no dynasty-year eligibility flip in Kotlin. | `MEGA-WORLD-046`; `league-branding.md` |

---

## F. Engineering meta (Done / Rule topics)

| ID | Pri | Status | Spec quote | Finding vs Android | Evidence |
|----|-----|--------|------------|-------------------|----------|
| MEGA-NOTES-057 | P2 | RULE | Implementation build order **Done** | `BUILD_CHECKLIST.md` phase order documented; MEGA shards supersede honest `[x]` state. | `docs/BUILD_CHECKLIST.md`, `MEGA_BUILD_CHECKLIST.md` |
| MEGA-NOTES-058 | P0 | PASS | App updates vs saves — **Rule** | ADR-001 migration chain + save-load fixture gates in unit/CI; pinned balance/sim retained in fixture. | `ADR-001-dynasty-save-backend.md`; `DynastyDatabaseMigrations.kt` |

---

## G. Gameplan (locked)

| ID | Pri | Status | Spec quote | Finding vs Android | Evidence |
|----|-----|--------|------------|-------------------|----------|
| MEGA-NOTES-059 | P1 | PARTIAL | Pre-game: schemes, rotation, usage; halftime same knobs | Strategy hub + halftime; rotation minutes UX incomplete. | `strategy-schemes.md` shard; `HalftimeSheet.kt` |
| MEGA-NOTES-060 | P1 | PASS | Coach **default** schemes on coach card Overview | Overview tab shows default O/D scheme labels on HC card | `HeadCoachCardScreen.kt` |
| MEGA-NOTES-061 | P1 | PASS | Edit prep before sim + halftime; replay before week advance | Week advance blocked until games resolved via task gate / sim flows. | `MEGA-FRAME-020`; `SimWeekAdvanceDialog.kt` |
| MEGA-NOTES-062 | P1 | PASS | After week advance, that week’s results final | `SaveSlotRepository` advances calendar monotonically; no re-open prior week UI. | `SaveSlotRepository.advanceWeek` |
| MEGA-NOTES-063 | P2 | SHIP | Offseason/preseason define **tasks** gating Dashboard advance (not separate layouts) | `OffseasonScreen`/`PreseasonScreen` use same shell + dashboard gate pattern. | `planning-notes.md` § Gameplan; `MEGA-FRAME-001` |

---

## H. Cross-cutting — topics marked Done but dependent specs

| ID | Pri | Status | Spec quote | Finding vs Android | Evidence |
|----|-----|--------|------------|-------------------|----------|
| MEGA-NOTES-064 | P1 | PARTIAL | Simulation engine possession sim (supports shell Done) | Smoke-tested possession sim; narration/clutch/watch depth gaps. | `sim-strategy-lineup-injuries.md` P0/P1 table |
| MEGA-NOTES-065 | P1 | PASS | Signing Day → preseason transition (recruiting Done) | `SigningDayPreseasonRecruitingTransition` gates S1 OS W1 ledger, reopens W2, closes + seeds next pool on preseason entry. | `SigningDayPreseasonRecruitingTransition.kt`, `SaveSlotRepository.kt` |
| MEGA-NOTES-066 | P2 | PASS | Notifications — no OS push (README + notes Done) | Feed-only; no FCM/notification permission flow in manifest for dynasty feed. | `notifications.md`; `AndroidManifest.xml` |
| MEGA-NOTES-067 | P1 | PARTIAL | In-season systems discipline/chemistry (Done) | Core sim hooks exist; dashboard surfacing uneven. | `MEGA-FRAME-032`, `037`; discipline/chemistry repos |
| MEGA-NOTES-068 | P2 | SHIP | Balance JSON wins for numbers (README rule) | UI parsers load `balance-2026.json` for recruiting/discipline/NIL costs. | `ADR-002`; `BalanceJsonLoader.kt` |
| MEGA-NOTES-069 | P1 | GAP | Game preview — Ratings + RPI (listed adjacent to Done strategy) | Preview card partial; RPI/NET integration on dashboard missing. | `MEGA-FRAME-034`–`035`; `NextGameCard.kt` |
| MEGA-NOTES-070 | P2 | GAP | Watch sim — 2s possession delay (adjacent tuning row) | Watch mode pacing not locked to 2s delay in sim UI. | `sim-strategy-lineup-injuries.md` watch section |
| MEGA-NOTES-071 | P2 | PARTIAL | NIL on player card Overview (adjacent topic) | NIL line on card partial vs recruiting commit overview valuation. | `player-cards.md` shard |
| MEGA-NOTES-072 | P1 | SHIP | Conference tournaments — real formats (tracker: Real formats) | Conf tournament templates per pack conf; generic bracket generator. | `MEGA-WORLD-016`–`017`; `ConferenceTournamentEngine.kt` |
| MEGA-NOTES-073 | P2 | PARTIAL | News — headlines; Top 60 board (tracker cross-ref) | Headlines ship; Top 60 tied to broken pool path (MEGA-NOTES-046). | `MEGA-RECR-008`; `NewsHubScreen.kt` |
| MEGA-NOTES-074 | P2 | META | Still tuning section — interest weights, Recommended bands | Proposed JSON weights in balance; not all deltas wired to spend. | `README.md` § Still tuning; `balance-2026.json` `recruiting.interest.*` |
| MEGA-NOTES-075 | P1 | PARTIAL | Season review → offseason handoff (EoS Done) | Recap prestige feeds next recruiting cycle when called; first-cycle prestige still wrong. | `MEGA-RECR-025`, `015`; `SeasonReviewRepository.kt` |
| MEGA-NOTES-076 | P1 | PARTIAL | Transfer portal shared budget with recruiting (Done) | Economy ledger shared in core; portal UI placeholder blocks player validation. | `MEGA-RECR-036`; `PortalEconomyRepository.kt` |
| MEGA-NOTES-077 | P2 | SHIP | 32 conferences in live universe (league Done companion) | Pack + tests assert 32 `conf-*` rows. | `MEGA-WORLD-002`; `embedded-default.json` |
| MEGA-NOTES-078 | P1 | PARTIAL | Awards — win contribution hidden in UI (Done) | Contribution used in award selection logic; labels simple in UI per spec. | `awards.md` shard; award engines |
| MEGA-NOTES-079 | P2 | PARTIAL | Identity URL pack signing (Still tuning in README) | URL import + allowlist ships; manifest signature verification gap. | `MEGA-WORLD-056`; `PackUrlPolicy.kt` |
| MEGA-NOTES-080 | P1 | PARTIAL | Batch sim world (supports time/postseason Done) | `LeagueWeekBatchSimEngine` simulates RS week for 365 teams within perf test budget. | `MEGA-WORLD-067`, `070`; batch tests |
| MEGA-NOTES-081 | P2 | PASS | Planning index — flat `.md` in `planning/` | Repo uses flat planning folder; links stable from `docs/` and ADRs. | `planning/README.md` § Layout |
| MEGA-NOTES-082 | P2 | META | Document history — tracker maintained | `planning-notes.md` history table through 2026-11-09; this traceability row 2026-11-11. | `planning-notes.md` § Document history |

---

## I. Planning file inventory (`planning/*.md` × 33)

| ID | Pri | Status | Spec quote | Finding vs Android | Evidence |
|----|-----|--------|------------|-------------------|----------|
| MEGA-NOTES-META-001 | P3 | META | `2026-NCAA-Directory.md` | exists ✓ · README index ✓ · shard `league-world-pack.md` | Pack census tests vs directory totals |
| MEGA-NOTES-META-002 | P3 | META | `README.md` | exists ✓ · self-index (not linked from own tables) · shard — (index only) | `planning/README.md` |
| MEGA-NOTES-META-003 | P3 | META | `app-shell.md` | exists ✓ · README ✓ · shard `news-shell-branding.md` | Splash/studio screens |
| MEGA-NOTES-META-004 | P3 | META | `awards.md` | exists ✓ · README ✓ · shard `postseason-awards-history.md` | `AwardsHubScreen.kt` |
| MEGA-NOTES-META-005 | P3 | META | `branding.md` | exists ✓ · README ✓ · shard `news-shell-branding.md` | `strings.xml`, `CreditsScreen.kt` |
| MEGA-NOTES-META-006 | P3 | META | `coach-card.md` | exists ✓ · README ✓ · shard `coach-player-ui.md` | `CoachCardScreen.kt` |
| MEGA-NOTES-META-007 | P3 | META | `dashboard-tasks.md` | exists ✓ · README ✓ · shard `frame-dashboard-time.md` | `DashboardTaskGate.kt` |
| MEGA-NOTES-META-008 | P3 | META | `game-design.md` | exists ✓ · README ✓ · shard `game-design.md` | GDD cross-links + `MEGA-GDD-*` |
| MEGA-NOTES-META-009 | P3 | META | `game-frame.md` | exists ✓ · README ✓ · shard `frame-dashboard-time.md` | `DynastyShellScreen.kt` |
| MEGA-NOTES-META-010 | P3 | META | `game-start.md` | exists ✓ · README ✓ · shards `recruiting-transfer-start.md`, `coach-player-ui.md` | `NewGameWizardScreen.kt` |
| MEGA-NOTES-META-011 | P3 | META | `history-hub.md` | exists ✓ · README ✓ · shard `postseason-awards-history.md` | `HistoryHubScreen.kt` |
| MEGA-NOTES-META-012 | P3 | META | `in-season-systems.md` | exists ✓ · README ✓ · shard `news-shell-branding.md` | discipline/chemistry/fatigue |
| MEGA-NOTES-META-013 | P3 | META | `injuries.md` | exists ✓ · README ✓ · shard `sim-strategy-lineup-injuries.md` | `InjuryRepository.kt` |
| MEGA-NOTES-META-014 | P3 | META | `league-branding.md` | exists ✓ · README ✓ · shard `league-world-pack.md` | `embedded-default.json` |
| MEGA-NOTES-META-015 | P3 | META | `ncba-branding.md` | exists ✓ · README ✓ · shard `league-world-pack.md` | NCBA strings + selection engine |
| MEGA-NOTES-META-016 | P3 | META | `news-hub.md` | exists ✓ · README ✓ · shard `news-shell-branding.md` | `NewsHubScreen.kt` |
| MEGA-NOTES-META-017 | P3 | META | `notifications.md` | exists ✓ · README ✓ · shard `news-shell-branding.md` | `NotificationFeedComposer.kt` |
| MEGA-NOTES-META-018 | P3 | META | `offseason.md` | exists ✓ · README ✓ · shard `frame-dashboard-time.md` | `OffseasonScreen.kt` |
| MEGA-NOTES-META-019 | P3 | META | `planning-notes.md` | exists ✓ · README ✓ · shard — (tracker; this file) | `planning/planning-notes.md` |
| MEGA-NOTES-META-020 | P3 | META | `player-archetypes.md` | exists ✓ · README ✓ · shard `coach-player-ui.md` | archetype model/seeders |
| MEGA-NOTES-META-021 | P3 | META | `player-cards.md` | exists ✓ · README ✓ · shard `coach-player-ui.md` | `PlayerCardScreen.kt` |
| MEGA-NOTES-META-022 | P3 | META | `player-loyalty-promises.md` | exists ✓ · README ✓ · shard `coach-player-ui.md` | loyalty/declare flows |
| MEGA-NOTES-META-023 | P3 | META | `positions-and-lineup.md` | exists ✓ · README ✓ · shard `sim-strategy-lineup-injuries.md` | lineup UI |
| MEGA-NOTES-META-024 | P3 | META | `postseason-frame.md` | exists ✓ · README ✓ · shard `postseason-awards-history.md` | March Mayhem UI |
| MEGA-NOTES-META-025 | P3 | META | `postseason.md` | exists ✓ · README ✓ · shard `postseason-awards-history.md` | postseason engines |
| MEGA-NOTES-META-026 | P3 | META | `preseason.md` | exists ✓ · README ✓ · shard `frame-dashboard-time.md` | `PreseasonScreen.kt` |
| MEGA-NOTES-META-027 | P3 | META | `recruiting-engine.md` | exists ✓ · README ✓ · shard `recruiting-transfer-start.md` | recruiting core/UI |
| MEGA-NOTES-META-028 | P3 | META | `season-review.md` | exists ✓ · README ✓ · shard `postseason-awards-history.md` | `SeasonReviewScreen.kt` |
| MEGA-NOTES-META-029 | P3 | META | `selection-engine.md` | exists ✓ · README ✓ · shard `postseason-awards-history.md` | `SelectionSundayEngine.kt` |
| MEGA-NOTES-META-030 | P3 | META | `simulation-engine.md` | exists ✓ · README ✓ · shard `sim-strategy-lineup-injuries.md` | `PossessionSimEngine.kt` |
| MEGA-NOTES-META-031 | P3 | META | `strategy-schemes.md` | exists ✓ · README ✓ · shard `sim-strategy-lineup-injuries.md` | strategy hub |
| MEGA-NOTES-META-032 | P3 | META | `time-system.md` | exists ✓ · README ✓ · shard `frame-dashboard-time.md` | `CalendarWeekLabel.kt` |
| MEGA-NOTES-META-033 | P3 | META | `transfer-portal.md` | exists ✓ · README ✓ · shard `recruiting-transfer-start.md` | portal core + placeholder UI |

---

## Summary counts

| Bucket | Row IDs | Count |
|--------|---------|------:|
| Documentation / app-update rules | `MEGA-NOTES-001`–`011` | 11 |
| Core, shell & time | `MEGA-NOTES-012`–`026` | 15 |
| Coach, strategy & players | `MEGA-NOTES-027`–`035` | 9 |
| Recruiting & economy | `MEGA-NOTES-036`–`047` | 12 |
| Postseason, history & world | `MEGA-NOTES-048`–`056` | 9 |
| Engineering meta | `MEGA-NOTES-057`–`058` | 2 |
| Gameplan (locked) | `MEGA-NOTES-059`–`063` | 5 |
| Cross-cutting / companion | `MEGA-NOTES-064`–`082` | 19 |
| Planning file inventory | `MEGA-NOTES-META-001`–`033` | 33 |
| **Total traceability rows** | | **115** |

**Topic rows (`MEGA-NOTES-001`–`082`, excluding META):** **82**  
**Inventory rows (`MEGA-NOTES-META-*`):** **33**  
**Grand total:** **115**

---

**Document history**

| Date | Change |
|------|--------|
| 2026-11-11 | Initial traceability matrix from full read of `planning-notes.md` + `README.md`; evidence from 8 MEGA audit shards + Android tree. |
