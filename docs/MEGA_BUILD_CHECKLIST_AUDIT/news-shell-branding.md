# MEGA audit — app shell, branding, news hub, notifications, discipline, chemistry

**Scope:** [`planning/app-shell.md`](../../planning/app-shell.md), [`planning/branding.md`](../../planning/branding.md), [`planning/news-hub.md`](../../planning/news-hub.md), [`planning/notifications.md`](../../planning/notifications.md), [`planning/in-season-systems.md`](../../planning/in-season-systems.md) vs Android pre-menu + dynasty UI (`android/app/.../ui/shell`, `ui/news`, `ui/discipline`, core `news` / `notification` / `discipline` / `chemistry` / `fatigue`).

**Audited:** 2026-11-11

**Legend**

| Tag | Meaning |
|-----|---------|
| **SHIP** | Matches spec (core + UI wired) |
| **PARTIAL** | Present but incomplete vs design lock |
| **GAP** | Missing or wrong behavior |
| **N/A** | Out of scope for current routing |

---

## Pre-menu — studio, splash, branding

| ID | Status | Spec / topic | Finding | Code / notes |
|----|--------|--------------|---------|--------------|
| MEGA-SHELL-001 | SHIP | Cold start: Studio → Splash → Home | Nav order `AppRoute.Studio` → `Splash` → `Home` | `DynastyApp.kt` |
| MEGA-SHELL-002 | SHIP | Studio auto-advance 3.5s | `GameConstants.STUDIO_AUTO_ADVANCE_MS = 3_500` | `StudioScreen.kt` |
| MEGA-SHELL-003 | SHIP | Studio tap skip after 0.5s min | `STUDIO_MIN_SKIP_MS`; clickable gated on `skipEnabled` | `StudioScreen.kt` |
| MEGA-SHELL-004 | SHIP | Studio Novux lockup, not game logo | `R.drawable.novux_studio_lockup` hero | `StudioScreen.kt`, `app-shell.md` |
| MEGA-SHELL-005 | SHIP | Studio novux.dev line | `R.string.studio_website` | `StudioScreen.kt` |
| MEGA-SHELL-006 | SHIP | Studio background `#181a25` full bleed | `StudioBrandBackgroundPolicy` paints full-bleed brand hex on studio route. | `StudioScreen.kt` |
| MEGA-SHELL-007 | SHIP | Studio tap hint in `strings.xml` | `studio_tap_to_continue` replaces hard-coded Compose copy. | `StudioScreen.kt`, `strings.xml` |
| MEGA-SHELL-008 | SHIP | Settings: skip studio intro | `AppSettings.skipStudioIntro` → start at Splash | `SettingsScreen`, `DynastyApp` `startRoute` |
| MEGA-SHELL-009 | SHIP | Splash: game logo + official title | `app_logo`, `GameConstants.GAME_TITLE` | `SplashScreen.kt` |
| MEGA-SHELL-010 | SHIP | Splash pulsing Tap to Play CTA | Scale 0.94↔1.06, opacity 0.78↔1.0, 900ms | `SplashScreen.kt`, `app-shell.md#tap-to-play` |
| MEGA-SHELL-011 | SHIP | Splash loading gate before CTA | `SplashReadyGatePolicy` + bootstrap in `DynastyApp` after pack + save init. | `SplashScreen.kt`, `DynastyApp.kt` |
| MEGA-SHELL-012 | SHIP | Splash pack / asset fail + Retry | `SplashPackErrorPolicy` — error copy + **Retry** relaunches bootstrap. | `SplashScreen.kt` |
| MEGA-SHELL-013 | SHIP | Splash footer © Novux + director line | `splash_copyright`, `game_director_line` | `SplashScreen.kt` |
| MEGA-SHELL-014 | SHIP | Brand background `#181A25` in theme + launcher | `GameConstants.BRAND_BACKGROUND_HEX`, `colors.xml`, adaptive icon | `Theme.kt`, `mipmap-anydpi-v26` |
| MEGA-SHELL-015 | SHIP | Contrast tokens on brand field | `#E8EAED` on-surface, `#A8AEB8` muted | `Theme.kt` vs `branding.md#color` |
| MEGA-SHELL-016 | PARTIAL | Home menu title branding | Text title only — no app logo on home | `HomeMenuScreen.kt` |
| MEGA-SHELL-017 | SHIP | Credits © Novux, website, disclaimer | `CreditsScreen.kt` | `game-start.md` |
| MEGA-SHELL-018 | GAP | Credits → Visit Novux / replay studio | Optional replay studio screen — not in Credits UI | `app-shell.md` |

---

## News — one pool, two surfaces

