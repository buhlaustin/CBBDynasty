# MEGA audit — game frame, dashboard tasks, time system (offseason / preseason UI)

**Scope:** [`planning/game-frame.md`](../../planning/game-frame.md), [`planning/dashboard-tasks.md`](../../planning/dashboard-tasks.md), [`planning/time-system.md`](../../planning/time-system.md), [`planning/offseason.md`](../../planning/offseason.md), [`planning/preseason.md`](../../planning/preseason.md) vs Android Compose UI under `android/app/src/main/kotlin/dev/novux/dynasty/cbb/ui/`.  
**Audited:** 2026-11-11 · **Android shell:** `DynastyShellChrome`, `DynastyShellScreen`, `OffseasonScreen`, `PreseasonScreen`, hub routes.

**Legend**

| Tag | Meaning |
|-----|---------|
| **SHIP** | Matches spec in UI (or core wired from same screen) |
| **PARTIAL** | Present but incomplete vs design lock |
| **GAP** | Missing or wrong behavior |
| **N/A** | Not applicable to current phase routing |

---

## Entry & persistent chrome

| ID | Status | Spec / topic | Finding | Code / notes |
|----|--------|--------------|---------|--------------|
| MEGA-FRAME-001 | SHIP | Single dynasty shell all phases | Offseason, preseason, in-season, season review reuse `DynastyShellChrome` + four bottom tabs | `OffseasonScreen`, `PreseasonScreen`, `DynastyShellScreen`, `SeasonReviewScreen` |
| MEGA-FRAME-002 | SHIP | Bottom nav order Roster → Calendar → Dashboard → News | Tab order matches game-frame | `DynastyShellChrome.kt` |
| MEGA-FRAME-003 | SHIP | Dashboard default tab | Screens initialize `selectedTab = DynastyShellTab.Dashboard` | Shell screen `remember` state |
| MEGA-FRAME-004 | SHIP | Header: hamburger + school + phase/week | `ModalNavigationDrawer` hub sheet; school + phase/week in `TopAppBar` | `DynastyShellChrome.kt` |
| MEGA-FRAME-005 | SHIP | No separate offseason/preseason app routes in career loop | `AppRoute.DynastyCareer` + `DynastyCareerHost` swaps body by phase; legacy routes alias same host | `DynastyCareerHost.kt`, `DynastyApp.kt`, `DynastyLaunch.kt` |
| MEGA-FRAME-006 | SHIP | Coaching Carousel not “Week 0” | Carousel is `CoachingCarouselScreen` with title only — no week index in UI | `CoachingCarouselScreen.kt`, `carousel_title` |
| MEGA-FRAME-007 | SHIP | Carousel uses non-shell chrome | Carousel in `DynastyShellChrome` with four tabs + hamburger | `CoachingCarouselScreen.kt`, `CoachingCarouselShellPathTest` |

---

## Time — week advance only

| ID | Status | Spec / topic | Finding | Code / notes |
|----|--------|--------------|---------|--------------|
| MEGA-FRAME-008 | SHIP | No Advance day control | No UI string or button for day advance | repo grep; `game-frame.md#time-week-advance-only` |
| MEGA-FRAME-009 | SHIP | Week labels — exhibition 1–3 | `CalendarWeekLabel` → `Exhibition Week N` | `CalendarWeekLabel.kt` |
| MEGA-FRAME-010 | SHIP | Week labels — RS 1–32 | `Week N` for `RegularSeason` | `CalendarWeekLabel.kt` |
| MEGA-FRAME-011 | SHIP | Conf tournament week indices | `Conference Tournament Week N` | `CalendarWeekLabel.kt` |
| MEGA-FRAME-012 | SHIP | Postseason weeks 1–7 | `Postseason Week N` | `CalendarWeekLabel.kt`, `time-system.md#national-postseason-weeks` |
| MEGA-FRAME-013 | SHIP | End of Season label | `End of Season — {year}` | `CalendarWeekLabel.kt` |
| MEGA-FRAME-014 | SHIP | Offseason week header 1–10 | `Offseason Week N` | `CalendarWeekLabel.kt` |
| MEGA-FRAME-015 | SHIP | Offseason theme subtitles | `OffseasonWeekHeading.themeLine` in `DynastyShellChrome` header subtitle | `DynastyShellChrome.kt`, `OffseasonScreen.kt` |
| MEGA-FRAME-016 | SHIP | NCBA round names on calendar | Postseason week headers use `PostseasonCalendar.displayWeekHeader` (Opening Round / Super 16, …) | `CalendarWeekLabel.kt`, `PostseasonCalendarRoundLabelPathTest` |
| MEGA-FRAME-017 | SHIP | Calendar lists games by date | `UserCalendarGameEntry.gameDateLabel` + dated game line in calendar tab | `CalendarWeekDisplayDate.kt`, `CalendarGameDateLinePathTest` |
| MEGA-FRAME-018 | SHIP | Bye week chip | `UserCalendarRowKind.Bye` shows bye chip | `DynastyShellCalendarTab.kt` |
| MEGA-FRAME-019 | SHIP | Empty exhibition week advance UX | `shell_calendar_exhibition_empty` on exhibition `OffWeek` rows | `DynastyShellCalendarTab.kt` |

