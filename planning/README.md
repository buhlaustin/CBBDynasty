# Planning index

Authoritative design for **Dynasty: College Basketball**. **Granular ship truth:** [`docs/MEGA_BUILD_CHECKLIST.md`](../docs/MEGA_BUILD_CHECKLIST.md) (every locked decision → `MEGA-*` item). Phase order: [`docs/BUILD_CHECKLIST.md`](../docs/BUILD_CHECKLIST.md).

| If you want… | Start here |
|--------------|------------|
| Product vision, economy, schedule rules, glossary | [`game-design.md`](game-design.md) |
| “What’s settled vs still tuning?” | [`planning-notes.md`](planning-notes.md) |
| One topic, full depth | Table below → linked spec |

**Layout:** Specs stay as **flat** `.md` files in this folder so links from `docs/`, `game-design`, and ADRs stay stable. Generators live in [`tools/`](tools/) (`generate_adaptive_icon.py`, `generate_default_pack.py`, …).

---

## Core & simulation

| Doc | Role |
|-----|------|
| [`game-design.md`](game-design.md) | GDD spine — loop, economy, scheduling, postseason summary, cross-links |
| [`simulation-engine.md`](simulation-engine.md) | Possession sim, box score, tuning bundle hooks |

---

## App shell & navigation

| Doc | Role |
|-----|------|
| [`branding.md`](branding.md) | Product name, studio, visual identity |
| [`app-shell.md`](app-shell.md) | Studio intro, splash, Tap to Play |
| [`game-start.md`](game-start.md) | New dynasty, coach create, school pick, contract preview |
| [`game-frame.md`](game-frame.md) | In-season chrome, dashboard, hubs, advance week, school hub |
| [`dashboard-tasks.md`](dashboard-tasks.md) | Required / optional dashboard cards (master index) |
| [`notifications.md`](notifications.md) | In-app feed only (no OS push) |
| [`news-hub.md`](news-hub.md) | League headlines & stories (not the notification feed) |
| [`history-hub.md`](history-hub.md) | GOAT, HOF, record books, EoS recompute |

---

## Coach, strategy & player UI

| Doc | Role |
|-----|------|
| [`coach-card.md`](coach-card.md) | Coach card, staff, contracts, AD trust |
| [`strategy-schemes.md`](strategy-schemes.md) | Offense/defense presets, halftime, watch sim |
| [`positions-and-lineup.md`](positions-and-lineup.md) | Positions, default lineup, interchange |
| [`player-cards.md`](player-cards.md) | Roster card tabs, NIL line, ratings display |
| [`player-archetypes.md`](player-archetypes.md) | Archetypes, hidden tags, draft projection |
| [`player-loyalty-promises.md`](player-loyalty-promises.md) | Loyalty, coach promises, declare / portal bias |

---

## Calendar & season phases

| Doc | Role |
|-----|------|
| [`time-system.md`](time-system.md) | Exhibition, RS weeks 1–32, byes, postseason week indices |
| [`preseason.md`](preseason.md) | Preseason week tasks, exhibitions, schedule builder |
| [`season-review.md`](season-review.md) | End-of-season week before offseason |
| [`offseason.md`](offseason.md) | Weeks 1–10, carousel, Signing Day, fundraising |

---

## Roster building (recruiting & portal)

| Doc | Role |
|-----|------|
| [`recruiting-engine.md`](recruiting-engine.md) | Year-long cycle, points, NIL, interest, Influence board, UI |
| [`transfer-portal.md`](transfer-portal.md) | Offseason portal only, shared budget with recruiting |

**Economy detail:** annual NIL pool + **NIL Budget** workflow → [`game-design.md#nil--program-economy`](game-design.md#nil--program-economy).

---

## In-season roster systems

| Doc | Role |
|-----|------|
| [`injuries.md`](injuries.md) | Injury weeks, medical redshirt, auto lineup |
| [`in-season-systems.md`](in-season-systems.md) | Discipline, academics, chemistry, fatigue |

---

## Postseason & selection

| Doc | Role |
|-----|------|
| [`postseason.md`](postseason.md) | Conference championships, Selection Sunday, NCBA / NCI / Heritage |
| [`selection-engine.md`](selection-engine.md) | Polls, NET-style index, seeding, bubble |
| [`postseason-frame.md`](postseason-frame.md) | March Mayhem hub UI |
| [`awards.md`](awards.md) | **Awards hub** (Yearly / Weekly), **NCBA** position POY, Conference honors |

---

## League data & fiction

| Doc | Role |
|-----|------|
| [`league-branding.md`](league-branding.md) | 365 programs, conferences, pipelines, pack rules |
| [`ncba-branding.md`](ncba-branding.md) | National body names, events, March Mayhem |
| [`2026-NCAA-Directory.md`](2026-NCAA-Directory.md) | Real-school → universe mapping reference |
| [`tools/`](tools/) | Pack / branding generators (not player-facing specs) |

---

## Suggested reading order (new contributor)

1. [`game-design.md`](game-design.md) — vision & loop (skim Contents)
2. [`game-frame.md`](game-frame.md) + [`dashboard-tasks.md`](dashboard-tasks.md) — how a week feels
3. [`time-system.md`](time-system.md) → [`offseason.md`](offseason.md) → [`preseason.md`](preseason.md)
4. Depth for your lane: recruiting pack, sim, postseason, or UI from the tables above

---

## Balance & engineering (outside this folder)

| Resource | Role |
|----------|------|
| [`assets/balance/balance-2026.json`](../assets/balance/balance-2026.json) | Tunable numbers (prestige tiers, NIL, discipline, …) |
| [`docs/ADR-001`](../docs/ADR-001-dynasty-save-backend.md) | Saves, migrations |
| [`docs/ADR-002`](../docs/ADR-002-balance-ruleset-bundle.md) | Balance ruleset pinning |
| [`docs/ADR-003`](../docs/ADR-003-sim-tuning-bundle.md) | Sim tuning only |
| [`docs/ARCHITECTURE.md`](../docs/ARCHITECTURE.md) | Android modules |

When planning text and balance JSON disagree, **JSON wins** for numbers ([ADR-002](../docs/ADR-002-balance-ruleset-bundle.md)).

---

## Still tuning (not missing specs)

Behavior is locked in prose; **numbers** or small gaps may remain in balance / checklist:

| Area | Where to look |
|------|----------------|
| Recruit interest **weights** & action deltas | [`recruiting-engine.md#open--tbd`](recruiting-engine.md#open--tbd), `recruiting.interest.*` in JSON (proposed) |
| `signLikelihood` bands | `recruiting.recommended` in balance |
| Identity **URL pack** signing | [`game-design.md`](game-design.md) (remaining import decisions) |

---

**Last updated:** 2026-11-09