| ID | Status | Spec / topic | Finding | Code / notes |
|----|--------|--------------|---------|--------------|
| MEGA-SHELL-019 | SHIP | Shallow dashboard cap (top N) | `ShallowNewsFeed` + `SHALLOW_FEED_CAP = 10` | `ShallowNewsFeed.kt`, `NewsHeadlinePublisher.kt` |
| MEGA-SHELL-020 | SHIP | Same archive powers hub Stories | `NewsFeedRepository.storiesFeed` = sorted archive | `NewsFeedRepository.kt` |
| MEGA-SHELL-021 | PARTIAL | Dashboard vs hub same priority queue | Shallow merge uses priority + `createdAtMs`; hub Stories sort emphasizes `createdAtMs` first | compare `storiesFeed` vs `ShallowNewsFeed` |
| MEGA-SHELL-022 | SHIP | In-season shallow News tab | `DynastyShellNewsTab` + `shallowFeed` on shell | `DynastyShellScreen.kt` |
| MEGA-SHELL-023 | GAP | Offseason / preseason News tab live slice | Placeholder empty feed on non–in-season shells | `DynastyShellNewsTabPlaceholder`, `OffseasonScreen`, `PreseasonScreen` |
| MEGA-SHELL-024 | GAP | News tab headline tap → detail / hub | Cards are non-clickable | `DynastyShellNewsTab.kt` |
| MEGA-SHELL-025 | GAP | Story date chip on feed lines | Spec: headline + date chip | `news-hub.md#stories-feed` — titles only |
| MEGA-SHELL-026 | SHIP | Hamburger → full News hub route | `AppRoute.NewsHub`, menu item | `DynastyHubMenu.kt`, `NewsHubScreen.kt` |
| MEGA-SHELL-027 | SHIP | Hub tabs: Stories / Portal / Recruits / Draft | Four tabs implemented | `NewsHubScreen.kt` |
| MEGA-SHELL-028 | SHIP | Portal tab hidden outside OS weeks 1–5 | `NewsHubPortalTabVisibilityPolicy` omits Portal from hub tab row. | `NewsHubScreen.kt` |
| MEGA-SHELL-029 | SHIP | Portal tab visible OS weeks 1–5 | Policy matches `PortalConstants.PORTAL_RECRUITING_WEEKS` | `NewsHubPolicyTest.kt` |
| MEGA-SHELL-030 | SHIP | Week 1 Top 40 placeholder until lock | `news_hub_portal_pre_lock` when `!top40Locked` | `NewsHubScreen.kt` |
| MEGA-SHELL-031 | SHIP | Portal Top 40 full #1–#40 human labels | `NewsHubPortalHumanRowPolicy` + pack display names on portal rows. | `news_hub_portal_row` |
| MEGA-SHELL-032 | SHIP | Portal recent commits / entrants sections | Top 40 + **Recent portal entrants** via `NewsHubPortalRecentSectionsPolicy`. | `NewsHubScreen.kt` |
| MEGA-SHELL-033 | SHIP | Portal filters row | Star filter chips + cadence hint (`NewsHubPortalFiltersPolicy`). | `NewsHubScreen.kt` |
| MEGA-SHELL-034 | SHIP | Portal row tap → profile / school hub | Row tap opens Transfer Portal recruiting (`NewsHubPortalRowNavigationPolicy`). | `DynastyApp.kt` |
| MEGA-SHELL-035 | SHIP | Add to board deep link | **Add to board** CTA → `NewsHubPortalAddToBoardPolicy.recruitingHubTab`. | `RecruitingHub` route |
| MEGA-SHELL-036 | SHIP | Recruits tab national Top 60 #1–#60 | Seed or `NationalRecruitingRankings.top60` | `NewsHubScreen.kt` |
| MEGA-SHELL-037 | PARTIAL | Draft tab dynamic pro pool | Offseason week 2 published lines only — not in-season dynamic board | `offseasonWeekTwoRepository` |
| MEGA-SHELL-038 | GAP | Story detail mini-article | `ReportArticleView` used elsewhere — **no** story detail screen for headlines | `news-hub.md#story-detail--articles` |
| MEGA-SHELL-039 | GAP | Portal / recruiting headline kinds in feed | `NewsHeadlineKind` lacks PortalEntry/Commit/Flip; generators not wired | `NewsHeadline.kt`, portal sim |
| MEGA-SHELL-040 | PARTIAL | Headline types (upsert, poll, carousel) | League results, injuries, awards, recruiting spotlight partially via `NewsHeadlinePublisher` | `NewsFeedRepository.appendLeagueWeek` |

---

## Notifications — dashboard feed (not News, not push)

