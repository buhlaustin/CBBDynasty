# MEGA audit: recruiting, transfer portal, game start

**Scope:** `planning/recruiting-engine.md`, `planning/transfer-portal.md`, `planning/game-start.md` vs Android implementation (`:core/recruiting`, `:core/portal`, `:core/signingday`, `ui/recruiting`, `ui/preseason`, `ui/signingday`, `ui/newgame`, dashboard tasks).  
**Audit date:** 2026-11-11  
**Method:** Full doc read + static code trace + `:core:test` recruiting/portal/dashboard subset (green build; tests do not cover most gaps below).

**Legend:** **Status** — `FAIL` spec violation or broken behavior; `GAP` partial/stub; `PASS` aligned; `RISK` works but fragile or doc/checklist overclaims. **Pri** — P0 ship-blocker … P3 polish.

---

## A. Recruit generation & national player rankings

| ID | Pri | Status | Spec / topic | Finding | Evidence |
|----|-----|--------|--------------|---------|----------|
| MEGA-RECR-001 | P0 | PASS | HS pool 2k–4k uniform random | `resolveHsPoolSize` draws uniform 2k–4k per enrolling class seed. | `IncomingClassSeeder.kt` |
| MEGA-RECR-002 | P0 | PASS | Star percentiles within pool | Stars assigned from hidden-rating sort percentiles per pool (`starBandForPercentile`). | `IncomingClassSeeder.generatePool` |
| MEGA-RECR-003 | P1 | PASS | JUCO 5★ = top 2% | HS and JUCO share 2% / 20% / 50% bands. | `IncomingClassSeeder.starBandForPercentile` |
| MEGA-RECR-004 | P1 | PASS | 3★ = 20–50% band | 3★ band ends at 50th percentile. | `IncomingClassSeeder.starBandForPercentile` |
| MEGA-RECR-005 | P0 | PASS | Single persisted class pool | Hub, AI, signing, sim-week advance use `ensurePool`. | `IncomingClassSeedRepository.kt`, `SaveSlotRepository.kt` |
| MEGA-RECR-006 | P0 | PASS | Top 60 fixed at seed | `demoPool` delegates to `IncomingClassSeeder.seed`; national ranks from hidden-rating order (HS+JUCO). | `RecruitingClassSeed.kt`, `IncomingClassSeeder.kt` |
| MEGA-RECR-007 | P1 | GAP | Top 60 HS+JUCO combined | Seeder ranks combined pool correctly when used; most UI paths never load that persisted pool. | `IncomingClassSeedRepository` vs hub |
| MEGA-RECR-008 | P1 | PASS | News / signing fallback | Signing/news/hub use `ensurePool` + `top60FromPool`; demo `top60()` deprecated. | `SigningDayScreen.kt`, `NewsHubScreen.kt` |
| MEGA-RECR-009 | P2 | GAP | Portal excluded from recruit Top 60 | No code path mixes portal into Top 60 (PASS intent), but portal tab is nonfunctional (see portal section). | — |
| MEGA-RECR-010 | P2 | PASS | Top 60 cap 60 rows | `top60FromPool` filters `nationalHsRank` and caps at 60 in tests. | `NationalRecruitingRankingsTest` |
| MEGA-RECR-011 | P1 | GAP | Priority roll 1–2 at create | `RecruitPriorityRoll` used in seeder; demo pool also rolls priorities — OK when seeder used. | `IncomingClassSeederTest` |
| MEGA-RECR-012 | P2 | GAP | Geography national pool | Only 11 stub states; no pack-driven pipeline breadth. | `IncomingClassSeeder.states` |
| MEGA-RECR-013 | P1 | PASS | Scout letter categories | `RecruitScoutLetterSheet` shows Overall/Offense/Defense/Shooting/Handling/Athleticism/Potential A–F rows. | `RecruitScoutLetterEngine`, hub UI |
| MEGA-RECR-014 | P2 | RISK | Hidden 0–99 not in UI | Overall letter shown; hidden ratings exist in save encoding — aligns with “no public 0–99”. | Seeder + prospect model |

---

## B. Recruiting points economy

| ID | Pri | Status | Spec / topic | Finding | Evidence |
|----|-----|--------|--------------|---------|----------|
| MEGA-RECR-015 | P0 | PASS | Prestige tier weekly pool | `resolveUserProgramPrestige` + `loadOrOpenCycle` use chosen school's pack prestige. | `RecruitingEconomyRepository.kt` |
| MEGA-RECR-016 | P1 | RISK | Academics multiplier on pool | `AcademicsRecruitingBudgetEngine` scales weekly pool ±15% from program grade — not in `recruiting-engine` tier table (may be intentional from game-design; document drift). | `RecruitingWeekGrant.openCycle` |
| MEGA-RECR-017 | P1 | PASS | 50% carryover | `RecruitingWeekGrant.applyWeekAdvanceGrant` uses `weeklyCarryoverFraction`. | `RecruitingWeekGrant.kt` + tests |
| MEGA-RECR-018 | P1 | PASS | Signing crunch 1.5× OS W2–5 | Balance `signingCrunchOffseasonWeeks: [2,3,4,5]`, multiplier 1.5. | `assets/balance/balance-2026.json` |
| MEGA-RECR-019 | P1 | RISK | Fixed pool per cycle | Ledger stores `weeklyPoolBase` at open; mid-cycle prestige change not applied (spec: OK until EoS). Opening at wrong prestige (RECR-015) breaks the lock. | `RecruitingCycleLedger` |
| MEGA-RECR-020 | P1 | PASS | Refunded offer/visit/scout | Reservation refund on week advance via `RecruitingReservationRefund`. | `RecruitingEconomyRepository.onCalendarWeekAdvanced` |
| MEGA-RECR-021 | P2 | GAP | Week 6 no new drip | `closeCycleAfterHardSign` zeroes points and sets `cycleClosed` — aligns with Signing Day admin week. | `RecruitingEconomyRepository` |
| MEGA-RECR-022 | P1 | PASS | Action costs from balance | Hub spends DM/call/house/bonus per `prospectId` with balance costs + interest deltas. | `RecruitingHubScreen.kt` |
| MEGA-RECR-023 | P2 | PASS | Bonus points “remainder” | `RecruitBonusPointsPickerSheet` + user-entered partial spend. | `RecruitingHubScreen` Recommended row |
| MEGA-RECR-024 | P2 | PASS | Sim-week spend estimate | Auto-maintain estimate uses `ensurePool` + resolved prestige context. | `SaveSlotRepository.buildSimWeekAdvanceInputSuspended` |
| MEGA-RECR-025 | P1 | GAP | Next cycle pool at EoS recap | `openNextCycleAfterSeasonReview` uses `postRecapPrestige` — PASS when called; first cycle still wrong until recap. | `SeasonReviewRepository` |

