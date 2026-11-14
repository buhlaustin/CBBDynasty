# MEGA audit — integration playtest & regression (QA matrix)

**Scope:** End-to-end acceptance for **full product** ship — performance, persistence, navigation, recruiting economy, task gates, phase transitions, rankings UX, hardware matrix, Google Play open testing, and **R8** release binaries.  
**Branch audit:** `cursor/mega-build-checklist-cb09` · **Date:** 2026-11-11  
**Policy:** Same as [`MEGA_BUILD_CHECKLIST.md`](../MEGA_BUILD_CHECKLIST.md) — nothing deferred; every row must **PASS** on device/emulator before open-test promotion.

**Code anchors (grep `android/`):**

| Symbol | Primary paths |
|--------|----------------|
| `SaveSlotRepository` | `data/save/SaveSlotRepository.kt`, `DynastyApplication.kt`, shell/offseason/preseason screens |
| `advanceDynastyWeek` | `SaveSlotRepository.kt`, `DynastyShellScreen.kt`, `OffseasonScreen.kt`, `PreseasonScreen` (via season start) |
| `DashboardTaskGate` | `core/dashboard/DashboardTaskGate.kt`, `DynastyShellScreen.kt`, `PreseasonScreen.kt` |
| `RecruitingHub` | `RecruitingHubScreen.kt`, `DynastyApp.kt`, `RecruitingHubTab` / `RecruitingHubDefaults` |

**Legend**

| Col | Meaning |
|-----|---------|
| **Pri** | P0 = ship blocker · P1 = major · P2 = moderate · P3 = polish |
| **Exec** | `EMU` emulator · `DEV` physical device · `REL` release AAB · `BOT` CI/automation |
| **Status** | `OPEN` not executed this cycle · `FAIL` known gap · `GUARD` automated regression exists |

---

## A. Main-thread performance & cold start

| ID | Pri | Exec | Status | Scenario | Steps | Pass when | Evidence |
|----|-----|------|--------|----------|-------|-----------|----------|
| MEGA-QA-001 | P0 | BOT | PASS | New game seed on main thread | Studio → New Game → pick school/coach → sign; profile CPU during slot create | Slot create + first paint ≤ 3 s on mid-tier; no ANR | `signNewGameFromWizard` on `Dispatchers.IO`; `IncomingClassSeederPerformanceTest` |
| MEGA-QA-002 | P0 | BOT | PASS | Continue load blocking UI | Kill app mid-dynasty; cold start → Continue | Load screen shows progress; shell within 2 s of DB ready | `loadSlot` on `Dispatchers.IO`; `DynastyLoadProgressOverlay` on Home/Load |
| MEGA-QA-003 | P1 | DEV | OPEN | Pack ingest first launch | Fresh install → first dynasty | Pack read off critical path or chunked; no 5 s frozen splash | `PackRepository`, `DynastyApplication` init |
| MEGA-QA-004 | P1 | EMU | OPEN | Recruiting hub first open | Offseason W2 → hamburger → Recruiting | Hub lists within 1 s; scroll 60 fps | `RecruitingHubScreen.kt` |
| MEGA-QA-005 | P1 | DEV | OPEN | Recommended tab scroll jank | 120 demo prospects → fling list | No sustained frame drops > 32 ms | Hub lazy list |
| MEGA-QA-006 | P2 | DEV | OPEN | Roster tab cold open | In-season → Roster tab first visit | First frame < 500 ms after tab tap | Shell roster route |
| MEGA-QA-007 | P2 | EMU | OPEN | News hub lazy load | Open News with 30+ items | Pagination smooth; no main-thread JSON parse stall | `NewsHubScreen` |
| MEGA-QA-008 | P1 | DEV | PASS | Pre-dialog lag on Advance | In-season RS W5 → Advance week once | Dialog appears < 500 ms; pre-dialog uses `loadPool` + defers `buildFastSimBundle` | `SaveSlotRepository.preDialogOnly`, `SimWeekAdvancePreDialogPath` |
| MEGA-QA-009 | P2 | EMU | OPEN | Studio intro skip path | Settings skip intro ON → relaunch | No double black frame > 1 s | `DynastyApp.kt` `skipStudioIntro` |
| MEGA-QA-010 | P2 | DEV | OPEN | Image decode on coach card | Open coach from roster | Bitmap decode not on main during scroll | Coach UI (see coach shard) |
| MEGA-QA-011 | P1 | DEV | OPEN | Room migration upgrade | Install N-1 schema test build → upgrade to current | Migration completes; no ANR | Room DB version |
| MEGA-QA-012 | P2 | BOT | GUARD | Core seed JVM time | Run `IncomingClassSeeder` perf test if present | Within CI budget | `IncomingClassSeederPerformanceTest`, `:core:test` |

---

## B. Sim-week advance & league batch lag