---

## Dashboard task gate & advance confirmation

| ID | Status | Spec / topic | Finding | Code / notes |
|----|--------|--------------|---------|--------------|
| MEGA-FRAME-020 | SHIP | Required tasks block advance | `DashboardAdvanceGate.blocksWeekAdvance` disables button | `DynastyShellScreen`, `OffseasonScreen` |
| MEGA-FRAME-021 | SHIP | Optional tasks do not block | In-season optional panel + advance still enabled when required clear | `DashboardOptionalTasksPanel`, gate tests |
| MEGA-FRAME-022 | SHIP | Sim-week confirmation dialog | `SimWeekAdvanceDialog` for fast-sim / recruiting / simple advance lines | `SimWeekAdvanceDialog.kt` |
| MEGA-FRAME-023 | SHIP | Confirmation copy intent | Fast-sim line names opponent from `ScheduledGameStub` | `SimWeekAdvanceResolver.kt` |
| MEGA-FRAME-024 | SHIP | Task cards Required vs Optional badge | `DashboardTaskKindChip` on required + optional task cards | `DashboardTaskKindChip.kt`, `DashboardTaskBadgePathTest` |
| MEGA-FRAME-025 | SHIP | Preseason 8 required tasks | `PreseasonTaskId.entries` + `PreseasonStartGate` | `PreseasonScreen`, `dashboard-tasks.md#preseason` |
| MEGA-FRAME-026 | SHIP | Preseason task deep links | `PreseasonTaskDeepLinks` + jump chips scroll/open hub per task | `PreseasonRequiredTaskChips.kt`, `PreseasonDeepLinkNavigation.kt` |
| MEGA-FRAME-027 | SHIP | Preseason Start season gate | Button gated on all tasks complete (not “Advance week”) | `PreseasonScreen`, `preseason_start_season` |
| MEGA-FRAME-028 | SHIP | Offseason required by week | `OffseasonRequiredDashboardTasks` + required panels weeks 1, 6, 8, 10; advance gate unified | `OffseasonScreen.kt`, `OffseasonRequiredDashboardTasks.kt` |
| MEGA-FRAME-029 | SHIP | Offseason Week 1 dashboard tasks | `OffseasonWeekOneRequiredDashboardTasks` + required panel → `PlayersLeavingPanel` deep links | `OffseasonScreen.kt` |
| MEGA-FRAME-030 | SHIP | Coaching Carousel dashboard tasks | `CarouselDashboardTasks` + `DashboardRequiredTasksPanel` on carousel dashboard tab | `CarouselDashboardTasks.kt`, `CoachingCarouselScreen.kt` |
| MEGA-FRAME-031 | SHIP | In-season play/sim optional task | `DashboardTaskGate.inSeason` optional game tasks | `DynastyShellScreen` |
| MEGA-FRAME-032 | SHIP | Discipline required task | Required card → `DisciplineDecisionBottomSheet`; dedicated pending load + refresh on resolve | `DynastyShellScreen.kt`, `DisciplineDecisionBottomSheet.kt` |
| MEGA-FRAME-033 | SHIP | End of Season required acknowledge | `SeasonReviewRequiredDashboardTasks` + recap acknowledge gate on Enter offseason | `SeasonReviewScreen.kt` |

---

## Dashboard (home) content

| ID | Status | Spec / topic | Finding | Code / notes |
|----|--------|--------------|---------|--------------|
| MEGA-FRAME-034 | SHIP | Team overview card | `DashboardTeamOverviewCard` — W–L, SOS, conf standing, T25 chips | `DynastyShellScreen.kt` |
| MEGA-FRAME-035 | SHIP | NCBA NET tap → Rankings | NET row + poll line tap → `onOpenRankings` | `DashboardTeamOverviewCard.kt` |
| MEGA-FRAME-036 | SHIP | Postseason seed row on dashboard | `DashboardPostseasonSeedResolver` when Selection Sunday set | `DynastyShellScreen.kt` |
| MEGA-FRAME-037 | SHIP | Notifications feed | `DashboardNotificationFeedPanel` on in-season, offseason, preseason when `showNotificationFeed` | `DashboardNotificationSnapshotLoader`, phase shells |
| MEGA-FRAME-038 | SHIP | Last game summary | `DashboardLastGameCard` — W/L line, box score sheet, stats links, postgame report | `DashboardLastUserGameResolver`, `DynastyShellScreen.kt` |
| MEGA-FRAME-039 | SHIP | Next game card | Date + location line via `FirstPlayableGame.gameDateLabel` | `DynastyShellScreen.kt` `NextGameCard` |
| MEGA-FRAME-040 | SHIP | Game preview from next game | Opens `GamePreviewScreen` with win %, score, pregame article | `GamePreviewScreen.kt` |
| MEGA-FRAME-041 | SHIP | Game preview sections | Team scouting table + dynasty H2H from standings | `GamePreviewScoutingBuilder.kt`, `HeadToHeadHistoryResolver.kt`, `GamePreviewScreen.kt` |
| MEGA-FRAME-042 | SHIP | Primary CTA lineup/gameplan from preview | Filled Strategy CTA above H2H; Play game secondary | `GamePreviewScreen.kt` |
| MEGA-FRAME-043 | SHIP | Dashboard roster teaser vs spec | Removed 6-player preview strip; roster tab hint only | `DynastyShellScreen.kt` |