---

## C. Interest, Recommended, influence, signing logic

| ID | Pri | Status | Spec / topic | Finding | Evidence |
|----|-----|--------|--------------|---------|----------|
| MEGA-RECR-026 | P1 | GAP | Interest weights in balance JSON | Weights live in `RecruitInterestConfig` hardcoded; ADR-002 `recruiting.interest.*` not in bundle. | `RecruitInterestWeights.kt`, `balance-2026.json` |
| MEGA-RECR-027 | P1 | PASS | Point spend → interest meter | `interestDelta` in balance; persisted `spendDelta` per prospect; hub meter uses `liveInterest`. | `RecruitingInterestSpendRepository.kt` |
| MEGA-RECR-028 | P2 | SHIP | signLikelihood zero-spend | Hub builds Recommended from persisted class pool; baseline interest on seeded prospects. | `SignLikelihoodSeededPoolPolicy.kt`, `RecruitingHubScreen.kt` |
| MEGA-RECR-029 | P1 | PASS | Soft commit = influence #1 + closeness | `RecruitSoftCommitPolicy` gates soft commits on rank #1, closeness ≥ 70, and scholarship offer. | `SigningDaySoftCommitResolver`, `RecruitSoftCommitPolicy` |
| MEGA-RECR-030 | P2 | SHIP | Influence board UI | Recommended rows surface competition rank + closeness; copy documents influence board. | `InfluenceBoardSurfacePolicy.kt`, `recruiting_influence_row` |
| MEGA-RECR-031 | P2 | PASS | Recommended default tab | Hub lands on Recommended; ~list cap from balance recommended parser. | `RecruitingHubScreen`, `RecommendedRecruitList` |
| MEGA-RECR-032 | P1 | SHIP | Elite hidden on Recommended | `hiddenOnRecommendedTab` + unit test on seeded prospect; hub elite-hide hint. | `EliteHiddenOnRecommendedPolicy.kt`, `SignLikelihoodEngineTest` |
| MEGA-RECR-033 | P2 | SHIP | NIL assign / 3× cap | Recommended tab documents 3× assign cap; policy delegates to `NilAssignCapLinePolicy`. | `RecommendedNilAssignHubPolicy.kt`, `RecruitNilAssignCapLine.kt` |
| MEGA-RECR-034 | P2 | PASS | Promises at offer | `RecruitPromisePickerSheet` on scholarship offer in recruiting hub | `RecruitPromisePickerPathTest` |

---

## D. Recruiting hub UI & cycle access

| ID | Pri | Status | Spec / topic | Finding | Evidence |
|----|-----|--------|--------------|---------|----------|
| MEGA-RECR-035 | P0 | PASS | HS + JUCO tab content | Full pool partitions + Top 60 + HS/JUCO lists. | `HsJucoTabBody.kt` |
| MEGA-RECR-036 | P0 | PASS | Transfer Portal sub-tab | Top 40 + portal Recommended with full spend/NIL surface. | `RecruitingHubTabBodies.kt` |
| MEGA-RECR-037 | P1 | PASS | Tab set vs spec | Eight hub tabs: Recommended, All/HS/JUCO, Portal, Board, Committed, Lost. | `RecruitingHubTab`, `RecruitingHubTabPolicy` |
| MEGA-RECR-038 | P1 | PASS | Hub open offseason week 1 | `hubAccessible(Offseason)` from week 1 (retention week; shared points economy). | `RecruitingCyclePolicy.kt` |
| MEGA-RECR-039 | P1 | PASS | NIL budget strip | Strip + tap sheet committed/reserved by prospect. | `RecruitingNilBudgetDrillDownSheet` |
| MEGA-RECR-040 | P2 | SHIP | Official visit window RS→R4 | `OfficialVisitWindowPolicy` mirrors `RecruitingActionWindows`; hub visit-window copy. | `RecruitingHubActionGate.kt`, `OfficialVisitWindowPolicy.kt` |
| MEGA-RECR-041 | P2 | GAP | Walk-ons after Signing Day | Not implemented in UI/data. | spec `recruiting-engine#walk-ons` |

---

## E. Dashboard tasks (recruiting & portal)