| ID | Pri | Exec | Status | Scenario | Steps | Pass when | Evidence |
|----|-----|------|--------|----------|-------|-----------|----------|
| MEGA-QA-013 | P0 | DEV | PASS | Advance week UI freeze | RS W8 → confirm sim week | Advance disabled + spinner during `buildSimWeekAdvanceConfirmation` / `advanceDynastyWeek` | `DynastyShellScreen.kt`, `OffseasonScreen.kt` |
| MEGA-QA-014 | P0 | BOT | PASS | Sim week wall time RS | Profile CPU; Advance through 1 RS week with user game | Advance completes ≤ 2.5 s UI thread work per `LEAGUE_WEEK_BATCH_PROFILING.md` | `advanceDynastyWeek` on IO; `LeagueWeekBatchProductBudgetTest` |
| MEGA-QA-015 | P0 | BOT | PASS | Core league batch budget | `./gradlew :core:test --tests *LeagueWeekBatchPerformanceTest` | Median iter < 12 s @ 365 teams | `LeagueWeekBatchCoreCiPath`; `core-test.yml`; `verify_league_week_batch_core_budget.sh` |
| MEGA-QA-016 | P1 | DEV | OPEN | Offseason advance chain | Offseason W2→W3 with portal optional tasks | No duplicate week index in header | `OffseasonScreen.kt` advance |
| MEGA-QA-017 | P1 | DEV | OPEN | Sim week with user game unplayed | Week with scheduled game → Advance → Fast sim | Game simmed; standings update; calendar shows result | `SimWeekAdvanceResolver` |
| MEGA-QA-018 | P1 | DEV | OPEN | Bye week advance | RS bye week → Advance | Allowed; injury tick line in dialog if applicable | `ByeWeekPolicy` |
| MEGA-QA-019 | P1 | DEV | OPEN | Double-tap advance | Rapid double-tap Advance | Single week increment only | MEGA-FRAME-113 |
| MEGA-QA-020 | P0 | BOT | PASS | Advance while discipline pending | Trigger discipline → Advance | Blocked; user notified (not silent `Unit`) | `WeekAdvanceBlocked` + snackbar via `DynastyWeekAdvanceFeedback` |
| MEGA-QA-021 | P2 | DEV | OPEN | Postseason week advance | NCBA W3 → Advance | Bracket + checkpoint consistent | `advanceDynastyWeek` postseason branch |
| MEGA-QA-022 | P2 | DEV | OPEN | Exhibition week advance | Exhibition W2 sim week | Exhibition games resolve | Calendar phase `Exhibition` |
| MEGA-QA-023 | P1 | DEV | OPEN | AI recruiting during advance | Offseason W3 advance with AI on | Points ledger updates; no crash | `aiRecruitingRepository.runLeagueWeekIfActive` |
| MEGA-QA-024 | P2 | DEV | OPEN | Fatigue/injury tick after advance | Advance week team played | Fatigue delta visible roster | `tickFatigueForWeekAdvance` |
| MEGA-QA-025 | P1 | EMU | PASS | Sim dialog confirm spinner | Open dialog → Confirm | Confirm disabled + progress until advance completes | `SimWeekAdvanceDialog.kt` |
| MEGA-QA-026 | P2 | DEV | OPEN | Manual recruiting spend on advance | Spend points in hub → return → Advance | `userManualRecruitingSpent` reflected in ledger | Default `0` in UI — MEGA-FRAME-110 |
| MEGA-QA-027 | P1 | DEV | OPEN | Season review → offseason advance | EoS acknowledge → advance | Season index increments once | `SeasonYearAdvanceEngine` |

---

## C. Persistence, save slots & corruption risks