---

## Bottom tabs — Roster, Calendar, News

| ID | Status | Spec / topic | Finding | Code / notes |
|----|--------|--------------|---------|--------------|
| MEGA-FRAME-044 | SHIP | Roster starters / reserves / bench | `ShellRosterSectionPartition` + section headers on shell roster tab | `DynastyShellTabContent.kt`, `ShellRosterSectionPartition.kt` |
| MEGA-FRAME-045 | SHIP | Roster tap → player card | Preview sheet + full card from shell roster tab (in-season, OS, preseason, carousel) | `OffseasonScreen.kt`, `PreseasonScreen.kt`, `CoachingCarouselScreen.kt` |
| MEGA-FRAME-046 | SHIP | Calendar your games only | User `PreseasonSchedulePlan` via `UserCalendarRepository`; header copy; plan-order rows | `UserCalendarSchedulePolicy.kt`, `DynastyShellCalendarTab.kt` |
| MEGA-FRAME-047 | SHIP | Calendar conf vs non-conf distinction | Kind `AssistChip` per row; section headers when week has multiple games | `DynastyShellCalendarTab.kt` |
| MEGA-FRAME-048 | SHIP | Calendar tap → preview / box score | Tappable lines; upcoming → preview; played → box sheet + scores | `DynastyShellCalendarTab.kt`, `UserCalendarGameEnricher.kt`, `GamePreviewFromCalendar.kt` |
| MEGA-FRAME-049 | SHIP | News tab shallow headlines | `rememberShellNewsHeadlines` + `DynastyShellNewsTab` in OS/preseason/carousel | `ShellShallowNewsFeed.kt`, phase shell screens |
| MEGA-FRAME-050 | SHIP | News top N truncate policy | `ShallowNewsFeedSnapshot` + cap copy + “more in hub” / open hub CTA | `ShallowNewsFeed.kt`, `DynastyShellNewsTab` |
| MEGA-FRAME-051 | SHIP | News headline tap → detail / hub | Clickable cards → mini-article sheet; hub CTA on recruiting/awards/bracketology | `NewsHeadlineArticleBuilder`, `DynastyShellNewsTab` |

---

## Hamburger hubs & navigation

| ID | Status | Spec / topic | Finding | Code / notes |
|----|--------|--------------|---------|--------------|
| MEGA-FRAME-052 | SHIP | Core hub menu items | School, Recruiting, Strategy, Statistics, Rankings, News, History, Awards | `buildDynastyHubMenu` |
| MEGA-FRAME-053 | SHIP | March Mayhem visibility | Hub item when `Postseason` and `selectionSunday` published on standings | `DynastyHubMenuPolicy`, `DynastyShellScreen` |
| MEGA-FRAME-054 | SHIP | Conf tournament menu in phase | Extra item in conf tournament phase | `DynastyHubMenuPolicy` |
| MEGA-FRAME-055 | SHIP | Rankings hub 365 + polls | Full NET/Media/Coaches/Conf tabs when published; placeholder + preseason preview slice before | `RankingsHubPublishPolicy`, `RankingsHubScreen` |
| MEGA-FRAME-056 | SHIP | Dashboard default after load | `DynastyLaunch` navigates `DynastyCareer` for offseason/preseason/in-season | `DynastyLaunch.kt`, `DynastyCareerPhaseResolver.kt` |

---

## Offseason UI (weeks & screens)

| ID | Status | Spec / topic | Finding | Code / notes |
|----|--------|--------------|---------|--------------|
| MEGA-FRAME-057 | SHIP | Season 1 skip Week 10 | Policy in core + launch | `offseason.md#season-1-vs-later-seasons` |
| MEGA-FRAME-058 | SHIP | Week 1 Players Leaving panel | `PlayersLeavingPanel` on week 1 dashboard | `OffseasonScreen` |
| MEGA-FRAME-059 | SHIP | Week 1 Roster / Transfer tabs | `PortalWeekOnePartition` + tab counts, mutual-exclusion copy, empty states in `PlayersLeavingPanel` | `WeekOneLeavingTabPolicy`, `PlayersLeavingScreen.kt` |
| MEGA-FRAME-060 | SHIP | Week 2 draft results panel | `DraftResultsWeekTwoPanel` | `OffseasonScreen` week 2 |
| MEGA-FRAME-061 | SHIP | Weeks 2–5 portal optional tasks | Portal optional panel + pipeline entrant nudge, shared build helper, portal deep links | `OffseasonPortalOptionalNudgeResolver`, `OffseasonScreen` |
| MEGA-FRAME-062 | SHIP | Week 6 Signing Day required tasks | `SigningDayRequiredDashboardTasks` + signing screen route | `SigningDayScreen` |
| MEGA-FRAME-063 | SHIP | Week 7 summer workouts optional report | Optional review card + bottom sheet full report on Week 7 dashboard | `OffseasonWeekSevenOptionalDashboardTasks`, `SummerWorkoutReportPanel` |
| MEGA-FRAME-064 | SHIP | Week 8 roster ≤15 gate | `RosterCutsWeekEightPanel` + advance gate | `OffseasonScreen` |
| MEGA-FRAME-065 | SHIP | Week 9 fundraising / upgrades | Optional cards per event / naming / upgrades + fundraising bottom sheet | `OffseasonWeekNineOptionalDashboardTasks`, `FundraisingWeekNinePanel` |
| MEGA-FRAME-066 | SHIP | Week 10 conference offers Season 2+ | `ConferenceOffersWeekTenPanel` + advance block | `OffseasonScreen` |
| MEGA-FRAME-067 | SHIP | Advance to preseason CTA | `OffseasonWeekAdvancePolicy.showsAdvanceToPreseasonCta` — week 9 (S1) or week 10 (S2+) | `OffseasonScreen`, `OffseasonWeekAdvancePolicy` |
| MEGA-FRAME-068 | SHIP | Offseason sim-week on optional portal spend | `OffseasonAdvanceOptionalTaskAggregator` + portal nudge/auto-spend confirm lines in `SimWeekAdvanceResolver` | `OffseasonPortalSimWeekPolicy`, `OffseasonScreen` |