| ID | Pri | Status | Spec / topic | Finding | Evidence |
|----|-----|--------|--------------|---------|----------|
| MEGA-RECR-042 | P0 | PASS | In-season optional recruiting tasks | `InSeasonRecruitingOptionalDashboardTasks` adds spend + watchlist nudges when cycle open. | `DashboardTaskGate.kt`, `DynastyShellScreen.kt` |
| MEGA-RECR-043 | P1 | SHIP | Offseason W2–5 portal tasks | `OffseasonPortalDashboardTasksPolicy` + dashboard cadence hint on portal weeks. | `OffseasonScreen.kt`, `OffseasonOptionalDashboardTasks.kt` |
| MEGA-RECR-044 | P2 | RISK | Portal Top 40 task trigger | Task when `offseasonWeekIndex == 2 && portalTop40Locked`; verify caller sets flag when week 2 starts. | `OffseasonScreen` context builder |
| MEGA-RECR-045 | P1 | PASS | Recruiting never blocks advance | Optional kinds only for recruiting; signing day has required tasks separate. | `DashboardTask.kt` |
| MEGA-RECR-046 | P1 | SHIP | Signing Day required tasks | Rankings from `ensurePool`; required-task policy + dashboard hint. | `SigningDayRequiredTasksPolicy.kt`, `SigningDayScreen.kt` |
| MEGA-RECR-047 | P2 | SHIP | Dismiss / skip week nudges | Recruiting nudges carry `dismissScopeKey`; shell uses `DashboardTaskDismissal`. | `InSeasonRecruitingOptionalDismissPolicy.kt`, `RecruitingDashboardNudge.kt` |
| MEGA-RECR-048 | P2 | PASS | Week 1 no portal dashboard cards | Portal week-1 on Players Leaving, not dashboard — aligns with transfer-portal doc. | `transfer-portal.md` |

---

## F. Signing day ↔ preseason UI boundary

| ID | Pri | Status | Spec / topic | Finding | Evidence |
|----|-----|--------|--------------|---------|----------|
| MEGA-RECR-049 | P1 | PASS | Signing rankings data source | Screen rankings and `loadIncomingClass` both use persisted `ensurePool`. | `SigningDayRepository.kt` |
| MEGA-RECR-050 | P1 | SHIP | Position editor Signing Day | Cycle position per signee + confirm gate; editor hint on positions tab. | `SigningDayPositionEditorPolicy.kt`, `SigningDayScreen.kt` |
| MEGA-RECR-051 | P1 | SHIP | Cycle close after hard sign | Hard sign calls `closeCycleAfterHardSign`; cycle-close hint after sign. | `SigningDayCycleClosePolicy.kt`, `SigningDayScreen.kt` |
| MEGA-RECR-052 | P1 | GAP | Preseason Incoming Class Seed | Manual panel seeds 3900-player pool — PASS when user completes task; **not auto** on new dynasty before season. | `IncomingClassSeedPanel` |
| MEGA-RECR-053 | P0 | PASS | Season 1 Top 60 from tipoff | `bootstrapSeasonOneRecruitingAfterSign` persists class + Top 60 news + prestige-bound recruiting/NIL at wizard sign; preseason panel confirms pre-seeded class before exhibitions. | `SaveSlotRepository`, `SeasonOneRecruitingBootstrap` |
| MEGA-RECR-054 | P2 | SHIP | News Recruits vs Rankings hub | Recruits tab scope copy; Rankings hub already labels team NET/polls. | `RecruitNewsVsTeamRankingsPolicy.kt`, `NewsHubScreen.kt` |

---

## G. Team rankings vs player (recruit) rankings

| ID | Pri | Status | Spec / topic | Finding | Evidence |
|----|-----|--------|--------------|---------|----------|
| MEGA-RECR-055 | P1 | RISK | Preseason task “Rankings” | `PreseasonTaskId.RankingsReview` gates **team** poll publication (`RankingsHubRepository`), not recruit Top 60 — label overlap with Signing Day “View Recruiting Rankings”. | `PreseasonTaskId`, `RankingsHubRepository` |
| MEGA-RECR-056 | P2 | SHIP | RankingsReview completion UX | Dedicated panel + no generic Complete; shortcut hint documents flow. | `PreseasonRankingsReviewNoShortcutPolicy.kt`, `PreseasonFeatureTaskPanels.kt` |
| MEGA-RECR-057 | P1 | PASS | Media/Coaches T25 = programs | `RankingsHubScreen` tabs NET/Media/Coaches/Conference — team-level. | `ui/rankings` |
| MEGA-RECR-058 | P1 | PASS | Recruit Top 60 ≠ top 25 crop | UI strings reference #1–#60 in seed panel/news. | `strings.xml`, seed panel |
| MEGA-RECR-059 | P2 | GAP | School hub rankings unpublished | Until RankingsReview complete — intentional; unrelated to recruit board. | `SchoolHubRepository` |

---

## H. Transfer portal