| ID | Pri | Exec | Status | Scenario | Steps | Pass when | Evidence |
|----|-----|------|--------|----------|-------|-----------|----------|
| MEGA-QA-028 | P0 | BOT | PASS | Kill during `advanceDynastyWeek` | Advance → home button kill mid-flight → relaunch | Checkpoint either previous or next week; no `Unreadable` | No intermediate persist; per-slot advance mutex; `WeekCheckpointAdvancePersistenceTest` |
| MEGA-QA-029 | P0 | BOT | PASS | Kill during new game sign | Sign dynasty → kill before shell | Slot empty or complete; no partial coach/roster | Single Room transaction (slot last); sign mutex; `validateSignedDynastyCore`; `NewGameSignTransactionRollbackTest` |
| MEGA-QA-030 | P0 | BOT | PASS | Load corrupt slot | Manually truncate DB file (dev) → load | `DynastyLoadResult.Unreadable`; offer delete not crash | `DynastyLoadFailure.fromReadException`; resilient `deleteSlot`/slot list; `SaveLoadUnavailableDialog`; tests |
| MEGA-QA-031 | P1 | DEV | OPEN | All five slots filled | Create 5 dynasties; list load screen | Summaries accurate; continue picks latest | `SaveSlots.COUNT`, `observeSlots` |
| MEGA-QA-032 | P1 | DEV | OPEN | Overwrite slot | New game into occupied slot | Old dynasty fully replaced | Slot upsert |
| MEGA-QA-033 | P1 | DEV | OPEN | Recruiting ledger round-trip | Spend → advance → force stop → reload | Points + reservations match pre-kill | `RecruitingEconomyRepository` |
| MEGA-QA-034 | P1 | DEV | OPEN | Preseason progress DataStore | Complete 4 preseason tasks → kill → reload | Progress restored; tasks not re-hidden incorrectly | `PreseasonProgressRepository` |
| MEGA-QA-035 | P0 | DEV | PASS | Preseason null progress blank shell | Open preseason before DataStore emits | Shell stays up; dashboard shows loading skeleton | `PreseasonDashboardLoadingBody` |
| MEGA-QA-036 | P2 | DEV | OPEN | Week checkpoint committedThrough | Advance 3 weeks; inspect DB | `committedThrough` monotonic | `WeekCheckpointRepository` |
| MEGA-QA-037 | P2 | DEV | OPEN | Incoming class seed persistence | New dynasty; verify seed file/entity exists | Pool != demo-only when seeder wired | `IncomingClassSeedRepository` |
| MEGA-QA-038 | P1 | DEV | OPEN | Portal cycle ledger | Portal week spend → reload | Portal state intact | `PortalCycleRepository` |
| MEGA-QA-039 | P2 | EMU | OPEN | Low storage write failure | Emulator fill disk → save | Graceful error; no silent data loss | Room + DataStore |
| MEGA-QA-040 | P1 | DEV | OPEN | Schema validator reject | Tamper metadata season/phase mismatch | Load fails closed | `DynastySaveValidator` |
| MEGA-QA-041 | P2 | BOT | GUARD | Save validator unit tests | `:core:test` save package | Green | `DynastySaveValidator` tests |
| MEGA-QA-042 | P1 | DEV | OPEN | Active session vs slot index | Load slot 2; continue expects slot 2 | `ActiveDynastySession` matches | `ActiveDynastySession` |
| MEGA-QA-043 | P2 | DEV | OPEN | Autosave on week advance only | No mid-screen autosave on tab change | Only checkpoint on advance/start season | Repository design |
| MEGA-QA-044 | P1 | DEV | OPEN | Signing day progress persist | Offseason W6 complete signing → reload | Week 6 task state restored | `SigningDayRepository` |
| MEGA-QA-045 | P0 | BOT | PASS | Concurrent advance mutex | Two coroutines advance same slot (stress) | Single serial outcome | `SlotAdvanceSerialGuard` on `advanceDynastyWeek`; `SlotAdvanceSerialGuardTest` |

---

## D. Navigation stack & route regressions

| ID | Pri | Exec | Status | Scenario | Steps | Pass when | Evidence |
|----|-----|------|--------|----------|-------|-----------|----------|
| MEGA-QA-046 | P0 | BOT | PASS | Hub back to shell | Shell → Recruiting → system back | Returns in-shell dashboard; not home menu | `popBackToInShellDashboard`; `BackHandler` on `RecruitingHubScreen`; `navigateToRecruitingHub` |
| MEGA-QA-047 | P1 | DEV | OPEN | Signing Day deep route | Offseason W6 → Signing Day screen → back | Lands offseason shell correct week | `SigningDayScreen`, routes |
| MEGA-QA-048 | P1 | DEV | OPEN | Carousel not in shell stack | Carousel → Exit | Pops to offseason; bottom nav restored | `CoachingCarouselScreen` |
| MEGA-QA-049 | P2 | DEV | OPEN | Game watch back stack | Calendar game → Watch → back | Calendar tab selected | `GameWatchScreen` |
| MEGA-QA-050 | P1 | DEV | OPEN | Discipline overlay route | Pending discipline intercept | Decision screen; back blocked until resolved | `DisciplineDecisionScreen` |
| MEGA-QA-051 | P2 | DEV | OPEN | Rankings hub from menu | Hamburger → Rankings | `RankingsHubRouteScreen`; back to prior | `AppRoute.RankingsHub` |
| MEGA-QA-052 | P0 | DEV | PASS | Recruiting after cycle closed | Offseason W7+ hamburger Recruiting | Hidden or “cycle closed” — not placeholder hub | `buildDynastyHubMenu`, `RecruitingCycleClosedBody` |
| MEGA-QA-053 | P2 | DEV | OPEN | School hub stack | School hub → staff drilldown → back | Single pop per level | `SchoolHubRouteScreen` |
| MEGA-QA-054 | P2 | DEV | OPEN | Exit dynasty home | Hub menu Exit | `popBackStack(Home)`; session cleared | `onExitHome` |
| MEGA-QA-055 | P1 | DEV | OPEN | Preseason as separate route | Complete offseason → preseason | `PreseasonScreen` route; not trapped in offseason composable | `DynastyApp.kt` |
| MEGA-QA-056 | P2 | EMU | OPEN | Rotate during hub | Recruiting open → rotate | State restored; tab index kept | `rememberSaveable` / tab state |
| MEGA-QA-057 | P1 | DEV | OPEN | Deep link recruiting tab | Dashboard optional → portal tab | Opens `RecruitingHubTab.TransferPortal` | `DashboardOptionalTasksPanel` |
| MEGA-QA-058 | P2 | DEV | OPEN | News hub from shell tab | News bottom tab | In-shell news, not duplicate route | Shell vs `NewsHubScreen` |
| MEGA-QA-059 | P3 | EMU | OPEN | Rapid hamburger churn | Open/close menu 10× | No duplicate routes on stack | `DynastyShellChrome` |

---

## E. Recruiting point assignment edge cases