---

## Preseason UI (single week)

| ID | Status | Spec / topic | Finding | Code / notes |
|----|--------|--------------|---------|--------------|
| MEGA-FRAME-069 | SHIP | Single week Preseason label | Header `Preseason` | `CalendarWeekLabel` |
| MEGA-FRAME-070 | SHIP | Tasks 7–8 locked until invites | `preseason_task_locked_invites` + progress gate | `PreseasonScreen` |
| MEGA-FRAME-071 | SHIP | Task 1 Incoming class seed UX | Stepped HS/JUCO/rank/headline/save progress + determinate bar on `IncomingClassSeedPanel` | `IncomingClassSeedRunProgress`, `IncomingClassSeedRepository` |
| MEGA-FRAME-072 | SHIP | Task 4 NET + polls review sheet | `PreseasonRankingsReviewBottomSheet` — NET preview, program poll chips, Continue publishes + dismisses; hub route also pops on Continue | `PreseasonRankingsReviewUxPolicy` |
| MEGA-FRAME-073 | SHIP | Task 5 preseason report article | `PreseasonReportPanel` + `ReportArticleView` pattern | `preseason.md#view-preseason-report` |
| MEGA-FRAME-074 | SHIP | Task 6 tournament invites prestige tiers | Live prestige → `TournamentInvitesPrestigePolicy` tiers; elevated multi-invite cards + decline-all | `TournamentInvitesPanel`, `PreseasonScreen` |
| MEGA-FRAME-075 | SHIP | Task 7 exhibitions 0–3 / skip | Opponent search sheet, H/A, exhibition week slots; persisted drafts feed solver | `ExhibitionSchedulePanel`, `ExhibitionScheduleUxPolicy` |
| MEGA-FRAME-076 | SHIP | Task 8 RS schedule builder | Slot budget breakdown, validation errors, week-by-week review sheet before lock | `RegularSeasonScheduleBuilderUxPolicy`, `RegularSeasonSchedulePanel` |
| MEGA-FRAME-077 | SHIP | Preseason shell News tab live feed | Top 60 spotlight after seed; refresh on task complete + preseason empty hint | `PreseasonNewsFeedPolicy`, `NewsFeedRepository` |
| MEGA-FRAME-078 | SHIP | Start season → first playable focus | `PreseasonSeasonStartPolicy` + exhibition vs RS | `SaveSlotRepository.completePreseason` |

---

## Cross-cutting time & advance behavior

| ID | Status | Spec / topic | Finding | Code / notes |
|----|--------|--------------|---------|--------------|
| MEGA-FRAME-079 | SHIP | Bye week advance allowed | `ByeWeekPolicy.canAdvanceWeek` always true in-season | core tests |
| MEGA-FRAME-080 | SHIP | Injury weeks tick on bye advance | `InjuryByeAdvanceConfirmPolicy` — bye copy + simple advance tail; `userOnByeWeek` in repository | `InjuryByeAdvanceConfirmPolicy`, `SimWeekAdvanceDialog` |
| MEGA-FRAME-081 | SHIP | Early-event bye weeks on calendar | `EarlySeasonUserCalendarByes` + `UserCalendarRowKind.EventEliminationBye` in shell calendar tab | `UserCalendarRepository`, `DynastyShellCalendarTab.kt` |
| MEGA-FRAME-082 | SHIP | Conf tourney shorter-bracket bye | `ConfTournamentStructuralBye` chip + conference detail on calendar tab | `UserCalendarTabBuilder`, `DynastyShellCalendarTab.kt` |
| MEGA-FRAME-083 | SHIP | Week advance league batch sim | RS advance confirm line `LeagueWeekBatch` from `simulatesLeagueWeekBatch` in repository input | `SimWeekAdvanceResolver`, `SaveSlotRepository`, `SimWeekAdvanceDialog` |
| MEGA-FRAME-084 | SHIP | Season review week no bottom-tab games | Calendar tab banner `season_review_calendar_banner` — no games this week | `SeasonReviewScreen` |