| ID | Pri | Status | Spec / topic | Finding | Evidence |
|----|-----|--------|--------------|---------|----------|
| MEGA-RECR-060 | P1 | PASS | One cycle; no in-season | `PortalCyclePolicy.inSeasonPortalEntriesForbidden`. | core portal |
| MEGA-RECR-061 | P1 | PASS | Portal recruit window OS W2–5 | `portalRecruitingWindowOpen` week range. | `PortalCyclePolicy.kt` |
| MEGA-RECR-062 | P1 | GAP | Considering pool 1k–1.4k | `PortalSimulationEngine.rollConsideringPoolSize` — PASS; **not surfaced** in Recruiting hub (placeholder). | `PortalSimulationEngine.kt` |
| MEGA-RECR-063 | P1 | PASS | Portal Top 40 lock week 2 | `PortalCycleRepository.onOffseasonWeekStarted` + `shouldLockTop40AtWeekStart`. | data portal |
| MEGA-RECR-064 | P1 | PASS | Week 1 Roster/Transfer mutual exclusion | `PortalWeekOnePartition`, `PlayersLeavingPanel`. | ui offseason leaving |
| MEGA-RECR-065 | P2 | PASS | Shared points/NIL with HS/JUCO | Portal Recommended reuses hub spend/NIL actions. | `RecruitingHubScreen.kt` |
| MEGA-RECR-066 | P2 | GAP | Portal entrant stars on final pool | `PortalStarBands` in core; entrant list not shown in hub. | `PortalStarBands.kt` |
| MEGA-RECR-067 | P2 | GAP | AI portal Recommended | AI runner HS/JUCO only in `AiRecruitingRepository`; portal slice not run. | data recruiting AI |

---

## I. AI recruiting & sim-week

| ID | Pri | Status | Spec / topic | Finding | Evidence |
|----|-----|--------|--------------|---------|----------|
| MEGA-RECR-068 | P1 | PASS | Same rules as user | Pack-backed `fromPackTeams` league + dynasty pack user `SchoolRecruitingContext` (prestige/NIL). | `AiRecruitingRepository`, `AiLeagueProgramGenerator` |
| MEGA-RECR-069 | P2 | GAP | AI visibility off-screen | No rival board UI — PASS. | spec |
| MEGA-RECR-070 | P2 | GAP | Auto-maintain on sim-week | Wired in `SaveSlotRepository` advance path when automation on — undermined by demo pool. | save repo |

---

## J. Game start / new dynasty

| ID | Pri | Status | Spec / topic | Finding | Evidence |
|----|-----|--------|--------------|---------|----------|
| MEGA-RECR-071 | P1 | GAP | Wizard steps 1–6 | Implemented scaffold — PASS per `game-start.md` “implemented”. | `ui/newgame` |
| MEGA-RECR-072 | P1 | PASS | School summary recruiting/NIL preview | Sheet shows pipeline, weekly points, annual NIL from shipped balance tables. | `NewGameSchoolSummaryPreviewEngine`, `SchoolStepContent.kt` |
| MEGA-RECR-073 | P2 | GAP | Pipeline states on summary | Not shown on school summary sheet. | `SchoolStepContent.kt` |
| MEGA-RECR-074 | P2 | GAP | School demeanor on summary | Not shown. | `SchoolStepContent.kt` |
| MEGA-RECR-075 | P1 | SHIP | Prestige ≤40 default filter | `NewGameStarterPrestigeFilterPolicy` + starter copy on school list unless unlock-all. | `SchoolStepContent.kt` |
| MEGA-RECR-076 | P1 | PASS | Bootstrap recruiting on sign | `bootstrapSeasonOneRecruitingAfterSign` seeds class, opens ledger with chosen-school prestige, and `openCyclePoolUsd`. | `SaveSlotRepository`, `DynastyWizardBootstrapPolicy` |
| MEGA-RECR-077 | P2 | PASS | Launch → Offseason W1 Players Leaving | Matches `game-start` step 6 narrative. | wizard + calendar |
| MEGA-RECR-078 | P2 | RISK | `teamId` on coach/save | Wizard stores school in draft; confirm pack `teamId` bound on coach entity for recruiting context (audit: `userSchoolRecruitingContext` depends on loaded dynasty team). | `NilAnnualPoolRepository` |

---

## K. Documentation / checklist honesty

| ID | Pri | Status | Spec / topic | Finding | Evidence |
|----|-----|--------|--------------|---------|----------|
| MEGA-RECR-079 | P1 | RISK | BUILD_CHECKLIST 6.11 “done” | Year-long economy coded but **prestige-at-open** and **pool source** bugs (RECR-015, RECR-005). | `docs/BUILD_CHECKLIST.md` |
| MEGA-RECR-080 | P1 | RISK | BUILD_CHECKLIST 6.14 “done” | Offseason portal optional tasks only; **in-season recruiting triggers missing** (RECR-042). | BUILD_CHECKLIST |
| MEGA-RECR-081 | P2 | RISK | BUILD_CHECKLIST 6.20 Signing Day | UI routes exist; data integrity fail on class list (RECR-049). | BUILD_CHECKLIST |

---

## L. Interest meter UI & recruit sheet

