# MEGA build checklist audit — sim · strategy · lineup · injuries

**Repo:** `/workspace`  
**Audited:** 2026-11-11 (expanded pass)  
**Planning authority:** [`planning/simulation-engine.md`](../../planning/simulation-engine.md), [`planning/strategy-schemes.md`](../../planning/strategy-schemes.md), [`planning/positions-and-lineup.md`](../../planning/positions-and-lineup.md), [`planning/injuries.md`](../../planning/injuries.md)  
**Code focus:** `android/core/src/main/kotlin/dev/novux/dynasty/cbb/core/sim/**`, `core/strategy`, `core/lineup`, `core/injuries`, `ui/sim`, `ui/strategy`  
**Method:** Full planning read + static trace vs possession engine, watch UI, Strategy hub, injury adjusters; **no** claim that thin sim is “complete product.”

## Status legend (sections A–F, original pass)

| Tag | Meaning |
|-----|---------|
| **SHIP** | Behavior or surface matches spec; covered by tests or wired in prod path |
| **PARTIAL** | Simplified or incomplete vs spec; usable stub |
| **GAP** | Spec requirement not implemented |
| **WIRE** | Core logic exists; dynasty UI, persistence, or batch path missing |

## Status legend (sections G–P, expanded product pass)

| Tag | Meaning |
|-----|---------|
| **FAIL** | Spec violation or missing mandatory product behavior |
| **GAP** | Partial stub; not shippable alone |
| **PASS** | Matches planning for this row |
| **RISK** | Works in narrow path; breaks on real dynasty flow |
| **WIRE** | Library or DB ready; UI or caller missing |

**Pri:** P0 = ship-blocker for credible game sim / Strategy loop; P1 = high; P2 = medium; P3 = polish.

**Product bar (user complaint: “simulations bare minimum and boring”):** Sections **G–P** are **full-product** requirements from planning — nothing deferred to “phase 2.” Implementation today is overwhelmingly **FAIL/GAP** on depth, narrative, box score, rotation, clutch, and injury-in-game.

---

## A — Possession engine & output (`:core/sim`)

| ID | Status | Requirement (spec) | Code / test | Finding |
|----|--------|-------------------|-------------|---------|
| MEGA-SIM-001 | SHIP | `simulateGame(input) → result`; stateless engine, caller owns calendar/save | `PossessionSimEngine.simulate`, `GameSimInput` | Contract present; name is `simulate` not `simulateGame` |
| MEGA-SIM-002 | SHIP | Regulation **40:00**, two halves **20:00** each | `GameSimClock`, `PossessionSimSession` | Clock starts 2400s, decrements per trip |
| MEGA-SIM-003 | PARTIAL | **30s** shot clock state + reset on change of possession / **20** after OReb | `SimTuning.shotClock`, `GameSimClock.MAX_TRIP_SECONDS` | JSON defines 30/20; engine caps trip at 30s only — no shot-clock state machine or OReb reset |
| MEGA-SIM-004 | SHIP | Shot-clock **violation** turnover type | `PossessionShotClockViolationPolicy` in possession loop | `PossessionSimSession.kt` |
| MEGA-SIM-005 | PARTIAL | Trip time bands: quick TO 3–6, quick shot 8–14, foul 12–18 | `SimTuning.timeUsedSeconds`, `sampleTripSeconds` | Bands loaded; pacing also scaled via `pacedTripSeconds` |
| MEGA-SIM-006 | GAP | Late-clock (22–30s) contested shot / TO / foul bumps from tuning | — | No late-clock branch keyed on `used` |
| MEGA-SIM-007 | PASS | **Overtime** 5:00 until winner | `GameSimOvertime`, `PossessionSimSession` | OT periods until score breaks tie |
| MEGA-SIM-008 | PARTIAL | **~60–70** possessions per team | `PossessionPaceGovernor`, paced trips | Trip pace scales when ahead/behind expected count |
| MEGA-SIM-009 | PARTIAL | Terminal events: TO, 2PT/3PT, FT chains, rebounds, blocks, steals text | `resolvePossessionForSession` | Only coarse TO / FGA / score; no FTs, rebounds, blocks, steals in box |
| MEGA-SIM-010 | GAP | Possession log format `14:32 \| HOME \| #12 3PT miss \| …` | `formatPossessionLog` | Generic `clock \| side \| Ns trip \| TO/+PTS/miss` — no jersey numbers |
| MEGA-SIM-011 | SHIP | Running score + clock in output | `GameSimResult`, watch UI score strings | Score lines updated in `GameWatchScreen` |
| MEGA-SIM-012 | PASS | Full **player** box: MIN, PTS, FGM/A, … | `PlayerBoxScore`, `GameSimResult.homePlayerBox` | All standard columns + `BoxScoreCoherence` player sums |
| MEGA-SIM-013 | SHIP | Team PTS = sum semantics / FGA bounds | `BoxScoreCoherence.assertValid` | Asserts scoreboard vs team box, FGM≤FGA, 3PM≤3PA |
| MEGA-SIM-014 | GAP | **200** player-minutes per team regulation coherence | `RotationMinutesAllocator` in prep only | Prep targets 200; sim does not accumulate MIN or validate |
| MEGA-SIM-015 | PASS | Release retry on coherence fail (cap N rolls) | `BoxScoreCoherenceSim` | Seed-bump retry before assert |
| MEGA-SIM-016 | GAP | **Personal fouls**, disqualification at 5, foul-trouble pull | — | Not modeled in possession loop |
| MEGA-SIM-017 | GAP | In-game **substitution** from rotation bands / fatigue | `RotationMinutesAllocator` (pre-game only) | Five-on-floor not rotated during sim |
| MEGA-SIM-018 | GAP | Mid-game **injury** DNP from injury minute onward | `injuries.md` sim hook | Injuries only `GameSimInjuryRoll` post-game |
| MEGA-SIM-019 | SHIP | `GameSimResult` includes `possessionLog`, `isComplete`, `midGame` | `GameSimModels.kt` | Present |
| MEGA-SIM-020 | PASS | `periodsPlayed` (regulation + OT count) | `GameSimResult.periodsPlayed` | `1 + overtimePeriods` |
| MEGA-SIM-021 | PASS | Participant fields: `height`, `weight`, `shooting` struct | `GameSimParticipant`, `GameSimShootingProfile`, `FloorShootingEffects` | Wired from roster; shooting nudges possession resolution |
| MEGA-SIM-022 | PASS | Slot-played ratings drive outcomes (84 PG at SG > 72 SG) | `SlotPlayedFloorRating`, `PositionComfortNudge` | Per-possession floor rating by slot played |
| MEGA-SIM-023 | PASS | Optional position-comfort nudge without flipping OVR order | `SimPositionComfortModifiers`, `PositionComfortNudge` | `SimTuning.positionComfort` consumed on floor ratings |
| MEGA-SIM-024 | SHIP | Home/venue modifiers when caller supplies | `SimVenueModifiers`, `SimVenueRatingPolicy`, `PossessionSimSession` | Applied to offense/defense ratings; late-home crowd +1 in final 2:00 |
| MEGA-SIM-025 | SHIP | Tuning bundle from `assets/sim/sim-tuning.json` | `SimTuningCodec`, `SimTuningAssetTest` | ADR-003 path wired in app |
| MEGA-SIM-026 | SHIP | Same engine for watch vs batch (`simToEnd`) | `GameWatchPlayback.simToEnd` | Identical `PossessionSimEngine.simulate` |
| MEGA-SIM-027 | SHIP | Deterministic variance via `seed` on `GameSimInput` | `PossessionSimSession` Random(seed) | Halftime resume preserves RNG state via `MidGameState` |
| MEGA-SIM-028 | GAP | `winSharesSeason` rollup | Dynasty EoS (`simulation-engine` §season win contribution) | Correctly **not** in sim module |