---

## User-reported regressions (dashboard / advance)

| ID | Status | Spec / topic | Finding | Code / notes |
|----|--------|--------------|---------|--------------|
| MEGA-FRAME-085 | PASS | **Bug: tasks disappear (preseason)** | Preseason dashboard uses single **LazyColumn** checklist (done + remaining); advance gate still uses incomplete-only required tasks | `PreseasonScreen.kt` |
| MEGA-FRAME-086 | PASS | Preseason progress loading UX | Dashboard shows skeleton while `PreseasonTaskProgress` loads; shell chrome remains visible | `PreseasonScreen.kt` |
| MEGA-FRAME-087 | PASS | **Bug: tasks disappear (dual list)** | Removed duplicate `DashboardRequiredTasksPanel` on preseason dashboard — one checklist surface | `PreseasonScreen.kt` |
| MEGA-FRAME-088 | PASS | **Bug: tasks disappear (spurious complete)** | Workouts/coach/rankings rows use feature panels; generic Complete removed; rankings publish via hub Continue | `PreseasonScreen.kt`, `PreseasonFeatureTaskPanels.kt`, `RankingsHubRepository.publishPreseasonRankings` |
| MEGA-FRAME-089 | PASS | **Bug: recruiting UI after Signing Day** | `effectiveCycleClosed` treats OS W7–10 as closed; dashboard shows closed copy not Open hub CTA; ledger persists closed on advance | `RecruitingCyclePolicy.kt`, `OffseasonScreen.kt` |
| MEGA-FRAME-090 | PASS | **Bug: recruiting between signing & preseason** | Hamburger **Recruiting** gated on `cycleClosed` — closed label + toast or `RecruitingCycleClosedBody` on route | `buildDynastyHubMenu`, `RecruitingCyclePolicy.hubNavigable` |
| MEGA-FRAME-091 | PASS | Sim week advance loading UX | Advance button and confirm dialog show spinner + disable while suspend advance runs | `DynastyShellScreen.kt`, `OffseasonScreen.kt` |
| MEGA-FRAME-092 | PASS | Sim week confirm loading UX | Dialog confirm shows spinner; dismiss blocked while advancing | `SimWeekAdvanceDialog.kt` |
| MEGA-FRAME-093 | PASS | **Bug: sim week lag (pre-dialog)** | Confirmation path sets `preDialogOnly` — `loadPool` (no seed) and skips `buildFastSimBundle` until confirm ([MEGA-QA-008]) | `SaveSlotRepository`, `SimWeekAdvancePreDialogPath` |
| MEGA-FRAME-094 | SHIP | **Bug: tasks disappear (portal weeks)** | After portal window closes, dashboard shows closed heading/body instead of removing optional block | `OffseasonScreen.kt`, `PortalCyclePolicy.portalWindowClosed` |

---

## Dashboard task catalog & persistence

| ID | Status | Spec / topic | Finding | Code / notes |
|----|--------|--------------|---------|--------------|
| MEGA-FRAME-095 | PASS | In-season recruiting optional tasks | `InSeasonRecruitingOptionalDashboardTasks` + shell `InSeasonRecruitingDashboardContext` emit spend + watchlist nudges when cycle open | `DashboardTaskGate.kt`, `DynastyShellScreen.kt` |
| MEGA-FRAME-096 | SHIP | Shell recruiting ledger | Single effect loads ledger + `InSeasonRecruitingDashboardContext` for optional tasks and sim-week confirm | `DynastyShellScreen.kt`, `RecruitingDashboardFirstPaint` |
| MEGA-FRAME-097 | SHIP | Required / Optional chips | `DashboardTaskKindChip` on both panels (same as MEGA-FRAME-024) | `DashboardTaskKindChip.kt` |
| MEGA-FRAME-098 | SHIP | Preseason required deep link | Jump chips + `navigatePreseasonDashboardTask` scroll/hub — no duplicate full task card panel | `PreseasonScreen.kt` |
| MEGA-FRAME-099 | SHIP | `PreseasonChecklist` deep link | Each task has unique `DashboardDeepLink` + `preseasonTaskId` on `DashboardTask` | `PreseasonTaskDeepLinks.kt` |
| MEGA-FRAME-100 | SHIP | Optional dismiss scope | Scoped dismiss keys `${phase}:${week}:${taskId}` via `DashboardTaskDismissal` on shell + offseason panels | `DashboardOptionalTasksPanel`, `DashboardTaskDismissal.kt` |
| MEGA-FRAME-101 | SHIP | Offseason dismiss persistence | Scoped optional dismiss keys persisted via `DashboardOptionalDismissRepository` (DataStore) | `OffseasonScreen.kt` |
| MEGA-FRAME-102 | SHIP | Week 1 required dashboard pattern | Required cards for pro day + transfer retention mirror panel actions | `OffseasonWeekOneRequiredDashboardTasks.kt` |
| MEGA-FRAME-103 | SHIP | Required blocks advance core | `DashboardAdvanceGate.blocksWeekAdvance` used on shell, offseason, preseason | `DashboardAdvanceGate.kt` |
| MEGA-FRAME-104 | SHIP | Discipline task visibility | Pending discipline loaded in shell dashboard hydration effect + dedicated refresh effect | `DynastyShellScreen.kt`, `DisciplineRequiredDashboardTasks` |
| MEGA-FRAME-105 | PASS | Signing Day panel null race | Week 6 shows `SigningDayRequiredTasksSkeleton` until `signingProgress` loads ([MEGA-QA-094]) | `OffseasonScreen.kt` |
| MEGA-FRAME-106 | SHIP | In-season optional game task | One optional play task per scheduled user game; `dismissScopeKey` = opponent team id | `InSeasonOptionalDashboardTasks` |

