# MEGA build checklist — full product (no deferrals)

**Product:** Dynasty: College Basketball · **Studio:** Novux Games  
**Authority:** Every row traces to a **locked** decision in [`planning/`](../planning/). This checklist **supersedes** the coarse phase table in [`BUILD_CHECKLIST.md`](BUILD_CHECKLIST.md) for honest ship tracking.

**Policy (from [`planning/planning-notes.md`](../planning/planning-notes.md))**

- We are building the **full game**, not a scaled-down v1, MVP, or “ships later” slice.
- **Nothing is deferred** on this list: `MISSING`, `FAIL`, `GAP`, `PARTIAL`, `STUB`, and `WRONG` all mean **work remains**.
- Mark **`[x]`** only when the item’s **Status** is `DONE` / `SHIP` / `PASS` in the audit shard and **Done when** is satisfied on device.

---

## Audit methodology (2026-11-11, expanded pass)

| Shard | Planning / scope | Rows (table) |
|-------|------------------|-------------:|
| [`game-design.md`](MEGA_BUILD_CHECKLIST_AUDIT/game-design.md) | `game-design.md` | 142 |
| [`recruiting-transfer-start.md`](MEGA_BUILD_CHECKLIST_AUDIT/recruiting-transfer-start.md) | recruiting-engine, transfer-portal, game-start | 166 |
| [`sim-strategy-lineup-injuries.md`](MEGA_BUILD_CHECKLIST_AUDIT/sim-strategy-lineup-injuries.md) | simulation-engine, strategy, lineup, injuries | 216 |
| [`frame-dashboard-time.md`](MEGA_BUILD_CHECKLIST_AUDIT/frame-dashboard-time.md) | game-frame, dashboard-tasks, time, off/preseason | 158 |
| [`coach-player-ui.md`](MEGA_BUILD_CHECKLIST_AUDIT/coach-player-ui.md) | coach-card, player-cards, archetypes, loyalty | 224 |
| [`postseason-awards-history.md`](MEGA_BUILD_CHECKLIST_AUDIT/postseason-awards-history.md) | postseason, selection, awards, season-review, history | 233 |
| [`news-shell-branding.md`](MEGA_BUILD_CHECKLIST_AUDIT/news-shell-branding.md) | news, notifications, in-season, app-shell, branding | 191 |
| [`league-world-pack.md`](MEGA_BUILD_CHECKLIST_AUDIT/league-world-pack.md) | league/NCBA/directory, pack | 152 |
| [`planning-notes-traceability.md`](MEGA_BUILD_CHECKLIST_AUDIT/planning-notes-traceability.md) | Every **Done** row in `planning-notes.md` + 33-file inventory | 115 |
| [`integration-playtest-regression.md`](MEGA_BUILD_CHECKLIST_AUDIT/integration-playtest-regression.md) | Perf, persistence, nav, playtest, Play/R8 release | 170 |

**Scale:** **~1,650** unique `MEGA-*` mentions in shards (rollup text repeats some IDs). **`mega_queue.py`** ingests primary audit rows. The executor queue in [`MEGA_BUILD_QUEUE.json`](MEGA_BUILD_QUEUE.json) excludes **`MEGA-QA-*`** (manual QA), **`MEGA-NOTES-*`** (traceability), and **`MEGA-POST-*`** (postseason — **`~166`** open rows still in shards). **Remaining subscription work** = `python3 planning/tools/mega_queue.py resync` then `stats` → **`remaining`** (persisted as `queueMetrics` in [`MEGA_BUILD_QUEUE_STATE.json`](MEGA_BUILD_QUEUE_STATE.json)). On the integration PR branch that is **480** open rows (**~479** in-season items; excludes **`MEGA-POST-*`**, **`MEGA-QA-*`**, **`MEGA-NOTES-*`**). Re-run after every shard status change — do not treat the old **646**-row JSON as canonical; it incorrectly included postseason rows.  
**Method:** Composer sub-agent spec↔code audits (static trace + grep). **`MEGA-QA-*`** lives only in [`integration-playtest-regression.md`](MEGA_BUILD_CHECKLIST_AUDIT/integration-playtest-regression.md) for manual/release checklists.

## Autonomous queue (subscription)

See **[`MEGA_BUILD_SUBSCRIPTION.md`](MEGA_BUILD_SUBSCRIPTION.md)** — timer **`mega-build-queue`**: one row per wake; **all commits** go to **`cursor/mega-build-executor-cb09`** and **one integration PR** (`update_pr` only). CLI: [`planning/tools/mega_queue.py`](../planning/tools/mega_queue.py).

**How to use (manual)**