---

## B — Playback & halftime UI

| ID | Status | Requirement (spec) | Code / test | Finding |
|----|--------|-------------------|-------------|---------|
| MEGA-SIM-029 | PASS | Default **2s** wall-clock between possession lines | `GameWatchPlayback`, `GameWatchScreen` | `DEFAULT_WATCH_SPEED_MULTIPLIER` 1×; `lineDelayMs` test |
| MEGA-SIM-030 | PASS | User **speed multiplier** control (0.5×, 2×, 4×) on watch screen | `GameWatchScreen`, `GameWatchSpeedPresets` | FilterChips 0.5–4× + Sim to end; live `onLineDelayMs` |
| MEGA-SIM-031 | SHIP | **Sim to end** completes full loop | `GameWatchPlayback.simToEnd` | Used by batch fast path when tuning passed |
| MEGA-SIM-032 | SHIP | Halftime pause on watched run; merge updated prep; resume second half | `GameWatchPlayback.watch`, `GameSimHalftimeTest` | MidGame + `homeHalftimeGameplan` |
| MEGA-SIM-033 | PASS | Halftime: change O/D scheme, rotation, usage, swap starters | `HalftimeCoachSheet`, `HalftimeRosterAdjust` | Full sheet: schemes, rotation chips, usage, legal starter swaps |
| MEGA-SIM-034 | SHIP | First-half stats/score fixed after halftime | `MidGameState` carry box scores | Scores accumulate from mid state |
| MEGA-SIM-035 | PARTIAL | `activeHomeGameplan(clock)` uses halftime plan in 2nd half | `GameSimInput.activeHomeGameplan` | `clock <= HALF_SECONDS` selects halftime plan (correct for countdown clock) |
| MEGA-SIM-036 | WIRE | Strategy **halftime preset** separate from pregame | `StrategyGameplanPanel` Set/Clear halftime | Stored in `halftimeGameplan` but watch passes `homeHalftimeGameplan` only when user sets preset — scrimmage button uses `halftimeGameplan ?: gameplan` as **pregame** only on `GameWatchLaunch` |
| MEGA-SIM-037 | PASS | In-sim shot clock display on lines (optional product) | `GameWatchScreen`, possession log `SC` | Chrome + per-line SC badge |
| MEGA-SIM-038 | PASS | Postgame report after sim | `PostgameBoxScorePanel`, `PostgameReportGenerator` | Box score tabs + narrative article |

---

## C — Strategy & schemes (`:core/strategy` + UI)

| ID | Status | Requirement (spec) | Code / test | Finding |
|----|--------|-------------------|-------------|---------|
| MEGA-SIM-039 | SHIP | **7** offensive presets | `OffensiveSchemePreset` enum | Matches planning table |
| MEGA-SIM-040 | SHIP | **7** defensive presets | `DefensiveSchemePreset` enum | Matches planning table |
| MEGA-SIM-041 | SHIP | Scheme modifiers affect 3PA rate, TO, pace, make bias | `SchemeModifierCatalog`, `SchemeMatchupResolverTest` | Wired in `PossessionSimSession` |
| MEGA-SIM-042 | SHIP | O/D interaction nudges (sample matchups) | `SchemeMatchupResolver.interactionNudge` | Partial 7×7 matrix — not exhaustive |
| MEGA-SIM-043 | SHIP | No separate pace slider — pace from offensive preset + interaction | `ResolvedSchemeEffects.tripPaceScale` | Aligns with spec |
| MEGA-SIM-044 | PASS | Per-game offensive/defensive scheme on `GameSimTeamInput` | `StrategyGameSchemePanel`, `GameWatchLaunch` | User picks O/D on Strategy; wired into watch `GameSimInput` |
| MEGA-SIM-045 | SHIP | Coach default O/D on new dynasty | `NewGameWizardScreen`, `SchoolStaffRepository` | Wizard + coach card — not per-game override |
| MEGA-SIM-046 | SHIP | Usage weights in gameplan | `GameplanInput.usageWeights`, `StrategyGameplanPanel` | Stepper per starter |
| MEGA-SIM-047 | PARTIAL | Usage affects offense via normalized share | `GameplanEffectsResolver.offenseTweaks` | Peak usage only — not per-player on possession |
| MEGA-SIM-048 | SHIP | **Deny primary scorer** toggle | Strategy panel + halftime dialog; test `denyPrimaryProducesMoreTurnovers` | Deny + primary slot chips in `StrategyDefensiveAssignmentPanel` |
| MEGA-SIM-049 | SHIP | **Late-game foul** strategy Standard / Foul early / Protect | `LateGameFoulStrategy`, defense tweaks last 2:00 | Chips in strategy + halftime |
| MEGA-SIM-050 | PASS | **Primary defensive assignment** map PG–C → defender | `GameplanInput.defensiveAssignments`, `DefensiveAssignmentPolicy`, sim deny bonus | Strategy panel + `GameplanEffectsResolver` when deny + assignment on primary slot |
| MEGA-SIM-051 | PASS | Strategy edits blocked after week advance | `StrategyEditLockPolicy`, `StrategyScreen` | Locked when no schedulable game or active week not past `committedThrough` |
| MEGA-SIM-052 | PARTIAL | Game preview win % / predicted score from ratings + RPI | `GamePreviewScreen` | Exists; not re-audited here — sim path separate |
| MEGA-SIM-053 | WIRE | Halftime gameplan persisted on dynasty save for real games | `GameWatchLaunch` globals | Scrimmage-only launch params; schedule games need save fields |