| ID | Status | Spec / topic | Finding | Code / notes |
|----|--------|--------------|---------|--------------|
| MEGA-SHELL-041 | SHIP | In-app feed panel on in-season dashboard | `DashboardNotificationFeedPanel` when `showNotificationFeed` | `DynastyShellScreen.kt` |
| MEGA-SHELL-042 | SHIP | Default feed visible; settings toggle | `showDashboardNotificationFeed` default true | `AppSettings`, `SettingsScreen` |
| MEGA-SHELL-043 | SHIP | No OS push / tray | No FCM or system notification implementation in app module | grep; `notifications.md#design-locks` |
| MEGA-SHELL-044 | PARTIAL | Notification copy in `strings.xml` | **`NotificationFeedComposer` hard-coded English** title/body strings | `NotificationFeedComposer.kt` vs `branding.md` |
| MEGA-SHELL-045 | SHIP | User injury → high feed row | Composed with `DashboardDeepLink.Strategy` | `NotificationFeedComposer` |
| MEGA-SHELL-046 | SHIP | User commits / decommits → high | Repository helpers + composer kinds | `NotificationFeedRepository`, composer |
| MEGA-SHELL-047 | SHIP | Fired / postseason eliminated blocking modal | `requiresBlockingModal` + `CareerBlockNotificationDialog` | `DynastyShellScreen` |
| MEGA-SHELL-048 | PARTIAL | Week advance snapshot breadth | Shell snapshot wires injury, game week, coach points, fired — **not** decommit, signing recap, rankings, postseason invite on advance | `DynastyShellScreen` LaunchedEffect |
| MEGA-SHELL-049 | SHIP | Discipline sheet opened → feed row | `NotificationFeedDisciplineRowPolicy` + sidecar discipline headline | `notifications.md#event-catalog` |
| MEGA-SHELL-050 | SHIP | Carousel offer / must-sign extension feed rows | Carousel pending cards in composer | `notifications.md` high tier |
| MEGA-SHELL-051 | SHIP | Selection Sunday feed row | In-field seed row → March Mayhem hub | `notifications.md` |
| MEGA-SHELL-052 | SHIP | Medical redshirt eligible feed row | `MedicalRedshirtPolicy` bodies in composer | `notifications.md` medium |
| MEGA-SHELL-053 | SHIP | Fundraising / facility complete (low) | `FundraisingComplete` rows from week-nine economy | `notifications.md` |
| MEGA-SHELL-054 | SHIP | Stories not duplicated in notification feed | League churn goes to News archive, not notification kinds for league-only events | architecture split |
| MEGA-SHELL-055 | SHIP | Feed deep links | `PlayWeekGame` → `ShellWeekGamePreviewLauncher`; discipline / carousel / fundraising routes wired | `DashboardNotificationFeedPanel.kt` |
| MEGA-SHELL-056 | SHIP | Notification feed on offseason dashboard | `OffseasonScreen` loads feed + sidecar like shell | `NotificationFeedOffseasonPhasePolicy` |

---

## Discipline — conduct, academic, lineup

| ID | Status | Spec / topic | Finding | Code / notes |
|----|--------|--------------|---------|--------------|
| MEGA-SHELL-057 | SHIP | Rolls on week advance RS + tourney + postseason | `DisciplineCalendarPolicy` | `DisciplineOccurrenceEngine.kt` |
| MEGA-SHELL-058 | SHIP | Season cap on discipline resolutions | `maxPerSeason` in tuning | `DisciplineOccurrenceEngine` |
| MEGA-SHELL-059 | SHIP | Required dashboard card blocks advance | `DisciplineRequiredDashboardTasks` + `DashboardAdvanceGate` | `DashboardTaskGate`, shell |
| MEGA-SHELL-060 | SHIP | Full-screen decision sheet | `DisciplineDecisionScreen` route from shell | `DynastyApp.kt` |
| MEGA-SHELL-061 | SHIP | Conduct: dismiss, suspend 1–10, conference delegate | UI options match design lock | `DisciplineDecisionScreen.kt` |
| MEGA-SHELL-062 | GAP | Conference draw animated coin-flip UI | Instant resolve in core — no animated draw surface | `in-season-systems.md#conference-draw` |
| MEGA-SHELL-063 | SHIP | Academic: sweep, suspend games, hold + support | Three branches + slider | `DisciplineDecisionScreen` |
| MEGA-SHELL-064 | SHIP | `gamesSuspendedRemaining` decrement per team game | `decrementGameSuspensions` when `teamPlayedGame` | `DisciplineResolver.kt` |
| MEGA-SHELL-065 | PARTIAL | Bye weeks do not decrement suspensions | Decrement only when `teamPlayedGame` — depends on caller passing bye correctly | `DisciplineRepository.afterWeekAdvance` |
| MEGA-SHELL-066 | SHIP | Suspended / hold ineligible in lineup | `DisciplineResolver.isIneligibleForLineup` in Strategy + auto-starters | `StrategyScreen`, `DefaultStarterAlgorithm` |
| MEGA-SHELL-067 | PARTIAL | Greyed suspended players in Strategy UI | Ineligible treated like injury grey path via `isIneligibleForLineup` — verify visual distinction | `StrategyScreen.kt` |
| MEGA-SHELL-068 | SHIP | Weekly academic support GPA tick | `tickAcademicSupport` on week advance | `DisciplineResolver`, repository |
| MEGA-SHELL-069 | GAP | Cover-up audit end of season | `academicCoverUp` flag set; **no** `auditBaseChance` roll in save flow | `balance-2026.json`, `SaveSlotRepository` |
| MEGA-SHELL-070 | GAP | AD trust / job security / News on discipline outcomes | Fields in spec table — not wired from resolver to coach card + News | `in-season-systems.md#ad-trust` |
| MEGA-SHELL-071 | SHIP | Conduct flavor from balance templates | `DisciplineConductFlavorPolicy` uses template id + label | `DisciplineOccurrenceEngine.rollConduct` |
| MEGA-SHELL-072 | SHIP | Academic window RS weeks 18–22 | `DisciplineAcademicWindowPolicy` matches balance default weeks | `balance-2026.json` |