---

## Sim-week advance pipeline & confirmation

| ID | Status | Spec / topic | Finding | Code / notes |
|----|--------|--------------|---------|--------------|
| MEGA-FRAME-107 | SHIP | Unplayed game copy | Opponent in `advance_confirm_line_fast_sim` via `stringFormatArgs` | `SimWeekAdvanceDialog.kt` |
| MEGA-FRAME-108 | SHIP | Offseason lightweight confirm | `OffseasonAdvanceConfirmPolicy` forces `SimpleAdvance` dialog on milestone offseason weeks | `SimWeekAdvanceResolver`, `SaveSlotRepository` |
| MEGA-FRAME-109 | SHIP | Portal line on advance | `PortalRecruitingWindowClosed` confirm line when portal window closed (week 6+) | `SimWeekAdvanceResolver.kt`, `SimWeekAdvanceDialog.kt` |
| MEGA-FRAME-110 | SHIP | Manual recruiting spend param | Hub spends recorded in `RecruitingManualWeekSpendRepository`; confirm/advance pass total to sim-week input | `RecruitingEconomyRepository`, `SaveSlotRepository` |
| MEGA-FRAME-111 | SHIP | Advance failure UX | `WeekAdvanceBlocked` + `Unreadable` snackbars; null confirm shows `advance_confirm_unavailable` | `DynastyWeekAdvanceFeedback.kt` |
| MEGA-FRAME-112 | SHIP | Injury tick on bye | `InjuryWeeksTickByeWeek` explains no-game week; wired via `ByeWeekPolicy` + `userOnByeWeek` | `InjuryByeAdvanceConfirmPolicy`, `ByeWeekPolicy` |
| MEGA-FRAME-113 | SHIP | Double-tap advance | Advance `Button` ignores clicks while in-flight (`weekAdvanceInProgress` / `dialogAdvanceInProgress`) before launching coroutine | `DynastyShellScreen.kt`, `OffseasonScreen.kt` |
| MEGA-FRAME-114 | SHIP | Advance work ordering | Post-confirm pipeline documented in `DynastyWeekAdvancePipelineStages`; pre-dialog lag split to [MEGA-QA-008] / [MEGA-FRAME-093] | `SaveSlotRepository.advanceDynastyWeek`, `DynastyWeekAdvancePipelineStages` |
| MEGA-FRAME-115 | SHIP | Simple advance line | `InSeasonAdvanceConfirmPolicy` forces destination `SimpleAdvance` dialog in exhibition/RS/postseason | `SimWeekAdvanceResolver`, `SaveSlotRepository` |
| MEGA-FRAME-116 | SHIP | Sim-week vs Start season | Preseason **Start season** CTA shows in-progress indicator and blocks double-tap while `startSeason` runs | `PreseasonScreen.kt`, `PreseasonSeasonStartRepository` |

---

## Offseason dashboard by week (shell body)

| ID | Status | Spec / topic | Finding | Code / notes |
|----|--------|--------------|---------|--------------|
| MEGA-FRAME-117 | SHIP | Weeks 3–5 dashboard body | Week-specific portal recap copy (`offseason_dashboard_week3_body` … `_week5_body`) on dashboard | `OffseasonScreen.kt` |
| MEGA-FRAME-118 | SHIP | Theme line placement | `OffseasonWeekHeading.themeLine` via `DynastyShellChrome.headerSubtitle` only (no duplicate dashboard title) | `OffseasonScreen.kt`, `DynastyShellChrome.kt` |
| MEGA-FRAME-119 | SHIP | Week 7 workout optional task | Optional review dashboard card opens full report sheet (see MEGA-FRAME-063) | `OffseasonWeekSevenOptionalDashboardTasks` |
| MEGA-FRAME-120 | SHIP | Week 8 scholarship gate | Week 8 advance + open slots use `RosterLockPolicy.scholarshipCount` (walk-ons excluded) | `OffseasonScreen`, `RosterLockPolicy` |
| MEGA-FRAME-121 | SHIP | Week 9 fundraising tasks | Optional fundraising dashboard cards per `dashboard-tasks.md` week 9 table | `OffseasonWeekNineOptionalDashboardTasks` |
| MEGA-FRAME-122 | SHIP | Week 10 → preseason CTA | Final-week CTA + helper copy tied to season 1 week 9 skip vs week 10 conf | `OffseasonWeekAdvancePolicy`, `OffseasonScreen` |
| MEGA-FRAME-123 | SHIP | Offseason calendar tab | Empty offseason calendar shows `shell_calendar_offseason_no_games` instead of preseason placeholder | `DynastyShellCalendarTab.kt` |
| MEGA-FRAME-124 | SHIP | Week 2 draft results + tasks | Optional portal / open-scholarship tasks use scholarship-based `openScholarshipSlots` from roster `LaunchedEffect` | `OffseasonScreen`, `OffseasonOptionalDashboardTasks` |
| MEGA-FRAME-125 | SHIP | Signing Day screen vs dashboard | Week 6 shell shows one **Signing Day launcher** task into full screen — steps live on `SigningDayScreen` only | `SigningDayShellDashboardTasks`, `OffseasonScreen` |
| MEGA-FRAME-126 | SHIP | Recruiting hub offseason W1 | Week 1 retention copy when hub closed; chasing opens Week 2 per spec | `OffseasonScreen`, `SigningDayPreseasonRecruitingTransition` |
| MEGA-FRAME-127 | SHIP | Close cycle UX after hard sign | Hub button hidden when `!hubNavigable`; closed cycle shows **Administration only** + body copy Weeks 7+ | `OffseasonScreen`, `RecruitingCyclePolicy` |