1. Pick a shard (or planning doc) and work **P0 → P1 → P2**.
2. When an item ships, update its row in the shard (`Status` → `DONE`/`PASS`/`SHIP`) and only then flip related phase rows in `BUILD_CHECKLIST.md`.
3. For **`planning-notes.md` topics marked Done**, start with [`planning-notes-traceability.md`](MEGA_BUILD_CHECKLIST_AUDIT/planning-notes-traceability.md) — many are **FAIL** vs code.
4. Before any release candidate, clear **`integration-playtest-regression.md`** P0 ship gate.

---

## Executive summary — why the old checklist lied

The phase checklist marked **hundreds** of steps `[x]` while the codebase still uses:

- **Demo/stub data paths** (`RecruitingClassSeed.demoPool`, default prestige 40) on live UI.
- **Placeholder tabs** (`recruiting_placeholder_hs_juco`, portal placeholder).
- **Thin shells** (coach card without tabs/portraits; player card without Stats/Games/History).
- **Possession sim** that passes smoke tests but not the depth in `simulation-engine.md` (narration, fouls, clutch, tuning surfaces).
- **`planning-notes.md` “Done” labels** that overstate shell wiring as full GDD depth (see traceability shard).

**Do not treat open testing as “feature complete.”** Ship when MEGA P0 trends to zero **on device**, not when phase boxes are checked.

---

## P0 — Ship blockers (rollup)

Consolidated hotspots. Every shard has its own P0 table with evidence paths.

### Recruiting & calendar

| ID | Finding |
|----|---------|
| MEGA-RECR-005, 006, 133 | **120-player `demoPool`** on hub/AI/signing vs persisted 2k–4k class |
| MEGA-RECR-015, 139 | Weekly points / spend tied to **prestige 40**, not chosen school |
| MEGA-RECR-027, 082 | Interest meter **does not move** on DM/call/house spend |
| MEGA-RECR-035–036 | HS/JUCO + **Portal hub placeholders** |
| MEGA-RECR-042, 145 | In-season **recruiting dashboard tasks** not emitted |
| MEGA-RECR-100 | **NIL cycle** not bootstrapped at dynasty sign |
| MEGA-RECR-131, 161 | Main-thread class seed → **lag / ANR** risk |
| MEGA-QA-052, 101–102 | Recruiting **visible when cycle closed** (signing→preseason) |

### Coach & player presentation

| ID | Finding |
|----|---------|
| MEGA-COACH-031, 108–109 | Coach card **no tab shell** (Overview/Contract/History/HOF/Draft) |
| MEGA-COACH-095–098 | **No portrait renderer** (coach + player headshot pipeline) |
| MEGA-COACH-060, 139, 142, 149–154 | Player **tabbed card** (Stats/Games/History) missing |
| MEGA-COACH-090 | Offseason/preseason roster row **tap no-op** |

### Simulation

| ID | Finding |
|----|---------|
| MEGA-SIM-086–087, 101–103 | Possession depth: shot clock, OReb, foul subs **not spec-grade** |
| MEGA-SIM-119–120, 125, 129–130 | **Watch mode** pacing/controls/halftime sheet thin |
| MEGA-SIM-137–138, 145 | **Strategy minutes / schemes** UI vs locked gameplan |

### Shell, dashboard, time

| ID | Finding |
|----|---------|
| MEGA-FRAME-085–088 | **Tasks disappear** (gate lists, `completeTask` bypass) |
| MEGA-FRAME-089–090, 156 | Recruiting **between signing & preseason** (nav + `hubAccessible`) |
| MEGA-FRAME-091–093, 158 | **Sim-week lag** (heavy `advanceDynastyWeek`, no loading UX) |
| MEGA-QA-008, 013–014 | Advance perf acceptance |

### GDD / world

| ID | Finding |
|----|---------|
| MEGA-GDD-074, 086 | **Statistics hub placeholder**; no shipping pack import UI |
| MEGA-GDD-099–100, 041 | **Standings tiebreakers** not implemented |
| MEGA-GDD-111–112 | Demo pool + default prestige on live recruiting economy |

### Release & persistence

| ID | Finding |
|----|---------|
| MEGA-QA-028–030 | Kill mid-advance / corrupt slot handling |
| MEGA-QA-119, 127–128, 134–137 | Emulator + **device + Play open-test** smoke |
| MEGA-QA-151–153 | **R8 / Room** release smoke |

---

## Your reported issues → audit IDs