---

## D — Lineup & rotation (`:core/lineup`, `GameLineupPrep`)

| ID | Status | Requirement (spec) | Code / test | Finding |
|----|--------|-------------------|-------------|---------|
| MEGA-SIM-054 | SHIP | Listed positions PG/SG/SF/PF/C | `PlayerPosition` | — |
| MEGA-SIM-055 | SHIP | Slot eligibility interchange rules | `LineupSlotPolicy.slotEligible` | Matches `slotEligible()` in planning doc |
| MEGA-SIM-056 | SHIP | Default starters: best **listed** OVR per slot; tie playerId | `DefaultStarterAlgorithm.pickStarters` | Does not auto PG→SG for higher OVR |
| MEGA-SIM-057 | SHIP | Manual interchange in prep when user starters passed | `GameLineupPrepTest.userInterchangeHonorsSlotPolicy` | — |
| MEGA-SIM-058 | WIRE | User override from Strategy UI drag / pick | `positions-and-lineup.md` §user override | **StrategyScreen** read-only cards; does not pass `starterSlotWire` map into `GameLineupPrep.build` |
| MEGA-SIM-059 | SHIP | Injured / medical RS ineligible for lineup | `RotationMinutesAllocator.isLineupEligible`, `DefaultStarterAlgorithm` | Filters `injuryActive` + weeks |
| MEGA-SIM-060 | PARTIAL | Empty slot unfilled + UI prompt | `StrategyScreen` `strategy_slot_empty` | Shows empty; no assign flow |
| MEGA-SIM-061 | SHIP | Rotation minutes bands → ~200 team minutes | `RotationMinutesAllocator`, `GameLineupPrepTest` | Starters 32 + bench grants |
| MEGA-SIM-062 | GAP | Bench sub logic during sim from minutes bands | `simulation-engine` §rotation | Minutes are prep weights only |
| MEGA-SIM-063 | PARTIAL | Reserves list / depth order in Strategy UI | `StrategyScreen` bench cards | Display only |
| MEGA-SIM-064 | WIRE | Persist user lineup across sessions | `DynastyRosterPlayerEntity.starterSlotWire` | DB column exists; Strategy does not edit |
| MEGA-SIM-065 | SHIP | Stamina gate for default starters | `DefaultStarterAlgorithm` `starterBlockStamina` | Strategy passes fatigue tuning threshold |

---

## E — Injuries (`:core/injuries`, sim hook, UI)

| ID | Status | Requirement (spec) | Code / test | Finding |
|----|--------|-------------------|-------------|---------|
| MEGA-SIM-066 | SHIP | Post-game roll for players with minutes; 0–2 cap per team | `InjuryOccurrenceEngine`, balance `maxInjuriesPerTeamPerGame` | — |
| MEGA-SIM-067 | SHIP | Phase multipliers (exhibition lower; no OS/preseason) | `InjuryOccurrenceEngine.rollAfterGame` | — |
| MEGA-SIM-068 | SHIP | Bank catalog from `balance-2026.json` injuries.bank | `InjuryOccurrenceEngine.bankFromBalance` | App loads 40-row bank in watch path |
| MEGA-SIM-069 | SHIP | Medical staff **−1 week** on minor | `InjuryOccurrenceEngine` | When grade ≥ baseline |
| MEGA-SIM-070 | PASS | Minutes weight for injury probability | `GameSimInjuryRoll.floorMinutesFromResult` | Uses floor ledger seconds when present |
| MEGA-SIM-071 | SHIP | `GameSimResult` home/away injury lists | `GameSimModels`, `GameSimInjuryRoll.attach` | Only when `isComplete` |
| MEGA-SIM-072 | SHIP | Persist + `applyInjuryLineupAdjust` after user scrimmage | `DynastyApp` `onSimComplete`, `InjuryLineupAdjust` | Scrimmage path applies injuries |
| MEGA-SIM-073 | SHIP | Auto-fill starters on injury (listed-position algorithm) | `InjuryLineupAdjustTest` | Snapshots `injuryWasStarterSlotWire` |
| MEGA-SIM-074 | SHIP | Auto-restore on `weeksRemaining → 0` | `applyInjuryReturnLineupAdjust`, week advance repos | Tested in injuries package |
| MEGA-SIM-075 | PARTIAL | Strategy: injured greyed / cannot slot | `StrategyScreen` alpha + `PlayerInjuryRosterRow` | Visual only — no drag to block |
| MEGA-SIM-076 | PASS | Medical redshirt postgame sheet | `PostgameInjuryFlow`, `GameWatchScreen` | Apply/Dismiss after watch + shell refresh |
| MEGA-SIM-077 | PASS | Injury line in **live** possession log when mid-game injury exists | `MidGameInjurySampler.formatLogLine`, `PossessionSimSession` | `INJURY — {name} · {label}` appended to log |
| MEGA-SIM-078 | PASS | AI league batch injury rolls | `AiLeagueWeekOps`, `AiLeagueSyntheticRoster` | `InjuryOccurrenceEngine.rollAfterGame` per AI game in RS batch |
| MEGA-SIM-079 | PASS | Real RS game uses possession sim + injuries | `LeagueWeekBatchRepository`, `LeagueBatchSimParams` | RS batch passes `SimTuning` + prestige; AI possession sim + bank rolls |
| MEGA-SIM-080 | SHIP | Fatigue weight in injury probability | `FatigueEngine.fatigueInjuryWeight` | — |