---

## Chemistry & fatigue

| ID | Status | Spec / topic | Finding | Code / notes |
|----|--------|--------------|---------|--------------|
| MEGA-SHELL-073 | SHIP | `teamChemistry` 0–100 persisted per season | `ChemistrySeasonState`, DataStore | `ChemistryRepository.kt` |
| MEGA-SHELL-074 | SHIP | Bootstrap ~70 new save | `ChemistryWeekEngine.bootstrap` | core tests |
| MEGA-SHELL-075 | SHIP | Weekly tick after advance (in-season phases) | `afterWeekAdvance` in repository + `SaveSlotRepository` | win/loss streak inputs |
| MEGA-SHELL-076 | SHIP | Display on School hub Overview + Strategy strip | `school_hub_chemistry`, `strategy_chemistry` | `SchoolHubScreen`, `StrategyScreen` |
| MEGA-SHELL-077 | SHIP | Chemistry clutch modifier in live sim | `SimChemistryClutchPolicy` in `PossessionSimSession` clutch possessions | `GameSimInput` chemistry fields |
| MEGA-SHELL-078 | SHIP | Promise broken / roster churn / charisma inputs | `ChemistryWeeklyTickInputPolicy.build` on week advance | `SaveSlotRepository` |
| MEGA-SHELL-079 | SHIP | Per-player `stamina` on roster model | DB columns + `FatigueEngine` | `RosterPlayer`, migrations |
| MEGA-SHELL-080 | SHIP | Fatigue tick on week advance | `DynastyRosterRepository.tickFatigueForWeekAdvance` | `SaveSlotRepository` |
| MEGA-SHELL-081 | SHIP | Strategy stamina readout + starter block &lt;20 | `strategy_stamina`, `starterBlockThreshold` | `StrategyScreen.kt` |
| MEGA-SHELL-082 | SHIP | Fatigue rotation pull in sim | `SimFatigueRotationPolicy` in rotation band + path test | `RotationBandFloorPolicy` |
| MEGA-SHELL-083 | SHIP | Press scheme / back-to-back drain extras | `FatiguePressBackToBackPolicy` on estimated game drain | `FatigueEngine.applyEstimatedGameDrain` |
| MEGA-SHELL-084 | SHIP | Chemistry does not block lineup edits | Display-only chemistry on Strategy | `StrategyScreen` |

---

## Cross-cutting shell UX

| ID | Status | Spec / topic | Finding | Code / notes |
|----|--------|--------------|---------|--------------|
| MEGA-SHELL-085 | SHIP | Resume from background skip studio | `LaunchResumeSkipStudioPolicy` + `rememberSaveable` studio beat consumed | `DynastyApp.kt` |
| MEGA-SHELL-086 | SHIP | Official product title constant | `Dynasty: College Basketball` single constant | `GameConstants.GAME_TITLE` |
| MEGA-SHELL-087 | GAP | `lint_player_strings.py` CI on notification composer | Hard-coded user-facing strings in core composer may fail lint if extended to core | `branding.md#ui-copy` |

---

## Hamburger navigation & hub routes