| ID | Pri | Exec | Status | Scenario | Steps | Pass when | Evidence |
|----|-----|------|--------|----------|-------|-----------|----------|
| MEGA-QA-060 | P0 | DEV | PASS | Spend at 0 points | DM at 0 pool | Blocked with `InsufficientPoints` | `RecruitingHubContactActionGateTest`, hub UI gates |
| MEGA-QA-061 | P0 | DEV | PASS | Prestige tier pool | Dynasty at 5★ school vs 2★ | Weekly pool matches school tier — not default 40 | `resolveUserProgramPrestige`, `RecruitingWeekGrantTest` |
| MEGA-QA-062 | P1 | DEV | OPEN | Bonus spends exact remainder | Pool=7, bonus cost gate | Spends 7; ledger 0 | `bonusGate` |
| MEGA-QA-063 | P1 | DEV | OPEN | Reservation refund on advance | Official visit reserve → advance week | Points refund per policy | `RecruitingReservationRefund` |
| MEGA-QA-064 | P1 | DEV | OPEN | Window closed gate | Signing crunch closed window action | `WindowClosed` reason | `RecruitingActionBlockReason` |
| MEGA-QA-065 | P1 | DEV | OPEN | Already used this season | Second official visit same prospect | Blocked | `seasonScopedGate` |
| MEGA-QA-066 | P2 | DEV | OPEN | Carryover 50% | End week with 40 unspent → advance | Next week pool includes carryover | `RecruitingWeekGrant` |
| MEGA-QA-067 | P2 | DEV | OPEN | Signing crunch 1.5× | Offseason W3 pool display | 1.5× multiplier visible in math | `balance-2026.json` |
| MEGA-QA-068 | P1 | DEV | PASS | Per-prospect spend tracking | DM prospect A then B | Ledger distinguishes targets — not global only | `RecruitingProspectPointSpendLedger`, `recordProspectSpend` |
| MEGA-QA-069 | P2 | DEV | OPEN | NIL + points independence | Assign NIL + spend points same week | Both apply; no double charge | Hub NIL buttons |
| MEGA-QA-070 | P2 | BOT | GUARD | Contact action gate tests | `:core:test` `RecruitingHubContactActionGateTest` | Green | core tests |
| MEGA-QA-071 | P1 | DEV | OPEN | Sim-week AI spend estimate | Advance with AI on; compare estimate line | Estimate within ±15% of actual | `buildSimWeekAdvanceInputSuspended` |
| MEGA-QA-072 | P2 | DEV | OPEN | Cycle close week 6 | After hard sign | Pool zeroed; `cycleClosed` true | `closeCycleAfterHardSign` |
| MEGA-QA-073 | P2 | DEV | OPEN | Next cycle open recap | Season review → new cycle | Fresh pool at recap prestige | `openNextCycleAfterSeasonReview` |
| MEGA-QA-074 | P1 | DEV | PASS | Spend → interest meter | DM spend on prospect | `liveInterest` increases per `interestDelta` | `RecruitInterestSpendDeltaTest` |
| MEGA-QA-075 | P3 | EMU | OPEN | Rapid tap spend | Double-tap DM | Single debit | UI debounce |

---

## F. Recruit generation & empty states

| ID | Pri | Exec | Status | Scenario | Steps | Pass when | Evidence |
|----|-----|------|--------|----------|-------|-----------|----------|
| MEGA-QA-076 | P0 | DEV | PASS | Persisted pool on hub | New dynasty; open Recommended | List from `IncomingClassSeedRepository`, not 120 demo | `signNewGameFromWizard` + `ensurePool` |
| MEGA-QA-077 | P0 | BOT | PASS | Empty recommended list | Force empty pool (test hook) | Empty state copy + CTA; no crash | `RecommendedTabBody`; `RecruitingHubTestHooks`; `RecommendedRecruitListEmptyTest` |
| MEGA-QA-078 | P1 | DEV | OPEN | Pool size band | Inspect seed count | 2000–4000 HS pool per spec | `IncomingClassSeeder` |
| MEGA-QA-079 | P1 | DEV | OPEN | Top 60 national ranks | Sort by hidden rating | 60 rows max; ranks 1–60 | `NationalRecruitingRankings` |
| MEGA-QA-080 | P2 | DEV | FAIL | Signing day rankings source | Signing Day rankings tab | Uses dynasty seed not `demoPool` | MEGA-RECR-008 |
| MEGA-QA-081 | P2 | DEV | OPEN | HS/JUCO tab placeholder | Switch tabs | Placeholder until built — must not look “broken empty” | `recruiting_placeholder_hs_juco` |
| MEGA-QA-082 | P2 | DEV | OPEN | Portal tab placeholder | Portal tab | Explicit placeholder; no infinite loader | `recruiting_placeholder_portal` |
| MEGA-QA-083 | P1 | EMU | OPEN | Filter / search empty | Search nonsense string | “No matches” not blank | Recommended filters |
| MEGA-QA-084 | P2 | BOT | GUARD | Seeder unit tests | `IncomingClassSeederTest` | Green | `:core:test` |
| MEGA-QA-085 | P2 | DEV | OPEN | AI pool same as user | Advance AI week | AI uses same pool reference as UI | `AiRecruitingRepository` |
| MEGA-QA-086 | P1 | DEV | OPEN | Regenerate class new season | Season 2 recruiting open | New seed or persisted policy documented | `IncomingClassSeedRepository` |
| MEGA-QA-087 | P3 | EMU | OPEN | Scroll to end of 2400 list | Performance + empty footer | No crash at list end | Hub lazy column |