---

## F — Cross-cutting integration gaps

| ID | Status | Requirement (spec) | Code / test | Finding |
|----|--------|-------------------|-------------|---------|
| MEGA-SIM-081 | PASS | User calendar game → Strategy prep → watch/sim commit | `GamePreviewScreen`, `UserCalendarGameWatchBootstrap`, `GameWatchLaunch` | Preview **Play game** wires prep/schemes/injuries; commits provisional mark |
| MEGA-SIM-082 | PASS | Batch week sim uses possession engine | `LeagueFastSim.simulateWithBatch`, `LeagueBatchSimParams` | RS repository always passes tuning; random stub only when batch null |
| MEGA-SIM-083 | PASS | `eventWeights` JSON consumed per possession | `SimEventWeightsResolver` | Trip buckets, rating-gap make/block, OReb, fouls from tuning |
| MEGA-SIM-084 | SHIP | `:core:test` possession / lineup / injury / watch tests | `android/core/src/test/.../sim/*` | Green in CI per BUILD_CHECKLIST 0.3 |
| MEGA-SIM-085 | PARTIAL | Tie games broken | `LeagueFastSim`, `GameSimOvertime` | Completed possession sim uses OT; +1 only for incomplete/stub paths |

---

## G — Possession depth & event chains (full product)

| ID | Pri | Status | Requirement (spec) | Code / test | Finding |
|----|-----|--------|-------------------|-------------|---------|
| MEGA-SIM-086 | P0 | PASS | Carry **shot-clock seconds remaining** per offensive trip; reset **30** on change, **20** after OReb | `PossessionSimSession.shotClockSeconds`, `PossessionShotClock` | Trip capped by shot clock; log `SC 30→22`; OReb retains offense + 20s reset |
| MEGA-SIM-087 | P0 | PASS | **Shot-clock violation** terminal event + AST/TO stat + log line | `resolveShotClockViolation`, `isShotClockViolation` | TO++ with `TurnoverTerminal.ShotClockViolation`; log `TO shot-clock` |
| MEGA-SIM-088 | P1 | PASS | **Late-clock** tuning (22–30s): contested make↓, TO↑, foul↑ | `PossessionLateClockPolicy` on `used` in session | Log `late-clock` tag |
| MEGA-SIM-089 | P1 | PASS | Sub-phases optional: drive → kick → shot within one trip | `PossessionSubPhaseEngine` | Log chain before terminal make/miss/TO |
| MEGA-SIM-090 | P0 | PASS | **Offensive rebound** → same team possession + 20s shot clock | `PossessionChange.OffenseRetainsOffensiveRebound` | `resolveMissRebound`; log `OREB +1`; SC 7→20 in log; `OffensiveReboundPossessionTest`; player `offensiveRebounds` |
| MEGA-SIM-091 | P1 | PASS | **Defensive rebound** change of possession in log text | `formatPossessionLog` | `miss | DREB | change possession` |
| MEGA-SIM-092 | P1 | PASS | **Steal / bad pass / travel / offensive foul** turnover flavors | `PossessionTurnoverFlavorPolicy` | Distinct `TurnoverTerminal` + log labels |
| MEGA-SIM-093 | P1 | PASS | **Block** on shot attempt + optional foul-on-block | `PossessionBlockPolicy` | `miss | BLK` + player block stat |
| MEGA-SIM-094 | P1 | PASS | **And-1** and shooting foul → FT chain (1–3 attempts) | `PossessionShootingFoulPolicy` | FTA/FTM on team box; log `and-1` / `shooting foul | FT n/m` |
| MEGA-SIM-095 | P1 | PASS | **Bonus / double bonus** team foul thresholds → automatic FT trips | `SimTeamFoulRules`, `TeamFoulBonusPolicy`, `TeamFoulHalfLedger` | Non-shooting fouls → FT when ≥7; double at 10; half/OT reset |
| MEGA-SIM-096 | P2 | FAIL | **Jump ball / held ball** rare alternate possession | Planning rare events | Not in engine |
| MEGA-SIM-097 | P1 | PASS | Possession log: **jersey # + player name** on scorer/assister/rebounder | `PossessionLogPlayerLabel`, `PossessionPlayerCredits` | Log `| #12 Name 2PT make (ast #5 …)` / `rebound #…` |
| MEGA-SIM-098 | P2 | FAIL | **Assists** attributed on made FGs from usage weights | — | No per-player stats |
| MEGA-SIM-099 | P1 | PASS | Consume **`eventWeights`** + rating-gap tables from `sim-tuning.json` | `SimEventWeightsResolver`, wired possession policies | Full tables in asset; session resolves per game |
| MEGA-SIM-100 | P1 | PASS | **Made basket at 0:00** half/game end tuning branch | `PossessionPeriodEndPolicy`, `periodEnd` tuning | Release-time make/FT counts or voided; log `0:00 release` |

---

## H — Fouls, rotation in sim, disqualification