| ID | Status | Spec / topic | Finding | Code / notes |
|----|--------|--------------|---------|--------------|
| MEGA-SHELL-088 | SHIP | Hub menu core order (School → Exit) | `buildDynastyHubMenu` matches game-frame hub list | `DynastyHubMenu.kt`, `game-frame.md#top-menu-hamburger--hubs` |
| MEGA-SHELL-089 | SHIP | Conf tournament item phase-gated | Visible only `CalendarPhase.ConferenceTournament` | `DynastyHubMenuPolicy.kt` |
| MEGA-SHELL-090 | SHIP | March Mayhem item phase-gated | Visible only `CalendarPhase.Postseason` | `DynastyHubMenuPolicy.kt` |
| MEGA-SHELL-091 | SHIP | Exit to Home menu | `shell_menu_exit_home` callback pops to home | `DynastyHubMenu.kt` |
| MEGA-SHELL-092 | SHIP | Strategy only via hamburger (not bottom nav) | Dedicated menu item; bottom bar is Roster/Calendar/Dashboard/News | `DynastyShellChrome.kt` |
| MEGA-SHELL-093 | SHIP | In-season menu extras: Staff + Automation | `InSeasonShellMenuExtrasPolicy` gates Staff + Automation `leadingExtras` on live in-season shell | `InSeasonShellMenuExtrasPolicy.kt`, `DynastyShellScreen.kt` |
| MEGA-SHELL-094 | SHIP | Offseason/preseason reuse same hub builder | `buildDynastyHubMenu(position, hubMenuCallbacks)` on all shell phases | `OffseasonScreen.kt`, `PreseasonScreen.kt` |
| MEGA-SHELL-095 | SHIP | News hub route from menu all phases | `AppRoute.NewsHub` via `onOpenNewsHub` | `DynastyApp.kt` |
| MEGA-SHELL-096 | SHIP | Statistics hub product vs menu promise | `StatisticsHubRouteScreen` + live leaderboards via `StatisticsHubLiveContentPolicy` | `StatisticsHubLiveContentPolicy.kt`, `StatisticsHubScreen.kt` |
| MEGA-SHELL-097 | SHIP | Rankings hub route (NET / Media / Coaches) | `AppRoute.RankingsHub` from menu | `DynastyApp.kt`, `RankingsHubScreen.kt` |
| MEGA-SHELL-098 | SHIP | History hub vs design depth | `HistoryHubDepthPolicy.majorRecordFeedLines` merges records, champions, GOAT anchors | `HistoryHubDepthPolicy.kt`, `HistoryHubScreen.kt` |
| MEGA-SHELL-099 | SHIP | Hamburger UI pattern | `ModalNavigationDrawer` via `ShellHamburgerDrawerPolicy` | `ShellHamburgerDrawerPolicy.kt`, `DynastyShellChrome.kt` |
| MEGA-SHELL-100 | SHIP | Hub routes lose shell bottom tabs | Overlay hubs `popBackToInShellDashboard` restores dashboard tab (`HubRouteShellRetentionPolicy`) | `HubRouteShellRetentionPolicy.kt`, `DynastyApp.kt` |
| MEGA-SHELL-101 | SHIP | Awards hub from menu | `AppRoute.AwardsHub` wired | `DynastyApp.kt` |
| MEGA-SHELL-102 | SHIP | Recruiting hub no focused-player deep link param | News portal row + `recruitingFocusProspectId` → Transfer Portal tab + highlight | `RecruitingHubFocusPolicy.kt`, `NewsHubScreen.kt`, `DynastyApp.kt` |
| MEGA-SHELL-103 | SHIP | School hub from menu | `onOpenSchool` → school overview route | `DynastyHubMenuCallbacks` |
| MEGA-SHELL-104 | SHIP | Season Review phase menu parity | `SeasonReviewMenuPolicy` + full `buildDynastyHubMenu` on season review shell | `SeasonReviewMenuPolicy.kt`, `SeasonReviewScreen.kt` |
| MEGA-SHELL-105 | SHIP | Menu strings in `strings.xml` | All `shell_menu_*` labels centralized | `strings.xml`, `branding.md#ui-copy` |

---

## Deep links & drill-in

| ID | Status | Spec / topic | Finding | Code / notes |
|----|--------|--------------|---------|--------------|
| MEGA-SHELL-106 | SHIP | Feed deep-link catalog | `DashboardNotificationDeepLinkCatalogPolicy` + `HistoryHub` enum; panel routes rankings/history | `DashboardNotificationDeepLinkCatalogPolicy.kt`, `DashboardNotificationFeedPanel.kt` |
| MEGA-SHELL-107 | SHIP | Rankings move row | `NotificationFeedRankingsMoveRowPolicy` → `RankingsHub` | `NotificationFeedRankingsMoveRowPolicy.kt`, `NotificationFeedComposer.kt` |
| MEGA-SHELL-108 | SHIP | Game-this-week → preview | `NotificationFeedGameWeekRowPolicy` + panel `onGamePreviewOpen` | `NotificationFeedGameWeekRowPolicy.kt`, `DashboardNotificationFeedPanel.kt` |
| MEGA-SHELL-109 | SHIP | Selection Sunday feed | Composer + `NotificationFeedSelectionSundayFeedPolicy` + March Mayhem tap | `NotificationFeedSelectionSundayFeedPolicy.kt`, `NotificationFeedComposer.kt` |
| MEGA-SHELL-110 | SHIP | Discipline feed deep link | `NotificationFeedDisciplineFeedPolicy` + `DisciplineSheet` in composer/panel | `NotificationFeedDisciplineFeedPolicy.kt` |
| MEGA-SHELL-111 | SHIP | Medical redshirt feed row | `NotificationFeedMedicalRedshirtFeedPolicy` + `MedicalRedshirtEligible` composer rows | `NotificationFeedMedicalRedshirtFeedPolicy.kt` |
| MEGA-SHELL-112 | SHIP | Story drill-in | `NewsStoryDrillInPolicy` + `NewsHeadlineStoryDetailSheet` (shell + hub) | `NewsStoryDrillInPolicy.kt`, `NewsHeadlineStoryDetailSheet.kt` |
| MEGA-SHELL-113 | SHIP | Portal story footer | `NewsPortalStoryFooterPolicy` + View in Portal CTA | `NewsPortalStoryFooterPolicy.kt`, `NewsHeadlineStoryDetailSheet.kt` |
| MEGA-SHELL-114 | SHIP | Commit feed → Recruiting Recommended / Portal | `NotificationFeedCommitRowPolicy` + portal commit list | `NotificationFeedComposer.kt` |
| MEGA-SHELL-115 | SHIP | Major national record → History hub | `NotificationFeedMajorRecordRowPolicy` | `NotificationFeedComposer.kt` |
| MEGA-SHELL-116 | SHIP | Fired / eliminated modal + Coach deep link | `requiresBlockingModal` + `CoachCard` link | `CareerBlockNotificationDialog`, shell |
| MEGA-SHELL-117 | SHIP | Signing Day deep links | Feed passes `row.deepLink` → `SigningDayInitialTab` | `NotificationFeedSigningDayFeedPolicy`, `DashboardNotificationFeedPanel.kt` |
| MEGA-SHELL-118 | SHIP | News hub Back preserves dashboard tab | `popBackToInShellDashboard` on News hub | `NewsHubBackNavigationPolicy`, `DynastyApp.kt` |