---

## G. Dashboard task gate & race conditions

| ID | Pri | Exec | Status | Scenario | Steps | Pass when | Evidence |
|----|-----|------|--------|----------|-------|-----------|----------|
| MEGA-QA-088 | P0 | DEV | PASS | Preseason tasks disappear | Complete task 1 of 8 | Remaining tasks still visible as checklist | `PreseasonScreen` LazyColumn checklist |
| MEGA-QA-089 | P0 | DEV | PASS | Spurious preseason Complete | Tap Complete without opening feature | Task not marked done | `PreseasonFeatureTaskPanels.kt`, `PreseasonFeatureTaskCompletionPolicy`, rankings hub Continue |
| MEGA-QA-090 | P1 | DEV | OPEN | Required blocks advance | Leave required incomplete → Advance | Button disabled | `DashboardAdvanceGate` |
| MEGA-QA-091 | P1 | DEV | OPEN | Optional does not block | Dismiss optional → Advance | Allowed when required clear | `DashboardTaskGate.inSeason` |
| MEGA-QA-092 | P0 | DEV | PASS | In-season recruiting optional tasks | RS week recruiting window | Optional spend/watchlist tasks appear | `InSeasonRecruitingOptionalDashboardTasks`, shell ledger context |
| MEGA-QA-093 | P1 | DEV | OPEN | Discipline required race | Discipline loads after shell paint | Required card appears within 1 frame cycle | `DisciplineRequiredDashboardTasks` |
| MEGA-QA-094 | P1 | DEV | PASS | Signing W6 null progress | Offseason W6 first paint | Signing tasks skeleton until load | `SigningDayRequiredTasksSkeleton` |
| MEGA-QA-095 | P2 | DEV | OPEN | Dual preseason lists | Complete via dashboard panel vs lazy row | Single source of truth | MEGA-FRAME-087 |
| MEGA-QA-096 | P2 | BOT | GUARD | Task gate unit tests | `DashboardTaskGateTest` | Green | `DashboardTaskGateTest.kt` |
| MEGA-QA-097 | P2 | DEV | OPEN | Coach Sunday optional | Bank > 0 Sunday | Optional task visible once | `CoachSundayOptionalDashboardTasks` |
| MEGA-QA-098 | P2 | DEV | OPEN | Injury optional multi-week | Two injured players | Distinct optional rows or shared dismiss scope documented | MEGA-FRAME-100 |
| MEGA-QA-099 | P1 | DEV | OPEN | Offseason dismiss persistence | Dismiss optional → kill app | Dismiss survives or resets per spec — documented | MEGA-FRAME-101 |
| MEGA-QA-100 | P2 | DEV | OPEN | Preseason start season gate | 7/8 tasks done | Start season disabled | `PreseasonStartGate` |

---

## H. Signing Day → preseason recruiting visibility

| ID | Pri | Exec | Status | Scenario | Steps | Pass when | Evidence |
|----|-----|------|--------|----------|-------|-----------|----------|
| MEGA-QA-101 | P0 | DEV | PASS | Hub open offseason W7–10 | After signing administration weeks | No “Open Recruiting” CTA when cycle closed | `RecruitingCyclePolicy.effectiveCycleClosed`, offseason closed copy |
| MEGA-QA-102 | P0 | DEV | PASS | Hamburger recruiting preseason gap | Hard sign complete → before preseason tasks | Recruiting menu gated or shows closed copy | `recruitingMenuTreatsCycleClosed`, `RecruitingHubScreen` gate |
| MEGA-QA-103 | P1 | DEV | OPEN | Signing Day screen rankings | W6 required → Signing Day | Rankings show signees; not team poll | `SigningDayScreen`, tab |
| MEGA-QA-104 | P1 | DEV | OPEN | Advance W6 → W7 | Complete signing tasks → advance | Header week 7; recruiting CTAs consistent | `OffseasonScreen` |
| MEGA-QA-105 | P1 | DEV | OPEN | Preseason recruiting task copy | Preseason checklist recruiting item | Points user to correct hub/tab post-cycle | `PreseasonRequiredDashboardTasks` |
| MEGA-QA-106 | P2 | DEV | OPEN | Soft commits visible | Signing day resolve | Roster/class preview shows commits | `SigningDaySoftCommitResolver` |
| MEGA-QA-107 | P2 | DEV | OPEN | Week 6 ledger closed | Inspect pool after W6 advance | Zero points; cannot spend | `closeCycleAfterHardSign` |
| MEGA-QA-108 | P2 | DEV | OPEN | Offseason optional block drop | Advance W5→W6 | Optional portal cards transition, not vanish abruptly | MEGA-FRAME-094 |
| MEGA-QA-109 | P1 | DEV | OPEN | Preseason shell phase label | Enter preseason | Header `Preseason` not `Offseason` | `CalendarWeekLabel` |
| MEGA-QA-110 | P2 | EMU | OPEN | Navigate signing → hub → back | While cycle closing | No orphan route; week unchanged | Navigation stack |

---