| ID | Pri | Status | Requirement (spec) | Code / test | Finding |
|----|-----|--------|-------------------|-------------|---------|
| MEGA-SIM-101 | P0 | PASS | **Personal foul** counter per player; **5** = DQ | `PersonalFoulLedger`, foul-stoppage trips | Per-player map on `GameSimResult`; log `PF id (n)` / `DQ` |
| MEGA-SIM-102 | P0 | PASS | **Foul trouble** auto-sub below target minutes band | `FoulTroubleRotationPolicy`, on-floor weighted ratings | 3 PF 1H / 4 PF 2H bench; reserves enter weighted ORtg/DRtg |
| MEGA-SIM-103 | P0 | PASS | In-game **substitution** from rotation bands when floor MIN diverges from targets | `RotationBandFloorPolicy`, `SimFloorMinutesLedger` | Floor seconds vs target pace; band subs adjust on-floor ratings |
| MEGA-SIM-104 | P1 | PASS | **Fatigue** decay during sim affects sub priority | `SimStaminaLedger`, `SimFatigueRotationPolicy` | Floor time drains stamina; tired players pulled per `BalanceFatigue` |
| MEGA-SIM-105 | P1 | PASS | Track **team fouls** per half for bonus logic | `TeamFoulGameLedger`, `TeamFoulHalfSummary` on `GameSimResult` | Per-half seal at halftime; bonus uses live half count; log + mid-game resume |
| MEGA-SIM-106 | P1 | PASS | **Intentional foul** branch under late-game foul strategy | `IntentionalFoulLateGamePolicy`, `TripSample.intentionalFoul` | Foul-early decision → explicit foul trip (no FGA), bonus FTs + log |
| MEGA-SIM-107 | P2 | FAIL | **Technical foul** rare event (optional tuning) | — | — |
| MEGA-SIM-108 | P1 | PASS | Disqualified player **cannot re-enter**; log DQ line | `PersonalFoulDisqualificationPolicy`, DQ sets on result/mid-game | Banner log + floor filters; resume `*DisqualifiedPlayerIds` |
| MEGA-SIM-109 | P1 | PASS | **200** team player-minutes coherence in sim + assert | `TeamPlayerMinutesCoherence`, `BoxScoreCoherence` | Floor ledger → MIN; reconcile to 5×40:00 (+ OT); assert on complete games |
| MEGA-SIM-110 | P2 | FAIL | Starter re-insertion at **half** opening five from prep | `MidGameState` | No on-floor state at half |

---

## I — Clutch & situational gameplan

| ID | Pri | Status | Requirement (spec) | Code / test | Finding |
|----|-----|--------|-------------------|-------------|---------|
| MEGA-SIM-111 | P1 | PASS | **Score differential** late (e.g. last 4:00) affects shot selection / fouling | `ScoreDifferentialSituationPolicy`, `SimSituationWeights` | Trailing → more 3PA/TO; leading → pace protect; defense foul nudge; log tags |
| MEGA-SIM-112 | P0 | PASS | **Late-game foul strategy** changes possession decisions (foul to extend, protect) not only defense tweak scalar | `LateGamePossessionPolicy`, `sampleTripForLateGame` | Trailing foul-early → foul-stoppage trips; protect lead avoids foul stops |
| MEGA-SIM-113 | P1 | PASS | **Protect lead**: lower pace, avoid fouls on 3PT attempts tuning | `ProtectLeadLateGamePolicy`, `SimProtectLeadWeights` | Longer trips; swap foul-stop trips; cut 3PT shooting-foul rates |
| MEGA-SIM-114 | P1 | PASS | **Foul early** when trailing: hack-a / quick fouls on trip sample | `FoulEarlyLateGamePolicy`, `SimFoulEarlyWeights` | Tunable intentional + hack-a quick foul trips; clock-stop resolve + logs |
| MEGA-SIM-115 | P1 | PASS | **Clutch** usage bump to gameplan peak usage player | `ClutchUsagePolicy`, `SimClutchUsageWeights` | Last 4:00 close → peak `usageWeights`×mult for stat attribution |
| MEGA-SIM-116 | P2 | FAIL | **Timeout** not simulated (acceptable) but **last possession** log tag | Product clarity | No “CLUTCH” / score context on lines |
| MEGA-SIM-117 | P1 | PASS | **Deny primary** raises pressure on assigned defender slot with foul risk | `DenyPrimaryScorerPolicy`, `SimDenyPrimaryWeights` | Assigned slot turnover boost; deny foul on primary touch → targeted PF + logs |
| MEGA-SIM-118 | P2 | FAIL | Home **crowd** nudge in final 2:00 when venue modifiers applied | `SimVenueModifiers` unwired | Venue loaded, not applied in session |

---

## J — Watch mode & playback (full product)

| ID | Pri | Status | Requirement (spec) | Code / test | Finding |
|----|-----|--------|-------------------|-------------|---------|
| MEGA-SIM-119 | P0 | PASS | Default **2.0s** wall-clock between lines (not forced 8×) | `GameWatchScreen`, `GameWatchPlayback` | Watch uses `DEFAULT_WATCH_SPEED_MULTIPLIER` (1×) not 8× |
| MEGA-SIM-120 | P0 | PASS | User **speed multiplier** UI: 0.5×, 1×, 2×, 4×, **Sim to end** | `GameWatchScreen`, `GameWatchPlayback` | Speed chips + zero-delay “Sim to end” mid-watch |
| MEGA-SIM-121 | P1 | PASS | **Pause / resume** watch without losing mid-game state | `GameWatchPauseGate`, `GameWatchScreen` | Mid-half Pause/Resume holds session; resume matches `simToEnd` |
| MEGA-SIM-122 | P1 | PASS | **Half indicator** + period label on watch chrome | `GameWatchChrome`, `GameWatchScreen` | Period + game clock chrome row |
| MEGA-SIM-123 | P2 | PASS | Optional **shot-clock seconds** on each log line | `PossessionStep.chrome`, feed SC badge | Shot clock on log + line badge |
| MEGA-SIM-124 | P1 | PASS | **Possession feed** scrollback + auto-scroll | `GameWatchScreen` LazyColumn | Scrollback + follow-live / Back to live |
| MEGA-SIM-125 | P0 | PASS | **Halftime sheet**: change O/D scheme, rotation emphasis, usage, swap legal starters | `HalftimeCoachSheet`, `GameWatchScreen` | Resumes with updated schemes, roster, gameplan |
| MEGA-SIM-126 | P1 | PASS | Halftime **opponent scouting** read-only stats strip | `HalftimeScoutingSnapshot`, `HalftimeCoachSheet` | Opponent FG/TO/PF + ratings |
| MEGA-SIM-127 | P1 | PASS | **Same seed** watch vs sim-to-end parity test in UI contract | `GameWatchUiParityContractTest` | Multi-seed log + score parity |
| MEGA-SIM-128 | P2 | FAIL | **Exhibition** vs **RS** badge on watch header | Calendar phase | Not surfaced |