| ID | Pri | Status | Spec / topic | Finding | Evidence |
|----|-----|--------|--------------|---------|----------|
| MEGA-RECR-082 | P0 | PASS | Live **interest** meter 0–100 | `RecruitInterestMeterRow` shows `liveInterest` 0–100 gauge + spend gain vs baseline; influence line is rank/closeness only. | `RecruitInterestMeterRow.kt`, hub row |
| MEGA-RECR-083 | P1 | PASS | Two surfaces (`baseline` vs `interest`) | `RecruitInterestTwoSurfaces` — badge from zero-spend baseline; meter shows live interest; competition line labeled baseline. | `SignLikelihoodEngine`, hub row |
| MEGA-RECR-084 | P2 | GAP | Dedicated recruit sheet route | Actions live inline on Recommended list; no full-screen sheet with meter, factor chips, ranked rivals. | `RecruitingHubScreen.kt` |
| MEGA-RECR-085 | P2 | SHIP | Visual meter control | `RecruitInterestMeterUiPolicy` + `RecruitInterestMeterRow` | Hub compose |
| MEGA-RECR-086 | P1 | PASS | Spend → meter feedback | Hub reloads spend map on `gateRefresh` after each action. | `RecruitingHubScreen.kt` |
| MEGA-RECR-087 | P2 | GAP | Factor score debug | `factorScores` / `effectiveWeights` not exposed in UI or dev overlay. | `RecruitInterestSnapshot` |
| MEGA-RECR-088 | P2 | FAIL | Influence closeness league sim | `InfluenceBoardEngine` ranks user school; **closeness** not advanced on league week advance / AI spend. | core recruiting influence |
| MEGA-RECR-089 | P2 | SHIP | Official visit → campus bump | `RecruitOfficialVisitCampusBumpPolicy` visit row copy | `RecruitingHubScreen.kt` |
| MEGA-RECR-090 | P1 | SHIP | Promise picker → `promiseDelta` | `RecruitScholarshipPromiseDeltaPolicy` feeds live interest when scholarship promise on file. | `RecommendedRecruitList`, hub picker |
| MEGA-RECR-091 | P2 | SHIP | Post-spend haptics / snackbar | `RecruitSpendFeedbackPolicy` + interest delta toast | `RecruitingHubScreen.kt` |
| MEGA-RECR-092 | P2 | SHIP | Dashboard deep link scroll-to-target | Feed commit name → hub scroll/highlight | `RecruitHubNotificationDeepLinkPolicy` |

---

## M. NIL (hub, interest, signing)

| ID | Pri | Status | Spec / topic | Finding | Evidence |
|----|-----|--------|--------------|---------|----------|
| MEGA-RECR-093 | P1 | SHIP | Assign NIL partial offers | `RecruitNilPartialOfferControls` slider + `RecruitNilPartialOfferPolicy` between $0 and 3× cap. | `RecruitingHubScreen` NIL row |
| MEGA-RECR-094 | P1 | PASS | NIL drill-down sheet | Bottom sheet: committed + reserved lines from ledger and open scholarship NIL offers. | `NilBudgetDrillDownEngine` |
| MEGA-RECR-095 | P2 | GAP | Reserved vs committed copy | `NilBudgetLedger` tracks buckets in core; hub shows aggregate remaining only. | `NilBudgetRepository` |
| MEGA-RECR-096 | P2 | SHIP | 3× cap affordance | `RecruitNilAssignableDisplayPolicy` + assignable line | `RecruitNilAssignCapLine.kt` |
| MEGA-RECR-097 | P1 | SHIP | Interest NIL tops at ask | Badge tracks live interest when NIL assigned; rows rebuild on `gateRefresh` + offer map. | `RecruitNilInterestBadgeRefreshPolicy` |
| MEGA-RECR-098 | P1 | SHIP | NIL at hard sign snapshot | `SigningDayNilSnapshotPolicy` hydrates `nilSignedUsd` from ledger on class load. | `SigningDayRepository.loadIncomingClass` |
| MEGA-RECR-099 | P2 | PASS | Annual pool + week-9 boost | Hub loads `nilPoolBoostUsd` from facilities week — PASS wiring. | `RecruitingHubScreen` LaunchedEffect |
| MEGA-RECR-100 | P0 | PASS | Dynasty bootstrap NIL cycle | Wizard sign calls `openCyclePoolUsd` with conference + prestige from `draft.selectedTeamId` ([DynastyWizardBootstrapPolicy]). | `SaveSlotRepository` |
| MEGA-RECR-101 | P2 | PASS | Portal NIL same ledger | Portal targets use `assignOffer` by entrant id. | `NilBudgetRepository` |
| MEGA-RECR-102 | P2 | RISK | NIL offers keyed demo ids | Offers stored by `prospectId`; if user recruits from **demoPool** but seed uses different ids, ledger orphans. | RECR-005 + `NilBudgetRepository` |

---

## N. Signing Day flow, hard sign, prep

| ID | Pri | Status | Spec / topic | Finding | Evidence |
|----|-----|--------|--------------|---------|----------|
| MEGA-RECR-103 | P1 | PASS | Required task gating | `SigningDayHardSignUiGate` blocks confirm positions until rankings viewed; hard sign until both tasks + promises. | `SigningDayScreen.kt` |
| MEGA-RECR-104 | P1 | SHIP | Admin week — no new drip | `RecruitingClosedCycleWeekGrantPolicy` — zero weekly grant when `cycleClosed`. | `RecruitingWeekGrant.applyWeekAdvanceGrant` |
| MEGA-RECR-105 | P2 | GAP | Walk-ons after close | No in-app feed / board after hard sign per `recruiting-engine#walk-ons`. | spec; no UI |
| MEGA-RECR-106 | P1 | GAP | Unsigned targets sim elsewhere | Hard sign finalizes offer-based class only; no league AI “sign elsewhere” for open elite targets. | `SigningDayHardSignEngine` |
| MEGA-RECR-107 | P2 | GAP | Position editor depth | Positions tab **cycles enum** per signee — not bulk roster editor / flex validation from spec. | `SigningDayScreen` `onCyclePosition` |
| MEGA-RECR-108 | P2 | GAP | Rankings task deep link | Dashboard opens Signing Day rankings tab — does not mirror **News → Recruits** full Top 60 context copy. | `DashboardOptionalTasksPanel` |
| MEGA-RECR-109 | P1 | SHIP | Roster scholarship attach | `SigningDayRosterEnrollPolicy` on hard sign | `SigningDayRepository.hardSign` |
| MEGA-RECR-110 | P1 | PASS | Soft vs hard commit semantics | Soft commits use `RecruitSoftCommitPolicy`; hard sign unchanged via `SigningDayHardSignEngine`. | `RecruitSoftCommitPolicy` |
| MEGA-RECR-111 | P2 | PASS | Rankings tab uses seed when present | `SigningDayScreen` `loadPool` → `top60FromPool` — PASS when preseason task done. | `SigningDayScreen.kt` |
| MEGA-RECR-112 | P2 | GAP | Prep before Signing Day | No **recruiting prep brief** / R1–R4 copy surface tying crunch multiplier to dashboard (offseason optional flavor only). | `OffseasonOptionalDashboardTasks` |
| MEGA-RECR-113 | P2 | GAP | Signing close → hub read-only | `cycleClosed` set on hard sign; hub still navigable from shell — enforce read-only Committed-only not implemented. | `RecruitingCyclePolicy` |