---

## Notification channels & feed catalog

| ID | Status | Spec / topic | Finding | Code / notes |
|----|--------|--------------|---------|--------------|
| MEGA-SHELL-119 | SHIP | In-app only — no FCM channel | No `NotificationChannel` / tray implementation | `AndroidManifest.xml`, `notifications.md#design-locks` |
| MEGA-SHELL-120 | SHIP | Master toggle: show dashboard feed | `AppSettings.showDashboardNotificationFeed` + Settings UI | `SettingsScreen.kt` |
| MEGA-SHELL-121 | SHIP | Per-category mute (settings) | `NotificationCategoryMutePolicy` + Settings toggles | `SettingsScreen.kt`, `AppSettings.kt` |
| MEGA-SHELL-122 | SHIP | Priority tiers High / Medium / Low in model | `NotificationPriority` on each `NotificationFeedItem` | `NotificationFeedItem.kt` |
| MEGA-SHELL-123 | SHIP | Sort newest-first within tier | `NotificationFeedTierSortPolicy` in repository/engine | `NotificationFeedRepository.kt` |
| MEGA-SHELL-124 | SHIP | `FundraisingComplete` kind unused | Composer emits on week-nine completed fundraisers / naming | `NotificationFeedItem.kt` vs `notifications.md` low tier |
| MEGA-SHELL-125 | SHIP | Carousel offer / extension feed rows | `NotificationFeedCarouselComposerPolicy` + sidecar bodies | `NotificationFeedComposer.kt` |
| MEGA-SHELL-126 | SHIP | Medical redshirt notification | `NotificationFeedMedicalRedshirtComposerPolicy` | `NotificationFeedComposer.kt` |
| MEGA-SHELL-127 | SHIP | Discipline opened → high feed row | `NotificationFeedDisciplineComposerPolicy` | `NotificationFeedComposer.kt` |
| MEGA-SHELL-128 | SHIP | Week-advance batch rows | `DashboardNotificationWeekAdvanceAppender` post `advanceDynastyWeek` | `SaveSlotRepository.kt` |
| MEGA-SHELL-129 | SHIP | Stories never duplicated as notifications | League week → `NewsFeedRepository` only | architecture split |
| MEGA-SHELL-130 | SHIP | Offseason portal commit feed rows | `userPortalCommitsThisWeek` + `NotificationFeedPortalOffseasonPolicy` | `NotificationFeedComposer.kt` |
| MEGA-SHELL-131 | SHIP | Portal alerts excluded in-season | No in-season transfer kinds in composer | design lock |
| MEGA-SHELL-132 | SHIP | One row per event on advance | `NotificationWeekAdvanceAppendPolicy.ONE_ROW_PER_EVENT` | pipeline stage marker |
| MEGA-SHELL-133 | SHIP | Task vs feed dedup rules | `NotificationTaskFeedDedupPolicy` (discipline + carousel) | `DashboardNotificationSnapshotSidecarLoader.kt` |

---

## News categories & headline taxonomy

| ID | Status | Spec / topic | Finding | Code / notes |
|----|--------|--------------|---------|--------------|
| MEGA-SHELL-134 | SHIP | `NewsHeadlineKind` surface labels on dashboard | `NewsHeadlineKindChipPolicy` + `newsHeadlineKindLabel` | `DynastyShellTabContent.kt` |
| MEGA-SHELL-135 | SHIP | Hub Stories tab hides kind chip | `NewsHubStoriesRowPolicy` kind + week row | `NewsHubScreen.kt` |
| MEGA-SHELL-136 | SHIP | Portal headline kinds in enum | `NewsPortalHeadlineKindPolicy` + publisher helpers | `NewsHeadline.kt` |
| MEGA-SHELL-137 | SHIP | Poll / NET movement headlines | `PollNetMovementNewsEngine` in `appendLeagueWeek` | `NewsFeedRepository.kt` |
| MEGA-SHELL-138 | PASS | Bracketology (February) stories | `BracketologyNewsEngine` on league week advance | `NewsFeedRepository.kt`, `MEGA-GDD-130` |
| MEGA-SHELL-139 | GAP | Coaching carousel league headlines | No `NewsHeadlineKind` or publisher path for carousel | `news-hub.md` brief list |
| MEGA-SHELL-140 | PARTIAL | Awards headlines kind | `appendAwardsHeadlines` + `NewsHeadlineKind.Awards` | postseason awards wiring |
| MEGA-SHELL-141 | SHIP | User vs league injury kinds split | `UserInjury` vs `LeagueInjury` | `NewsHeadlinePublisher.kt` |
| MEGA-SHELL-142 | PARTIAL | Recruiting spotlight / Top 60 story | Single seeded `top60-{season}` headline | `ensureRecruitingSpotlight` |
| MEGA-SHELL-143 | GAP | Tournament / postseason story lines | League batch RS-focused — postseason headlines not in `appendLeagueWeek` | `LeagueWeekBatchRepository.kt` |
| MEGA-SHELL-144 | SHIP | Archive cap ~200 headlines | `takeLast(200)` on merge | `NewsFeedRepository.kt` |
| MEGA-SHELL-145 | PARTIAL | Shallow cap vs spec 8–12 | `SHALLOW_FEED_CAP = 10` within range | `NewsHeadlinePublisher.kt` |
| MEGA-SHELL-146 | GAP | Priority boost user-school / portal window | Sort uses priority field — portal boost generators absent | `ShallowNewsFeed.kt` |