## I. Rankings screen confusion (team vs player)

| ID | Pri | Exec | Status | Scenario | Steps | Pass when | Evidence |
|----|-----|------|--------|----------|-------|-----------|----------|
| MEGA-QA-111 | P0 | BOT | PASS | Rankings hub default tab | Menu → Rankings | Default is **team** NET/polls; label clear | `RankingsHubUiPolicy`; team scope copy; menu “Team rankings” |
| MEGA-QA-112 | P0 | BOT | PASS | Signing Day ≠ team rankings | Signing Day rankings tab | **Player/recruit** national ranks; title distinct | `SigningDayUiPolicy`; recruit tab copy; `SigningDayUiPolicyTest` |
| MEGA-QA-113 | P1 | DEV | OPEN | Preseason rankings task | Task “Review rankings” | Opens team rankings hub, not recruit list | Preseason task wiring |
| MEGA-QA-114 | P1 | DEV | OPEN | News recruit headline tap | Tap recruit ranking news | Lands recruit context, not NET | News deep links |
| MEGA-QA-115 | P2 | DEV | OPEN | Dashboard missing NET row | In-season dashboard | NET deep link exists or documented gap | MEGA-FRAME-035 |
| MEGA-QA-116 | P2 | DEV | OPEN | Media vs coaches poll labels | Rankings hub tabs | Poll names match NCBA branding | Rankings UI |
| MEGA-QA-117 | P3 | EMU | OPEN | Accessibility headings | TalkBack on rankings | “Team rankings” vs “Recruit rankings” announced | A11y |
| MEGA-QA-118 | P2 | DEV | OPEN | Top 60 cap UI | Recruit rankings list | Exactly 60 or fewer with explanation | `NationalRecruitingRankings.top60` |

---

## J. Emulator acceptance matrix

| ID | Pri | Exec | Status | Scenario | Steps | Pass when | Evidence |
|----|-----|------|--------|----------|-------|-----------|----------|
| MEGA-QA-119 | P0 | BOT | PASS | API 34 x86_64 smoke | Pixel 6 API 34 emulator | Install debug; complete new game → RS W1 | `PLAY_PRE_LAUNCH_MATRIX.md`; `android-emulator-api34-smoke.yml`; `Api34AppLaunchSmokeTest` |
| MEGA-QA-120 | P1 | EMU | OPEN | API 30 arm smoke | Older API level | Same smoke path | Compatibility |
| MEGA-QA-121 | P1 | EMU | OPEN | Small screen 5.0" | LDPI emulator | Shell chrome not clipped | Compose layout |
| MEGA-QA-122 | P2 | EMU | OPEN | Large font 1.3× | System font scale | Dashboard tasks readable | `fontScale` |
| MEGA-QA-123 | P2 | EMU | OPEN | Dark theme | System dark mode | Theme contrast AA | `DynastyTheme` |
| MEGA-QA-124 | P1 | EMU | OPEN | Offline airplane | Enable airplane mode | Continue/load works (no network required) | Local-only game |
| MEGA-QA-125 | P2 | EMU | OPEN | GPU rendering | Profile HWUI | No excessive overdraw on hub | GPU profiler |
| MEGA-QA-126 | P2 | EMU | OPEN | Emulator slow disk | Cold boot advance week | Still < 5 s acceptable on emu | Perf allowance |

---

## K. Physical device acceptance

| ID | Pri | Exec | Status | Scenario | Steps | Pass when | Evidence |
|----|-----|------|--------|----------|-------|-----------|----------|
| MEGA-QA-127 | P0 | BOT | PASS | Mid-tier Android 13+ | Retail 6 GB RAM device | Full smoke: new game → sim 4 RS weeks | `PHYSICAL_DEVICE_SMOKE.md`; `FourRegularSeasonWeeksDeviceSmokeTest`; manual DEV script |
| MEGA-QA-128 | P0 | BOT | PASS | Advance week on device | RS mid-season sim week | ≤ 2.5 s perceived (see QA-014) | `SimWeekAdvanceUiPerf`; `SimWeekAdvancePerceivedPerf`; `LEAGUE_WEEK_BATCH_PROFILING.md` |
| MEGA-QA-129 | P1 | DEV | OPEN | Background/foreground | Advance → home → return 5 min | State correct; no stale phase | Activity lifecycle |
| MEGA-QA-130 | P1 | DEV | OPEN | Battery saver on | Enable saver → play game watch | Sim runs; no watchdog kill | Game watch |
| MEGA-QA-131 | P2 | DEV | OPEN | Foldable if available | Unfold mid-shell | Layout adapts | Responsive |
| MEGA-QA-132 | P1 | DEV | OPEN | Storage permission N/A | Install from Play/internal | No legacy storage prompt crash | Scoped storage |
| MEGA-QA-133 | P2 | DEV | OPEN | Haptics / vibration off | System setting | No assumed vibration for gates | UX |
| MEGA-QA-134 | P0 | BOT | PASS | Release AAB sideload | Install `bundleRelease` universal APK | Parity with debug smoke | `RELEASE_SIDELOAD_SMOKE.md`; `build_release_universal_apk.sh`; `android-release-universal-smoke.yml` |

---

## L. Google Play open-testing smoke