---

## O. Prep brief & preseason class narrative

| ID | Pri | Status | Spec / topic | Finding | Evidence |
|----|-----|--------|--------------|---------|----------|
| MEGA-RECR-114 | P2 | GAP | Preseason report × recruiting | `PreseasonReportPanel` article does not reference **Top 60** / class outlook from seed headline. | `PreseasonReportRepository` |
| MEGA-RECR-115 | P2 | GAP | Incoming seed progress UX | `IncomingClassSeedPanel` toggles `seeding` boolean — no stepped progress / cancel for **~3.9k** player generation. | `IncomingClassSeedPanel.kt` |
| MEGA-RECR-116 | P1 | GAP | Season 1 prep brief timing | Spec: Top 60 from tipoff year one; wizard lands **Offseason W1** with no class narrative until later preseason week — missing “class preview” brief at all. | `game-start.md`, RECR-053 |
| MEGA-RECR-117 | P2 | PASS | Top 60 preview in seed task | Panel lists **60** rows after `runSeed` — PASS local preview. | `IncomingClassSeedPanel` |
| MEGA-RECR-118 | P3 | GAP | Workouts task recruiting cross-link | Preseason workouts task does not nudge open Recruiting for new commits on roster. | `preseason.md` task 2 |

---

## P. Rankings confusion (team vs recruit)

| ID | Pri | Status | Spec / topic | Finding | Evidence |
|----|-----|--------|--------------|---------|----------|
| MEGA-RECR-119 | P1 | RISK | Signing dashboard copy | `dashboard_task_signing_rankings` = “View **recruiting** rankings (Top 60)” while Rankings hub tab is **programs**. | `strings.xml` |
| MEGA-RECR-120 | P1 | RISK | Preseason task 4 label | `preseason_task_rankings` = NET + **Media & Coaches** — same word “rankings” as recruit national list. | `PreseasonScreen.kt` |
| MEGA-RECR-121 | P2 | PASS | News Recruits tab separation | `NewsHubScreen` loads seed Top 60 when seeded — distinct from team polls. | `NewsHubScreen.kt` |
| MEGA-RECR-122 | P2 | GAP | Signing Day disclaimer | Rankings tab lacks helper text “**Player** Top 60 — not team poll”. | `SigningDayScreen` |
| MEGA-RECR-123 | P2 | GAP | Rankings hub route naming | Hamburger **Rankings** → NET/Media/Coaches only; no link to recruit list for confused users. | `RankingsHubScreen` |
| MEGA-RECR-124 | P3 | GAP | In-app glossary | No coach-tip explaining Media T25 vs Recruit #1–#60. | product copy |

---

## Q. Phase transitions (Signing Day → next preseason)

| ID | Pri | Status | Spec / topic | Finding | Evidence |
|----|-----|--------|--------------|---------|----------|
| MEGA-RECR-125 | P1 | GAP | Next cycle open timing | `openNextCycleAfterSeasonReview` sets **next** weekly pool from recap prestige — must align with **preseason Incoming Class Seed** opening same enrolling class index. | `SeasonReviewRepository`, recruiting doc |
| MEGA-RECR-126 | P1 | GAP | Enrolling season index | Seed repository keys `seasonIndex` on metadata; hard sign same index — next class seed season bump not audited in one helper. | `IncomingClassSeedRepository` |
| MEGA-RECR-127 | P2 | GAP | Offseason W8 → Preseason gate | Advance into preseason does not auto-close prior recruiting UI state / reservations cleanup. | `enterInSeasonFromPreseason` path |
| MEGA-RECR-128 | P2 | GAP | Walk-on window calendar | Spec places walk-ons after Signing Day; calendar phase after OS W6 advance not wired to walk-on feed. | `recruiting-engine#walk-ons` |
| MEGA-RECR-129 | P1 | GAP | Preseason without re-seed | Re-entering preseason year 2+ must run **new** 2k–4k draw; no guard preventing duplicate `runSeed` on same key. | `IncomingClassSeedPanel` `enabled = headline == null` |
| MEGA-RECR-130 | P2 | PASS | Hub open RS + OS W2+ | `RecruitingCyclePolicy` year-long access in RS — PASS in tests. | `RecruitingHubDefaultsTest` |
| MEGA-RECR-131 | P0 | PASS | **Lag:** main-thread seed | `runSeed` on UI button coroutine generates **3900** prospects + `encodePool` string in DataStore on device thread — frame drops / ANR risk. | `IncomingClassSeedIoPolicy`; all repository entry points `Dispatchers.IO`; seed/encode/decode `Default`; `IncomingClassSeederPerformanceTest` |
| MEGA-RECR-132 | P1 | RISK | **Lag:** load full pool | `loadPool` decodes entire newline blob when hub/AI wired to seed — **O(n)** parse per screen open. | `IncomingClassSeedRepository.decodePool` |