---

## K — Box score & postgame surfaces

| ID | Pri | Status | Requirement (spec) | Code / test | Finding |
|----|-----|--------|-------------------|-------------|---------|
| MEGA-SIM-129 | P0 | PASS | **Player box** all columns: MIN, PTS, FGM/A, 3PM/A, FTM/A, REB, AST, STL, BLK, TO, PF | `PlayerBoxScore`, `PlayerPossessionStatRecorder` | Per-possession attribution + `BoxScoreCoherence` player sums |
| MEGA-SIM-130 | P0 | PASS | Postgame **box score screen** (not article-only) with team + player tabs | `PostgameBoxScorePanel`, `GameWatchScreen` | Team cards + player stat table tabs after final |
| MEGA-SIM-131 | P1 | PASS | **Team totals** shooting %, turnovers, fouls on box UI | `TeamBoxScoreDisplay`, `PostgameBoxScorePanel` | FG/3P/FT%, OREB/DREB, TO/PF on team tab |
| MEGA-SIM-132 | P1 | PASS | **DNP-INJ** / partial MIN when mid-game injury | `PlayerBoxScoreMinutesLabel`, box UI | DNP-INJ when mid-game injury + 0 MIN |
| MEGA-SIM-133 | P1 | PASS | Export / persist box on dynasty **game record** for schedule replay | `ScrimmageGameRecord`, `ScrimmageGameRecordRepository` | Scrimmage watch saves `ProvisionalGameBoxArchive` |
| MEGA-SIM-134 | P2 | FAIL | **Plus-minus** optional column (product) | — | Not required by planning; nice for depth |
| MEGA-SIM-135 | P1 | PASS | Coherence **retry loop** on release (cap N) per spec | `BoxScoreCoherenceSim` | Up to 8 seed bumps before fail |
| MEGA-SIM-136 | P1 | PASS | **`periodsPlayed`** on `GameSimResult` + OT display | `GameSimResult`, watch final string | `periodsPlayed` / OT log lines |

---

## L — Rotation minutes UI (Strategy hub)

| ID | Pri | Status | Requirement (spec) | Code / test | Finding |
|----|-----|--------|-------------------|-------------|---------|
| MEGA-SIM-137 | P0 | PASS | Edit **minutes bands** per rotation player (steppers/sliders) | `StrategyRotationMinutesPanel`, `RotationMinutesBalancer` | ±1 steppers; 200-min rebalance; flows to `GameLineupPrep` |
| MEGA-SIM-138 | P0 | PASS | **200 minutes** budget meter with over/under warning | `RotationMinutesBudgetMeter`, `RotationMinutesBudget` | Linear meter + under/over copy; balance action |
| MEGA-SIM-139 | P1 | PASS | **Drag-to-slot** starter assignment with eligibility guard | `StrategyScreen` tap slot → bench | Tap assign + `LineupSlotPolicy` guard |
| MEGA-SIM-140 | P1 | PASS | **Pick from eligible list** per slot (PG…C) | `strategy_pick_starter`, cycle/pick | Empty slot pick + bench assign |
| MEGA-SIM-141 | P1 | PASS | Persist **`starterSlotWire`** from Strategy saves | `RosterStarterWireResolver`, `applyStarterLineup` | Save lineup → Room `starterSlotWire` |
| MEGA-SIM-142 | P2 | FAIL | **Reset to default** starters button invokes `DefaultStarterAlgorithm` | Strategy hub | Not exposed |
| MEGA-SIM-143 | P1 | PASS | **Halftime rotation emphasis** tweak (band nudge) | `HalftimeCoachSheet`, `HalftimeRosterAdjust`, `HalftimeRotationBandHint` | Rotation chips + minute-band summary; second-half target MIN nudge |
| MEGA-SIM-144 | P2 | FAIL | Show **target MIN** on player card in Strategy row | Prep fields | OVR only prominent |

---

## M — Scheme modifiers & gameplan depth

| ID | Pri | Status | Requirement (spec) | Code / test | Finding |
|----|-----|--------|-------------------|-------------|---------|
| MEGA-SIM-145 | P0 | PASS | Per-game **offensive + defensive scheme** pickers on Strategy tied to `GameSimTeamInput` | `StrategyGameSchemePanel`, `GameWatchScreen` | 7×7 chips → `GameWatchLaunch` → sim home team |
| MEGA-SIM-146 | P1 | PASS | Full **7×7** scheme interaction matrix in tuning data | `SchemeInteractionMatrix`, `assets/sim/scheme-interaction-matrix.json` | 49-cell default matrix + tuning JSON overrides |
| MEGA-SIM-147 | P1 | PASS | **Box-and-One** / **Press** fatigue cost during sim minutes | `SchemeFatigueCost`, `PossessionSimSession.accrueFloorMinutes` | Extra stamina drain on defending unit per trip |
| MEGA-SIM-148 | P1 | PASS | **Perimeter / Post** presets bump correct slot usage on possessions | `SchemeOffensiveSlotUsageBias` | Perimeter/post slots weighted before possession credits |
| MEGA-SIM-149 | P0 | PASS | **Primary defensive assignment** UI: map opp PG–C → your defender | `StrategyDefensiveAssignmentPanel`, `DefensiveAssignmentPolicy` | Swap per opp slot; defaults from starters |
| MEGA-SIM-150 | P1 | PASS | Pick **opponent primary scorer slot** for deny toggle | `StrategyDefensiveAssignmentPanel` primary chips | Wired to `GameplanInput.primaryScorerSlot` |
| MEGA-SIM-151 | P1 | PASS | **Coach default schemes** as pre-game default with per-game override save | `DynastyStrategyPrepRepository`, `StrategyScreen`, `UserLeagueGameSimFactory` | Coach O/D default; save game prep; league sim reads override |
| MEGA-SIM-152 | P2 | PASS | Scheme tooltips with identity copy from planning table | `SchemePresetIdentityCopy`, `SchemesStepContent` | Identity line under O/D chips on Strategy + wizard |