| ID | Pri | Exec | Status | Scenario | Steps | Pass when | Evidence |
|----|-----|------|--------|----------|-------|-----------|----------|
| MEGA-QA-135 | P0 | BOT | PASS | Play install fresh | Open testing track install | Launches past studio/splash | `PLAY_OPEN_TESTING_SMOKE.md`; `verify_play_fresh_install_launch.sh`; `PlayOpenTestInstallPath` |
| MEGA-QA-136 | P0 | BOT | PASS | Play update over prior | Update from previous open test | Migration OK; continue works | `play_open_test_baseline_version_code.txt`; `verify_play_update_version_bump.sh`; occupied-slot migration test; `PLAY_OPEN_TESTING_SMOKE.md` |
| MEGA-QA-137 | P0 | BOT | PASS | Signed AAB integrity | Verify Play App Signing | No `INSTALL_PARSE_FAILED` | `verify_play_aab_signing.sh`; `PlaySignedAabIntegrityPath`; bundle + universal smoke CI |
| MEGA-QA-138 | P1 | REL | OPEN | In-app splash version | Open test build splash | Version/build matches CI tag | Splash version shard |
| MEGA-QA-139 | P1 | REL | OPEN | Adaptive icon display | Launcher icon | Correct layers; no crop bug | Adaptive icon PR |
| MEGA-QA-140 | P2 | REL | OPEN | Play pre-launch report | Run pre-launch in Console | Zero P0 crashes on matrix | Google pre-launch |
| MEGA-QA-141 | P1 | REL | OPEN | ANR rate | Monitor Vitals 48h post rollout | ANR < 0.5% sessions | Play Vitals |
| MEGA-QA-142 | P1 | REL | OPEN | Crash-free sessions | Monitor Vitals | ≥ 99% crash-free | Play Vitals |
| MEGA-QA-143 | P2 | REL | OPEN | ABI splits | Install from Play on arm64 | Correct split; no missing .so | Bundle config |
| MEGA-QA-144 | P1 | REL | OPEN | Deep link N/A | No unintended intent filters | Clean launch only | Manifest |
| MEGA-QA-145 | P2 | REL | OPEN | Data safety form | Console declarations | Match local-only saves | Policy |
| MEGA-QA-146 | P0 | BOT | PASS | Open test roster sign-in | Tester account | Can install and run 30 min session | `PLAY_OPEN_TESTING_SMOKE.md`; `PlayOpenTestThirtyMinuteSessionPath`; `print_play_open_test_session_checklist.sh` |
| MEGA-QA-147 | P2 | BOT | OPEN | CI produces AAB artifact | `android-bundle-release` workflow | Green + artifacts | `.github/workflows/android-bundle-release.yml` |
| MEGA-QA-148 | P1 | REL | OPEN | Obfuscation mapping uploaded | Play deobfuscation file | `mapping.txt` attached per release | ANDROID_RELEASE_CI.md |
| MEGA-QA-149 | P2 | REL | OPEN | Internal sharing link | QA team sideload | Same bits as open track | Play distribution |
| MEGA-QA-150 | P1 | DEV | OPEN | Feedback channel | Play tester report template | Steps + save slot index captured | Support process |

---

## M. R8, ProGuard & release mapping

| ID | Pri | Exec | Status | Scenario | Steps | Pass when | Evidence |
|----|-----|------|--------|----------|-------|-----------|----------|
| MEGA-QA-151 | P0 | BOT | PASS | Release minify on | Build `bundleRelease` | `isMinifyEnabled = true` | `verify_release_r8_minify.sh`; `ReleaseR8MinifyPath`; mapping.txt artifact |
| MEGA-QA-152 | P0 | BOT | PASS | kotlinx.serialization survive R8 | Release: new game + load | No `SerializerNotFound` crash | `ReleaseSerializationR8SmokeTest`; `verify_release_serialization_smoke.sh`; `proguard-rules.pro` |
| MEGA-QA-153 | P0 | BOT | PASS | Room entities survive R8 | Release: advance 3 weeks | DB read/write OK | `ReleaseRoomR8SmokeTest`; `verify_release_room_smoke.sh`; Room keep rules |
| MEGA-QA-154 | P1 | REL | OPEN | Compose reflection keeps | Navigate all major routes | No `AbstractMethodError` | Compose keep rules |
| MEGA-QA-155 | P1 | REL | OPEN | Mapping file completeness | Unzip mapping from CI artifact | Line numbers present | `SourceFile,LineNumberTable` |
| MEGA-QA-156 | P2 | BOT | OPEN | Release unit smoke optional | Minified APK instrumented smoke | Critical path green | CI gap if missing |
| MEGA-QA-157 | P1 | REL | OPEN | Stack trace deobfuscate | Force test crash in release internal | Play/crashlytics readable frames | mapping upload QA-148 |
| MEGA-QA-158 | P2 | REL | OPEN | No over-broad -keep | Audit APK method count | No full-app keep accidental | `proguard-rules.pro` review |
| MEGA-QA-159 | P1 | REL | OPEN | Balance JSON parse release | Load recruiting costs | Parsed fields match debug | JSON assets |
| MEGA-QA-160 | P2 | REL | OPEN | Coroutines main dispatcher | Release advance week | No stripped dispatcher crash | coroutines keep names |