---

## Preseason gate, checklist & transition

| ID | Status | Spec / topic | Finding | Code / notes |
|----|--------|--------------|---------|--------------|
| MEGA-FRAME-128 | SHIP | Top 60 → News tab | `isSeeded` spotlight publish on load + after seed task; live `DynastyShellNewsTab` feed | `PreseasonScreen`, `NewsFeedRepository` |
| MEGA-FRAME-129 | SHIP | Tasks 7–8 invite gate | Locked copy + `schedulingUnlocked()` matches `preseason.md` dependency on task 6 | `PreseasonScreen.kt` |
| MEGA-FRAME-130 | SHIP | Start season navigation | `PreseasonScreen.onSeasonStarted` no-op; `ActiveDynastySession` listener recomposition inside `DynastyCareerHost` | `DynastyCareerHost.kt` |
| MEGA-FRAME-131 | SHIP | Task order UX | `preseason_task_order_hint` + `PreseasonTaskOrderPolicy` documents flexible tasks 1–5 | `PreseasonScreen.kt` |
| MEGA-FRAME-132 | SHIP | Coach upgrades wiring | Task 3 **Mark done** gated until Strategy opened once | `PreseasonCoachUpgradesPanel` |
| MEGA-FRAME-133 | SHIP | Rankings review task | Rankings hub route view-only; task completes via review sheet **Continue** (`publishPreseasonRankings`) | `PreseasonRankingsReviewBottomSheet` |
| MEGA-FRAME-134 | SHIP | Exhibition / RS builders | `ExhibitionDraftIssue` + builder status banner and task rows surface validation | `ExhibitionScheduleUxPolicy`, `PreseasonScreen` |
| MEGA-FRAME-135 | SHIP | Preseason header hint | `preseason_shell_header_hint` on `DynastyShellChrome.headerSubtitle` | `PreseasonScreen.kt` |
| MEGA-FRAME-136 | SHIP | Redshirt in task 2 | Workouts panel calls out optional medical redshirt via roster tab | `PreseasonWorkoutsPanel` |
| MEGA-FRAME-137 | SHIP | Eight-task gate to start | `PreseasonStartGate.canStartSeason` + `DashboardAdvanceGate` on start button | `PreseasonScreen.kt` |

---

## Phase routing, hubs & shell parity

| ID | Status | Spec / topic | Finding | Code / notes |
|----|--------|--------------|---------|--------------|
| MEGA-FRAME-138 | SHIP | Multi-route career loop | Single launch target `dynasty_career`; phase → body via `DynastyCareerPhaseResolver` | `DynastyCareerUnifiedRoutePathTest` |
| MEGA-FRAME-139 | SHIP | Hub back stack / tab reset | `popBackToInShellDashboard` bumps tab-restore nonce; shell uses `rememberDynastyShellSelectedTab` | `DynastyNavigation`, `ActiveDynastySession` |
| MEGA-FRAME-140 | SHIP | Season review chrome | Four-tab `DynastyShellChrome` + `season_review_shell_header_hint` on season review week | `SeasonReviewScreen.kt` |
| MEGA-FRAME-141 | SHIP | Players Leaving routing | Week 1 retention via in-shell `PlayersLeavingPanel` on `OffseasonScreen` (no separate route) | `WeekOneLeavingInShellPathTest` |
| MEGA-FRAME-142 | SHIP | Recruiting hub during preseason | Preseason setup banner on hub explains checklist vs live chasing | `RecruitingHubScreen.kt` |
| MEGA-FRAME-143 | SHIP | Carousel → Week 1 handoff | Carousel shell → offseason shell; shared bottom nav through handoff | `CoachingCarouselScreen.kt`, `DynastyLaunch.kt` |
| MEGA-FRAME-144 | SHIP | Job center unemployed | Unemployed flow uses `DynastyCareer` + `UnemployedCoachCareerScreen` in `DynastyShellChrome` | `UnemployedCoachShellPolicy`, `DynastyCareerHost.kt` |
| MEGA-FRAME-145 | SHIP | Awards hub shell access | Shell menu → `AwardsHub` route with back + `awards_hub_shell_access_hint` | `DynastyHubMenu.kt`, `AwardsHubScreen.kt` |