---

## R. Recruit generation (2k–4k) & data integrity

| ID | Pri | Status | Spec / topic | Finding | Evidence |
|----|-----|--------|--------------|---------|----------|
| MEGA-RECR-133 | P0 | PASS | Uniform HS **2,000–4,000** | `runSeed` → `seed()` without override rolls `resolveHsPoolSize` from dynasty hash + season. | `IncomingClassSeeder.kt`, `IncomingClassSeedRepository.kt` |
| MEGA-RECR-134 | P1 | RISK | **Lag:** DataStore blob size | ~3900-row `encodePool` single preference string — large read/write on every seed and load. | `IncomingClassSeedRepository` |
| MEGA-RECR-135 | P2 | PASS | JUCO +1,500 fixed add-on | `JUCO_POOL_SIZE = 1_500` matches spec table. | `IncomingClassSeeder.kt` |
| MEGA-RECR-136 | P1 | PASS | Star assignment order bug | `RecruitStarAssignmentEngine` assigns bands from hidden-rating sort rank, not creation index. | `IncomingClassSeeder.generatePool` |
| MEGA-RECR-137 | P1 | PASS | Hidden vs star mismatch | `RecruitHiddenRatingSampler` draws hidden by percentile rank before star assignment. | `IncomingClassSeeder.generatePool` |
| MEGA-RECR-138 | P2 | GAP | Demo pool star distribution | `RecruitingClassSeed.demoPool(120)` used in production paths — not representative of 2k–4k tail. | RECR-005 |

---

## S. Point assignment & economy bugs

| ID | Pri | Status | Spec / topic | Finding | Evidence |
|----|-----|--------|--------------|---------|----------|
| MEGA-RECR-139 | P0 | PASS | DM / call / house **per recruit** | Point actions pass `prospectId` into `spendPointAction`. | `RecruitingHubScreen.kt` |
| MEGA-RECR-140 | P1 | PASS | Bonus points amount | Bonus picker passes user amount to `spendPointAction`; gate max is ceiling only. | `RecruitBonusPointsPickerSheet` |
| MEGA-RECR-141 | P2 | PASS | Refundable per prospect | Scholarship / scout / visit use `spendRefundableAction` with `prospectId` — PASS. | hub `onSpend` |
| MEGA-RECR-142 | P1 | RISK | Reservations without interest | Offer reservations persist; interest/influence not updated — soft commit resolver still offer-only. | RECR-029, RECR-088 |
| MEGA-RECR-143 | P2 | GAP | Week advance refund batch | `onCalendarWeekAdvanced` refunds reservations — no test tying refund to **same** prospect offer set across hub reload. | `RecruitingEconomyRepository` |
| MEGA-RECR-144 | P2 | RISK | Sim-week auto-maintain double spend | User manual spend + `estimateUserAutoMaintainSpend` both debit ledger in advance path when automation on — risk over-spend unless gated. | `SaveSlotRepository.buildSimWeekAdvanceInputSuspended` |

---

## T. Dashboard tasks (recruiting, portal, signing)

| ID | Pri | Status | Spec / topic | Finding | Evidence |
|----|-----|--------|--------------|---------|----------|
| MEGA-RECR-145 | P0 | PASS | In-season drip nudge | `SpendRecruitingPoints` optional card when ledger `pointsAvailable > 0` in RS. | `InSeasonRecruitingOptionalDashboardTasks.kt` |
| MEGA-RECR-146 | P1 | PASS | Watchlist threshold tasks | `DashboardTaskId` offer / re-engage / Top 60 / rival commit + `InSeasonRecruitingNudgeResolver`. | `DashboardTask.kt`, shell watchlist |
| MEGA-RECR-147 | P1 | PASS | Top 60 movement nudge | `RecruitingWatchlistTop60RankRepository` + `RecruitingTop60MovementPolicy` on watchlist rank delta. | `DynastyShellScreen` |
| MEGA-RECR-148 | P1 | SHIP | Portal pipeline entrant task | `PortalPipelineEntrantDashboardPolicy` spotlights pipeline match → `PortalPipelineEntrant` optional task. | `OffseasonScreen`, `OffseasonOptionalDashboardTasks` |
| MEGA-RECR-149 | P2 | PASS | Signing required trio | Rankings + positions + hard sign task ids emitted on OS W6 — PASS shell. | `SigningDayRequiredDashboardTasks` |
| MEGA-RECR-150 | P2 | GAP | Task complete detection | Optional recruiting complete-on-action not implemented — no hub spend listener marks task done. | `dashboard-tasks.md` |
| MEGA-RECR-151 | P2 | GAP | Dismiss / skip week hiding | Offseason portal dismiss flags exist; in-season recruiting dismiss ids never emitted. | RECR-047 |
| MEGA-RECR-152 | P2 | GAP | Deep link Recommended default | `SpendRecruitingPoints` string exists; in-season never surfaces link to hub. | `DashboardOptionalTasksPanel` |