---

## N — Injury: mid-game, return paths, medical RS UX

| ID | Pri | Status | Requirement (spec) | Code / test | Finding |
|----|-----|--------|-------------------|-------------|---------|
| MEGA-SIM-153 | P0 | PASS | **Mid-game injury** sample during possession loop; stop minutes accrual | `MidGameInjurySampler`, `PossessionSimSession` + `injuryRoll` | Per-possession roll; injured pulled from floor/minutes ledger |
| MEGA-SIM-154 | P0 | PASS | Possession log line: **“INJURY — {Name} · {label}”** | `MidGameInjurySampler.formatLogLine` | Wired in possession loop |
| MEGA-SIM-155 | P1 | PASS | `occurredInGameMinute` on injury event in result | `GameSimInjuryEvent` on mid-game path | Set from game clock |
| MEGA-SIM-156 | P1 | PASS | Injury probability uses **actual box MIN** not prep estimate | `GameSimInjuryRoll.floorMinutesFromResult` | Post-game roll weights `homeFloorSecondsByPlayer`; test vs prep estimate |
| MEGA-SIM-157 | P1 | PASS | Auto **lineup adjust** on save with starter promotion rules | `InjuryLineupAdjust` | Core tested |
| MEGA-SIM-158 | P1 | PASS | **Return** reclaim starter slot + demote interim | `applyInjuryReturnLineupAdjust` | Week advance path |
| MEGA-SIM-159 | P1 | PASS | **Multi-return same week** PG→C ordering | `InjuryRosterOps.tickWeekAdvance` | Returns sorted PG→C before `applyInjuryReturnLineupAdjust` |
| MEGA-SIM-160 | P0 | PASS | **Medical redshirt** postgame sheet: Apply / Dismiss with eligibility gates | `PostgameInjuryFlow`, `MedicalRedshirtConfirmDialog` | Gates via `MedicalRedshirtPolicy`; confirmation cites games + schedule index |
| MEGA-SIM-161 | P1 | PASS | **Medical RS**: no return adjust; season OUT flag on card | `MedicalRedshirtPolicy.apply`, postgame flow | Apply sets `medicalRedshirtSeason`; clears injury weeks |
| MEGA-SIM-162 | P1 | PASS | Strategy **blocks drag** for `injuryActive` (not grey alpha only) | `StrategyScreen` | `injuryActive` blocks tap/cycle/assign; not only alpha |
| MEGA-SIM-163 | P1 | PASS | AI league games: same injury bank + lineup adjust | `AiLeagueWeekOps.rollInjuriesForCompletedGame` | Synthetic roster minutes + bank per AI game; lineup adjust team count |
| MEGA-SIM-164 | P2 | PASS | **News** headline on major user injury from watch path | `UserInjuryNewsGate`, `DynastyApp` watch dismiss | Major/moderate/high-OVR injuries → `appendUserInjuries` |

---

## O — Lineup validation & sim prep gates

| ID | Pri | Status | Requirement (spec) | Code / test | Finding |
|----|-----|--------|-------------------|-------------|---------|
| MEGA-SIM-165 | P0 | PASS | **Cannot start sim** with empty starter slot or illegal interchange | `GameLineupPrepValidator`, Strategy + watch gate | `build` requires valid five; watch blocked |
| MEGA-SIM-166 | P1 | PASS | Engine **`slotEligible`** re-validates prep at sim entry | `GameLineupPrepValidator` + `LineupSlotPolicy` | Validated before `build` and on watch launch |
| MEGA-SIM-167 | P1 | PASS | **Listed-position ratings in slot played** per possession | `SlotPlayedFloorRating` | Five slot map + comfort nudge on floor |
| MEGA-SIM-168 | P2 | PASS | **Position comfort** nudge from tuning without OVR order flip | `PositionComfortNudge` | −1 eligible interchange, −3 otherwise |
| MEGA-SIM-169 | P1 | PASS | **Height/weight/shooting** struct on `GameSimParticipant` | `FloorShootingEffects`, `GameSimShootingProfile` | Floor three-point tendency nudges scheme effects per possession |
| MEGA-SIM-170 | P1 | PASS | Apply **home venue modifiers** in `PossessionSimSession` | `SimVenueRatingPolicy` | Offense/defense scaled via `GameSimInput.homeVenue` |
| MEGA-SIM-171 | P0 | PASS | **Overtime** loop until winner; `periodsPlayed` | `GameSimOvertime` watch + fast sim | `overtimePeriods`, watch final OT label |
| MEGA-SIM-172 | P1 | PASS | **Strategy lock** after week advance (design lock) | `StrategyEditLockPolicy`, `StrategyScreen` | Prep/read-only when calendar gate closed |
| MEGA-SIM-173 | P1 | PASS | Calendar **user game** wires schemes, roster, injuries, tuning | `UserCalendarGameWatchBootstrap`, `GameWatchScreen` | Preview play path; away-home + `userTeamIsHome` on commit |
| MEGA-SIM-174 | P2 | FAIL | **WIN_SHARES** not in box (correct) — dynasty EoS job documented in sim README | MEGA-SIM-028 | PASS intent; document in UI |

---

## P — Overtime, batch parity, “not boring” acceptance