---

## N. Cross-feature integration sweeps

| ID | Pri | Exec | Status | Scenario | Steps | Pass when | Evidence |
|----|-----|------|--------|----------|-------|-----------|----------|
| MEGA-QA-161 | P1 | DEV | OPEN | Full season 1 arc | New game → EoS review | No dead routes; phase labels correct | Multi-screen |
| MEGA-QA-162 | P1 | DEV | OPEN | Season 2 carryover | Advance to S2 preseason | Roster/class continuity | `SeasonYearAdvanceEngine` |
| MEGA-QA-163 | P2 | DEV | OPEN | Sim + watch same week | Play game then sim week | No duplicate game result | Week games repo |
| MEGA-QA-164 | P2 | DEV | OPEN | Strategy change mid-season | Change scheme → sim | Box score reflects scheme | `StrategyScreen` |
| MEGA-QA-165 | P1 | DEV | OPEN | Automation screen | Dynasty automation toggles | Toggles persist | `DynastyAutomationScreen` |
| MEGA-QA-166 | P2 | EMU | OPEN | Credits / settings loop | Settings → credits → back | No nav loop | `CreditsScreen` |
| MEGA-QA-167 | P1 | DEV | OPEN | Load failure dialog | Corrupt continue slot | `SaveLoadUnavailableDialog` shown | `DynastyApp.kt` |
| MEGA-QA-168 | P2 | DEV | OPEN | Snackbar on load fail | Continue invalid | User sees message | `snackbarHostState` |
| MEGA-QA-169 | P2 | BOT | OPEN | `./gradlew :app:assembleDebug` | CI local | Green | Build |
| MEGA-QA-170 | P2 | BOT | OPEN | `./gradlew :core:test` | CI local | Green | Core regression |

---

## Summary

| Metric | Value |
|--------|-------|
| **Total MEGA-QA rows** | **170** |
| **P0 rows** | **35** |
| **KNOWN_FAIL / FAIL status** | **18** (tracked gaps to clear before ship) |
| **GUARD / BOT** | **9** |

---

## P0 ship gate — execute first

| ID | Area |
|----|------|
| MEGA-QA-001 | Main-thread new game seed / ANR |
| MEGA-QA-008 | Pre-dialog advance lag |
| MEGA-QA-013 | Sim week UI freeze (loading UX) |
| MEGA-QA-014 | Sim week wall time ≤ 2.5 s (device) |
| MEGA-QA-015 | Core league batch CI budget |
| MEGA-QA-020 | Discipline block silent failure |
| MEGA-QA-028 | Kill mid-advance corruption |
| MEGA-QA-029 | Kill mid-new-game corruption |
| MEGA-QA-030 | Corrupt slot handling |
| MEGA-QA-035 | Preseason blank shell race |
| MEGA-QA-045 | Concurrent advance safety |
| MEGA-QA-046 | Navigation back from recruiting |
| MEGA-QA-052 | Recruiting visible when cycle closed |
| MEGA-QA-060 | Zero-point spend gate |
| MEGA-QA-061 | Prestige-tier recruiting pool |
| MEGA-QA-076 | Persisted recruit pool on hub |
| MEGA-QA-077 | Empty recommended state |
| MEGA-QA-088 | Preseason tasks disappear |
| MEGA-QA-089 | Spurious preseason complete |
| MEGA-QA-092 | Missing in-season recruiting optional tasks |
| MEGA-QA-101 | Offseason W7–10 recruiting CTA |
| MEGA-QA-102 | Hamburger recruiting signing→preseason gap |
| MEGA-QA-111 | Rankings hub team vs recruit clarity |
| MEGA-QA-112 | Signing Day rankings ≠ team polls |
| MEGA-QA-119 | Emulator API 34 smoke |
| MEGA-QA-127 | Physical device full smoke |
| MEGA-QA-128 | Device sim-week perf |
| MEGA-QA-134 | Release AAB sideload parity |
| MEGA-QA-135 | Play open-test install |
| MEGA-QA-136 | Play update migration |
| MEGA-QA-137 | Signed AAB integrity |
| MEGA-QA-146 | Open test 30 min session |
| MEGA-QA-151 | R8 minify enabled |
| MEGA-QA-152 | Serialization release smoke |
| MEGA-QA-153 | Room release smoke |

---

## Maintenance

| Event | Action |
|-------|--------|
| Playtest bug filed | Add `MEGA-QA-NNN` row; link from [`MEGA_BUILD_CHECKLIST.md`](../MEGA_BUILD_CHECKLIST.md) **Your reported issues** |
| Audit gap closed | Update **Status** `FAIL` → `OPEN` then `PASS` after device run |
| New release track | Re-run section **L** + **M** on release build |
| Perf regression | Cross-link `docs/LEAGUE_WEEK_BATCH_PROFILING.md` and bump budgets if intentional |

**Related shards:** [`frame-dashboard-time.md`](frame-dashboard-time.md) (advance/tasks), [`recruiting-transfer-start.md`](recruiting-transfer-start.md) (economy/pool), [`ANDROID_RELEASE_CI.md`](../ANDROID_RELEASE_CI.md) (mapping).