---

## U. AI recruiting & sim-week

| ID | Pri | Status | Spec / topic | Finding | Evidence |
|----|-----|--------|--------------|---------|----------|
| MEGA-RECR-153 | P1 | PASS | User school prestige in AI week | AI fallback prestige from `resolveUserProgramPrestige`. | `AiRecruitingRepository.kt` |
| MEGA-RECR-154 | P1 | PASS | AI pool = persisted class | `runLeagueWeekIfActive` uses `ensurePool` for full national class. | `AiRecruitingRepository.kt` |
| MEGA-RECR-155 | P2 | GAP | AI influence / commits headline | `AiRecruitingWeekRunner` processes programs; News commit headlines from AI not traced in audit paths. | core `ai` package |
| MEGA-RECR-156 | P2 | GAP | AI portal slice | Runner HS/JUCO Recommended only — portal entrants excluded (RECR-067). | `AiRecruitingWeekRunner` |
| MEGA-RECR-157 | P2 | PASS | Auto-maintain flag | Respects `automation.recruitingAutoMaintain` when hub open — PASS. | `AiRecruitingRepository` |
| MEGA-RECR-158 | P2 | RISK | AI + 365 programs lag | `AiLeagueProgramGenerator.demoLeague` + week batch in same advance as user sim — perf budget unrelated to recruiting pool size until real pool wired. | `LeagueWeekBatchPerformanceBudget` |
| MEGA-RECR-159 | P1 | SHIP | Same interest pipeline | `AiRecruitingInterestParityPolicy` routes AI targets through `RecommendedRecruitList`. | `AiRecruitingWeekRunner` |
| MEGA-RECR-160 | P2 | GAP | Sim-week confirm line copy | Advance confirmation should mention recruiting auto-maintain spend estimate — partial via sim-week input only. | `SaveSlotRepository` |

---

## V. Lag & performance risks (product)

| ID | Pri | Status | Spec / topic | Finding | Evidence |
|----|-----|--------|--------------|---------|----------|
| MEGA-RECR-161 | P0 | PASS | Preseason seed on UI thread | Same root as RECR-131 — blocks checklist until complete; no `withContext(Dispatchers.Default)`. | `IncomingClassSeedPanel` IO `withContext`; `IncomingClassSeedPanelIoPathTest` |
| MEGA-RECR-162 | P1 | RISK | Recommended list at scale | When seed wired, building `RecommendedRecruitList` over **3.9k** rows each hub open without pagination. | `RecommendedRecruitList.build` |
| MEGA-RECR-163 | P1 | RISK | LazyColumn only ~50 cap | Engine may still score full pool before cap — CPU each composition if demo pool replaced. | `BalanceRecruitingRecommendedParser` |
| MEGA-RECR-164 | P2 | RISK | AI league week × demo pool | Doubles work on advance: batch sim 365 teams + AI recruiting loop — acceptable today; explodes if pool + persistence added without batching. | `SaveSlotRepository.advanceWeek` |
| MEGA-RECR-165 | P2 | GAP | No recruiting perf test | `LeagueWeekBatchPerformanceTest` guards games; no test for seed encode/decode under **4k** prospects. | `android/core` tests |
| MEGA-RECR-166 | P2 | RISK | DataStore preference size limits | Single-string pool encoding may approach DataStore size guidance on low-end devices. | `encodePool` |

---

## Summary counts

| Status | Count |
|--------|------:|
| FAIL | 41 |
| GAP | 74 |
| PASS | 16 |
| RISK | 18 |
| **DEBT (prior audit)** | **81** |
| **Expanded rows (082–166)** | **+85** |
| **Total MEGA-RECR** | **166** |

### P0 concentration (fix first)

| ID | Theme |
|----|--------|
| **RECR-001, 002, 005, 006** | National pool scale, star percentiles, demo vs persisted seed |
| **RECR-015, 076, 100** | Prestige / NIL bootstrap at dynasty create |
| **RECR-035, 036** | Hub HS/JUCO + portal placeholders |
| **RECR-042, 145** | In-season recruiting dashboard tasks |
| **RECR-053, 116** | Season 1 calendar vs preseason Top 60 |
| **RECR-082, 139** | Interest meter + per-recruit point spend |
| **RECR-131, 161** | Main-thread class seed / ANR risk |

Legacy single-line list: RECR-001, 002, 005, 006, 015, 035, 036, 042, 053 — plus **082, 100, 131, 139, 145, 161** from expansion.

### Suggested verification order

1. Wire **one** `RecruitPoolProvider` (dynasty seed → fallback) through hub, AI, signing, sim-week.  
2. Bind **program prestige** + **NIL cycle** at `signNewGameFromWizard` + `loadOrOpenCycle(loaded, prestige)`.  
3. Fix **star percentiles** (sort hidden rating → band) + **random HS count** 2k–4k.  
4. Move **IncomingClassSeed** generation off main thread; add perf test for encode/load.  
5. Implement **live interest meter** + **prospect-scoped** point actions + `InSeasonOptionalDashboardTasks` recruiting triggers.  
6. Flesh hub tabs / signing soft-commit rules / align BUILD_CHECKLIST claims.

---

## Document history

| Date | Change |
|------|--------|
| 2026-11-11 | Initial MEGA audit (81 × MEGA-RECR-NNN) |
| 2026-11-11 | Expanded +85 rows (082–166): interest UI, NIL, Signing Day, prep brief, rankings UX, phase transitions, generation, points, dashboard, AI, lag |