---

## Time labels, calendar & bye semantics

| ID | Status | Spec / topic | Finding | Code / notes |
|----|--------|--------------|---------|--------------|
| MEGA-FRAME-146 | SHIP | Conf tournament week N in header | Shell + calendar rows use **Week N of M** from `ConferenceTournamentCalendar.leagueMaxConfTourneyWeeks` | `CalendarWeekLabel.kt`, `DynastyShellScreen.kt` |
| MEGA-FRAME-147 | SHIP | Postseason round labels | Same as MEGA-FRAME-016 — round name in week header on calendar tab | `CalendarWeekLabel.kt` |
| MEGA-FRAME-148 | SHIP | Exhibition empty week copy | Zero exhibitions selection shows `preseason_exhibitions_zero_week_copy` | `ExhibitionSchedulePanel.kt` |
| MEGA-FRAME-149 | SHIP | Event bye after elimination | Shared with 081 — Event bye chip + event detail copy on calendar | `DynastyShellCalendarTab.kt` |
| MEGA-FRAME-150 | SHIP | Preseason single-week label | Header displays **Preseason — setup week** | `CalendarWeekLabel.kt` |
| MEGA-FRAME-151 | SHIP | End of Season vs review | Dashboard headline matches shell `CalendarWeekLabel` (**End of Season — {year}**) | `SeasonReviewScreen.kt` |
| MEGA-FRAME-152 | SHIP | RS week 1–32 batching | Shell week header = checkpoint; RS batch gated by `RegularSeasonWeekBatchPolicy` on confirm only | `RegularSeasonWeekBatchPolicy`, `SaveSlotRepository` |
| MEGA-FRAME-153 | SHIP | NCBA top-52 bye week 1 | `NcbaOpeningRoundBye` row + copy when user bid skips Opening Round | `PostseasonUserCalendar.kt`, `UserCalendarRepository.kt` |

---

## Notifications, feeds & advance feedback

| ID | Status | Spec / topic | Finding | Code / notes |
|----|--------|--------------|---------|--------------|
| MEGA-FRAME-154 | SHIP | Offseason/preseason notification feed | Same feed panel + snapshot loader on `OffseasonScreen` / `PreseasonScreen` | `MEGA-FRAME-037` |
| MEGA-FRAME-155 | SHIP | Session refresh after advance | Shell screens listen to `ActiveDynastySession` and refresh local `loaded` | `PreseasonScreen`, `OffseasonScreen`, `DynastyShellScreen` |
| MEGA-FRAME-156 | PASS | Hub Recruiting when cycle closed | Menu shows **Recruiting (cycle closed)** with explanation toast when ledger `cycleClosed` | `buildDynastyHubMenu`, `notifyRecruitingCycleClosed` |
| MEGA-FRAME-157 | SHIP | No Advance day control | Reconfirmed — no day-advance UI anywhere in shell/offseason/preseason | repo grep |
| MEGA-FRAME-158 | SHIP | Checkpoint transparency | Successful advance shows `advance_week_saved` snackbar via `applyWeekAdvanceResult` | `DynastyWeekAdvanceFeedback.kt` |

---

## Summary counts

| Status | Count |
|--------|------:|
| SHIP | 36 |
| PARTIAL | 45 |
| GAP | 77 |
| **Total items** | **158** |

**New rows this pass:** **74** (`MEGA-FRAME-085` … `MEGA-FRAME-158`).

| Metric | Value |
|--------|------:|
| Prior total (2026-11-11 initial) | 84 |
| **User-reported bug rows** | 10 |
| **GAP** (all rows) | 77 (48.7%) |
| **PARTIAL** | 45 (28.5%) |
| **SHIP** | 36 (22.8%) |

**Priority clusters for frame/dashboard/time:** MEGA-FRAME-085–094 (user bugs: tasks, recruiting gap, sim-week lag), MEGA-FRAME-034–038 (dashboard home), MEGA-FRAME-095–096 (in-season recruiting tasks), MEGA-FRAME-091–094 / 113–114 (advance perf UX), MEGA-FRAME-007/005/138 (shell unity), MEGA-FRAME-089–090/127/156 (recruiting hub after signing), MEGA-FRAME-016–017/147–148 (calendar polish), MEGA-FRAME-077/128 (preseason news).

---

## Document history

| Date | Change |
|------|--------|
| 2026-11-11 | Initial UI audit vs game-frame, dashboard-tasks, time-system, offseason, preseason planning docs |
| 2026-11-11 | +74 rows (085–158): user-reported tasks/recruiting/sim-lag bugs; dashboard persistence; sim-week pipeline; offseason-by-week; preseason transition; routing; calendar/byes; notifications |