---

## In-season polls, NET & stats surfaces

| ID | Status | Spec / topic | Finding | Code / notes |
|----|--------|--------------|---------|--------------|
| MEGA-SHELL-147 | SHIP | Weekly NET attach on RS advance | `attachWeeklyNet` in league week batch | `LeagueWeekBatchRepository.kt` |
| MEGA-SHELL-148 | GAP | NET/poll movement as News stories | Rankings engine runs; **no** headline publisher for T25 moves | `news-hub.md` mixed feed |
| MEGA-SHELL-149 | SHIP | Rankings hub NET / Media / Coaches tabs | Full hub when polls published | `RankingsHubScreen.kt` |
| MEGA-SHELL-150 | PARTIAL | Rankings vs News Recruits disambiguation | Separate hubs — no cross-link from hamburger Rankings to recruit Top 60 | `MEGA-RECR-123` |
| MEGA-SHELL-151 | GAP | Statistics hub national leaders | Placeholder — in-season stats surface not live | `statistics_hub_placeholder` |
| MEGA-SHELL-152 | PARTIAL | Preseason rankings review vs News | Team poll publication task — not reflected in News feed | `PreseasonTaskId.RankingsReview` |
| MEGA-SHELL-153 | GAP | User-team large NET move → low-priority feed | `rankingsMoveLine` input exists but shell snapshot rarely passes it | `NotificationSnapshotInput`, shell |
| MEGA-SHELL-154 | N/A | In-season portal stats in News | Portal tab hidden in-season — by design | `NewsHubPolicy` |
| MEGA-SHELL-155 | PARTIAL | Draft tab dynamic in-season board | Loads `offseasonWeekTwoRepository` published lines — RS weeks usually empty | `NewsHubScreen.kt` `DraftTab` |

---

## Branding tokens & copy CI

| ID | Status | Spec / topic | Finding | Code / notes |
|----|--------|--------------|---------|--------------|
| MEGA-SHELL-156 | SHIP | `brand_background` `#181A25` in resources | Matches branding lock | `colors.xml`, `branding.md#color` |
| MEGA-SHELL-157 | SHIP | On-surface tokens in colors + theme | `#E8EAED`, `#A8AEB8` mirrored in `Theme.kt` | `colors.xml`, `Theme.kt` |
| MEGA-SHELL-158 | SHIP | `GameConstants.BRAND_BACKGROUND_HEX` single source | Theme references constant | `GameConstants.kt`, `Theme.kt` |
| MEGA-SHELL-159 | GAP | Studio splash tap hint not in strings | Hard-coded English in `StudioScreen` | `MEGA-SHELL-007` |
| MEGA-SHELL-160 | GAP | Notification composer copy not in strings | All titles/bodies inline English in core | `NotificationFeedComposer.kt` |
| MEGA-SHELL-161 | PARTIAL | Shell header product framing | `shell_dynasty_school` = “Dynasty: {school}” — not full official title on chrome | `strings.xml` vs store title |
| MEGA-SHELL-162 | SHIP | `PackDisplayNames` for league labels | Core helper for pack-facing names | `branding.md#ui-copy` |
| MEGA-SHELL-163 | PARTIAL | Home menu title text-only | No app logo on home — same as MEGA-SHELL-016 | `HomeMenuScreen.kt` |
| MEGA-SHELL-164 | SHIP | Credits legal strings centralized | Publisher, disclaimer, novux.dev in `strings.xml` | `CreditsScreen.kt` |
| MEGA-SHELL-165 | GAP | Core notification strings vs `lint_player_strings.py` | Lint targets player strings — composer escape hatch | `branding.md#ui-copy`, CI scope |

---

## Adaptive icon & launcher