| ID | Pri | Status | Requirement (spec) | Code / test | Finding |
|----|-----|--------|-------------------|-------------|---------|
| MEGA-SIM-175 | P0 | PASS | **League user games** use full possession engine + injuries + box persistence | `UserLeagueGameSimulator`, `ProvisionalGameMark`, `SaveSlotRepository` | Watch + advance fast-sim on possession path; box JSON in `provisional_games` |
| MEGA-SIM-176 | P1 | PASS | **AI games** possession sim with roster-derived ratings (no random score fallback) | `LeagueFastSim.simulateWithBatch`, `TeamSimRatings` | Prestige-derived ratings; RS batch always supplies `LeagueBatchSimParams` |
| MEGA-SIM-177 | P1 | PASS | Possession count **enforced** in band 60–70 via tuning feedback | `PossessionPaceGovernor` | Dynamic trip pace multiplier in session |
| MEGA-SIM-178 | P2 | PASS | **Tie** handling: OT in possession sim; standings use true W/L | `LeagueFastSim` | No +1 home nudge on completed OT sim |
| MEGA-SIM-179 | P2 | PASS | **Exhibition** injury rate + no medical RS counters surfaced postgame | `PostgameInjurySheet`, `MedicalRedshirtPolicy` | RS games played + exhibition medical RS ineligible copy |
| MEGA-SIM-180 | P1 | PASS | Acceptance: **≥8 distinct terminal event texts** per game on average | `PossessionTerminalSignature`, `PossessionTerminalDiversityTest` | Explicit +2/+3/FT terminals + diversity test |

---

## Summary counts (all items)

| Bucket | Sections A–F (legacy tag) | Sections G–P (expanded tag) | Combined |
|--------|---------------------------|-----------------------------|----------|
| SHIP / PASS | 38 | 2 | **40** |
| PARTIAL / GAP | 18 + 18 GAP + 11 WIRE | 85 GAP/FAIL − 57 FAIL* | — |
| GAP (A–F only) | 18 | — | **18** |
| PARTIAL (A–F) | 18 | — | **18** |
| WIRE (A–F) | 11 | 0 | **11** |
| FAIL (G–P) | — | **57** | **57** |
| GAP (G–P sub) | — | **28** (marked GAP in tables) | **28** |
| PASS (G–P) | — | 2 | **2** |
| **Total `MEGA-SIM-*` rows** | **85** | **95** | **180** |

\*Expanded section uses FAIL for spec violations; rows marked GAP in tables are incremental stubs (28). Treat **ship readiness** as **40 PASS/SHIP of 180 (22%)**.

### Legacy A–F only (unchanged rollup)

| Status | Count (of 85) |
|--------|----------------|
| SHIP | 38 |
| PARTIAL | 18 |
| WIRE | 11 |
| GAP | 18 |

### Expanded G–P rollup

| Status | Count (of 95) |
|--------|----------------|
| FAIL | 57 |
| GAP | 28 |
| PASS | 2 |
| RISK | 0 |
| WIRE | 0 |

---

## P0 FAIL inventory (expanded pass)

All **P0** rows from sections G–P (**22** ids). Fix these before calling game sim “full product”:

| ID | Topic |
|----|--------|
| MEGA-SIM-086 | Shot-clock state machine |
| MEGA-SIM-087 | Shot-clock violation |
| MEGA-SIM-090 | Offensive rebound / 20s reset |
| MEGA-SIM-101 | Personal fouls |
| MEGA-SIM-102 | Foul-trouble substitution |
| MEGA-SIM-103 | In-game rotation from minutes bands |
| MEGA-SIM-112 | Late foul strategy possession logic |
| MEGA-SIM-119 | Watch default 2s pacing |
| MEGA-SIM-120 | Watch speed multiplier UI |
| MEGA-SIM-125 | Full halftime coach sheet |
| MEGA-SIM-129 | Player box score in engine |
| MEGA-SIM-130 | Postgame box score UI |
| MEGA-SIM-137 | Rotation minutes editing UI |
| MEGA-SIM-138 | 200-minute budget meter |
| MEGA-SIM-145 | Per-game scheme pickers → sim input |
| MEGA-SIM-149 | Defensive assignment map UI |
| MEGA-SIM-153 | Mid-game injury in possession loop |
| MEGA-SIM-154 | Live injury log line |
| MEGA-SIM-160 | Medical redshirt postgame sheet |
| MEGA-SIM-165 | Block sim on invalid/empty lineup |
| MEGA-SIM-171 | Overtime + `periodsPlayed` |
| MEGA-SIM-175 | User league games on full engine path |

**P0 overlap with legacy GAP rows (still open):** 004, 007, 012, 014, 016, 017, 018, 030, 033, 050, 058, 062, 077 — treat as same workstream as ids above.

### Suggested build order (density first)

1. **Player + team box** in engine (129) → unlock minutes, injury weight, postgame UI (130, 156).  
2. **On-floor state + subs** (103, 101–102) → fouls, foul strategy, fatigue.  
3. **Shot clock + OReb + violations** (086–087, 090) → log depth (097).  
4. **Watch UX** (119–120, 125) → product feel.  
5. **Strategy wiring** (137–141, 145, 149, 165) → user agency before tip.  
6. **Mid-game injury + medical sheet** (153–154, 160).  
7. **OT + league parity** (171, 175–176).

**Highest-impact gaps for spec fidelity (legacy):** player-level box score and minutes (012, 014, 062), shot-clock violations and OReb reset (003–004), fouls/rotation in sim (016–017), Strategy lineup override + schemes on game input (044, 058, 064), watch UX timing (029–030), halftime full coach adjustments (033), defensive assignment (050), mid-game injuries (018, 077), league AI games on full engine + injury bank (079).

**Build checklist cross-ref:** Phase **10.1–10.8** marked `[x]` in [`docs/BUILD_CHECKLIST.md`](../BUILD_CHECKLIST.md); this audit refines **done when** versus planning detail for sim/strategy/lineup/injuries surfaces — **expanded pass adds 95 rows** for full-product depth.

---

## Document history

| Date | Change |
|------|--------|
| 2026-11-11 | Initial audit — 85× `MEGA-SIM-NNN` items vs four planning docs + core/sim UI |
| 2026-11-11 | Expanded pass — **+95** rows (086–180): possession depth, fouls/clutch, watch, box score, rotation UI, schemes, injuries, lineup validation; **180** total; **22 P0 FAIL** (G–P) |