| Symptom | Audit mapping |
|---------|----------------|
| No coach avatars | `MEGA-COACH-095`–`098`, `016`–`018`; traceability `MEGA-NOTES-*` coach row |
| Sim bare / boring | `MEGA-SIM-086`–`180` shard sections G–P |
| Recruiting points buggy | `MEGA-RECR-015`, `022`, `027`, `139`; `MEGA-QA-060`–`061` |
| Recruits not generating | `MEGA-RECR-001`–`008`, `133`; `MEGA-QA-076`–`077` |
| Tasks disappear | `MEGA-FRAME-085`–`088`, `MEGA-QA-088`–`092` |
| Recruiting stuck signing day → preseason | `MEGA-FRAME-089`–`090`, `MEGA-RECR-038`, `051`; `MEGA-QA-101`–`102` |
| Team vs player rankings confusion | `MEGA-RECR-055`–`056`, `MEGA-QA-111`–`112` |
| Laggy / buggy | `MEGA-QA-001`, `008`, `013`–`014`, `128`; `MEGA-RECR-131`, `161` |

---

## Planning doc → shard index

| Planning doc | Primary shard |
|--------------|----------------|
| `game-design.md` | `game-design.md` |
| `recruiting-engine.md` | `recruiting-transfer-start.md` |
| `transfer-portal.md` | `recruiting-transfer-start.md` |
| `game-start.md` | `recruiting-transfer-start.md`, `coach-player-ui.md` |
| `simulation-engine.md` | `sim-strategy-lineup-injuries.md` |
| `strategy-schemes.md` | `sim-strategy-lineup-injuries.md` |
| `positions-and-lineup.md` | `sim-strategy-lineup-injuries.md` |
| `injuries.md` | `sim-strategy-lineup-injuries.md` |
| `game-frame.md` | `frame-dashboard-time.md` |
| `dashboard-tasks.md` | `frame-dashboard-time.md` |
| `time-system.md` | `frame-dashboard-time.md` |
| `offseason.md` | `frame-dashboard-time.md` |
| `preseason.md` | `frame-dashboard-time.md` |
| `coach-card.md` | `coach-player-ui.md` |
| `player-cards.md` | `coach-player-ui.md` |
| `player-archetypes.md` | `coach-player-ui.md` |
| `player-loyalty-promises.md` | `coach-player-ui.md` |
| `postseason.md` | `postseason-awards-history.md` |
| `postseason-frame.md` | `postseason-awards-history.md` |
| `selection-engine.md` | `postseason-awards-history.md` |
| `awards.md` | `postseason-awards-history.md` |
| `season-review.md` | `postseason-awards-history.md` |
| `history-hub.md` | `postseason-awards-history.md` |
| `news-hub.md` | `news-shell-branding.md` |
| `notifications.md` | `news-shell-branding.md` |
| `in-season-systems.md` | `news-shell-branding.md` |
| `app-shell.md` | `news-shell-branding.md` |
| `branding.md` | `news-shell-branding.md` |
| `league-branding.md` | `league-world-pack.md` |
| `ncba-branding.md` | `league-world-pack.md` |
| `2026-NCAA-Directory.md` | `league-world-pack.md` |
| `planning-notes.md` | `planning-notes-traceability.md` |
| Playtest / CI / Play | `integration-playtest-regression.md` |

---

## Workstreams (recommended parallel teams)

1. **Recruiting truth path** — kill `demoPool`; prestige from school; HS/JUCO/Portal tabs; interest spend; NIL bootstrap; class off main thread.  
2. **Coach & player presentation** — portrait renderer + full card tabs per `coach-card.md` / `player-cards.md`.  
3. **Sim depth** — possession narrative, foul strategy, watch mode, box score, rotation UX (`simulation-engine.md`).  
4. **Dashboard & calendar** — task gate completeness, phase transitions, hamburger routes when cycle closed.  
5. **League & postseason** — tiebreakers, March Mayhem / bracket / selection depth.  
6. **Shell & news** — statistics hub, deep links, notification copy, adaptive icon per `branding.md`.  
7. **QA gate** — execute `MEGA-QA-*` P0 matrix on API 34 emulator + physical device before Play promote.

---

## Document maintenance

| Event | Action |
|-------|--------|
| Planning **design lock** changes | Update planning doc + matching **shard** + this index. |
| Feature actually ships | Set shard row to `DONE`/`PASS`/`SHIP`; update `planning-notes-traceability` if topic was falsely **Done**. |
| New playtest bug | Add `MEGA-QA-*` or domain row; link under **Your reported issues**. |
| Release gate | **No open P0** in shards + integration P0 ship gate cleared on device. |

### Document history

| Date | Change |
|------|--------|
| 2026-11-11 | Initial MEGA checklist — 722 items, 8 shards |
| 2026-11-11 | **Expanded pass** — ~1,788 rows, 10 shards (planning-notes traceability + integration QA); deepened all 8 original audits |