| ID | Status | Spec / topic | Finding | Code / notes |
|----|--------|--------------|---------|--------------|
| MEGA-SHELL-166 | SHIP | Adaptive background `@color/brand_background` | Matches `adaptive-icon-background.svg` intent | `ic_launcher.xml`, `branding.md#adaptive-icon` |
| MEGA-SHELL-167 | SHIP | Foreground PNG synced from Public-Assets | `ic_launcher_foreground.png` byte-identical to `Public-Assets/adaptive-icon/adaptive-icon-foreground.png` | repo compare 2026-11-11 |
| MEGA-SHELL-168 | SHIP | Round + square adaptive mipmaps | `ic_launcher` + `ic_launcher_round` v26 | `mipmap-anydpi-v26/` |
| MEGA-SHELL-169 | PARTIAL | Foreground references `@drawable/ic_launcher_foreground` | Release merge may use `ic_launcher_foreground_adaptive` wrapper — verify release artifact | `ic_launcher_foreground_adaptive.xml` |
| MEGA-SHELL-170 | GAP | Automated CI check Public-Assets → Android copy | No script enforcing foreground drift | `branding.md` implementation notes |
| MEGA-SHELL-171 | N/A | `adaptive-icon-canvas.png` QA composite | Marketing asset only — not shipped in APK | `Public-Assets/adaptive-icon/` |
| MEGA-SHELL-172 | SHIP | Package id `com.dynasty.cbb` on launcher | Store listing alignment | `build.gradle`, `PLAY_STORE_LISTING.md` |

---

## Shell chrome & lifecycle

| ID | Status | Spec / topic | Finding | Code / notes |
|----|--------|--------------|---------|--------------|
| MEGA-SHELL-173 | SHIP | Bottom nav four tabs + icons | Roster, Calendar, Dashboard, News | `DynastyShellChrome.kt` |
| MEGA-SHELL-174 | SHIP | Top bar school + phase/week subtitle | Two-line title block | `DynastyShellChrome.kt` |
| MEGA-SHELL-175 | PARTIAL | Brand field on shell scaffold | Material `Scaffold` theme — not explicit `#181a25` on all shell surfaces | `Theme.kt` |
| MEGA-SHELL-176 | GAP | Resume from background skip studio | No `ProcessLifecycle` path — cold start only audited | `app-shell.md#launch-flow`, `DynastyApp.kt` |
| MEGA-SHELL-177 | PARTIAL | News hub chrome vs in-shell News tab | Hub uses standalone `TopAppBar` — different from dynasty chrome | `NewsHubScreen.kt` |
| MEGA-SHELL-178 | SHIP | Settings skip studio affects `startRoute` | `skipStudioIntro` → Splash not Studio | `DynastyApp.kt` |
| MEGA-SHELL-179 | GAP | Splash loading spinner vs ready gate | Fixed delay — not tied to pack index | `MEGA-SHELL-011` |
| MEGA-SHELL-180 | PARTIAL | Offseason/preseason News tab | Placeholder empty feed — should show shallow slice when archive has OS portal headlines | `DynastyShellNewsTabPlaceholder` |
| MEGA-SHELL-181 | SHIP | In-season dashboard panels stack | Tasks + optional notification feed + news on dashboard tab | `DynastyShellScreen.kt` |
| MEGA-SHELL-182 | GAP | Per-category notification settings UI section | Settings has notifications section — only master toggle | `SettingsScreen.kt` |

---

## Summary counts

| Status | Count |
|--------|------:|
| SHIP | 79 |
| PARTIAL | 39 |
| GAP | 62 |
| N/A | 2 |
| **Total items** | **182** |

### P0 backlog (ship blockers)

| ID | Topic |
|----|--------|
| MEGA-SHELL-012 | Splash pack fail + Retry — blocks clean first run |
| MEGA-SHELL-023 | Offseason/preseason News tab live shallow feed |
| MEGA-SHELL-031 | Portal Top 40 human-readable rows |
| MEGA-SHELL-038 | Story detail mini-article |
| MEGA-SHELL-039 | Portal headline kinds + generators |
| MEGA-SHELL-044 | Notification copy → `strings.xml` |
| MEGA-SHELL-049–051 | Discipline / carousel / Selection Sunday feed rows |
| MEGA-SHELL-056 | Notification feed on offseason dashboard |
| MEGA-SHELL-096 | Statistics hub placeholder behind live menu item |
| MEGA-SHELL-106–110 | Notification + news deep links (Rankings, game preview, discipline, Selection Sunday) |
| MEGA-SHELL-112–113 | News headline drill-in + portal story CTAs |
| MEGA-SHELL-137–138 | Poll / bracketology News headlines |
| MEGA-SHELL-160 | Hard-coded notification strings (branding CI) |

**Priority clusters:** MEGA-SHELL-031–035 (News portal product depth), MEGA-SHELL-038–039 / 112–113 (stories + drill-in), MEGA-SHELL-044–056 / 119–133 (notification catalog + advance + strings), MEGA-SHELL-088–102 (hamburger hubs + Statistics placeholder), MEGA-SHELL-106–118 (deep links), MEGA-SHELL-134–146 (headline taxonomy), MEGA-SHELL-147–155 (NET/polls vs News), MEGA-SHELL-062/069–070 (discipline UX + consequences), MEGA-SHELL-077–078 (chemistry sim), MEGA-SHELL-011–012 / 176 (splash + lifecycle).

---

## Document history

| Date | Change |
|------|--------|
| 2026-11-11 | Initial audit vs app-shell, branding, news-hub, notifications, in-season-systems |
| 2026-11-11 | +95 rows (088–182): hamburger hubs, deep links, notification catalog, news taxonomy, NET/polls/stats, branding tokens, adaptive icon, shell chrome (182 total) |
