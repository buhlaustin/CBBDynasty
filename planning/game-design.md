# Dynasty: College Basketball — Game Design Document

> **Planning index:** [`README.md`](README.md) (all specs by category) · **Tracker:** [`planning-notes.md`](planning-notes.md)  
> **Branding:** [**Dynasty: College Basketball**](branding.md) · Novux Games · bg `#181a25` · logo [`Public-Assets/app-logo.svg`](../Public-Assets/app-logo.svg)

**Platform:** Android (mobile)  
**Presentation:** Text-based **2D** sim (Campus Dynasty–style); **offline** with **local save files**  
**Mode:** Dynasty / program builder — men's college basketball  
**Game sim:** **[`simulation-engine.md`](simulation-engine.md)** — possession engine only (clock, box score)  
**App shell:** **[`app-shell.md`](app-shell.md)** — studio screen, splash, Tap to Play  
**Game start:** **[`game-start.md`](game-start.md)** — home, new dynasty, coach create, school pick  
**In-season UI:** **[`game-frame.md`](game-frame.md)** — dashboard, tabs, hubs  
**Player UI:** **[`player-cards.md`](player-cards.md)** — preview, card tabs, ratings; **[`player-archetypes.md`](player-archetypes.md)** — archetypes, draft rank; **[`player-loyalty-promises.md`](player-loyalty-promises.md)** — loyalty, coach promises, draft/portal  
**Lineup:** **[`positions-and-lineup.md`](positions-and-lineup.md)** — PG/SG/SF/PF/C, interchange, default starters  
**Strategy:** **[`strategy-schemes.md`](strategy-schemes.md)** — O/D presets, halftime, preview/watch  
**Coach UI:** **[`coach-card.md`](coach-card.md)** — coach preview, card tabs, School staff  
**Recruiting:** **[`recruiting-engine.md`](recruiting-engine.md)** — year-long cycle, signing crunch, NIL + points  
**League identity:** **[`league-branding.md`](league-branding.md)** — conference/team real ↔ universe mapping  
**NCBA universe:** **[`ncba-branding.md`](ncba-branding.md)** — national body, March Mayhem, early events  
**Offseason:** **[`offseason.md`](offseason.md)** — carousel, weeks 1–10, Signing Day week 6, Season 1 rules  
**Preseason:** **[`preseason.md`](preseason.md)** — single-week tasks, reports, schedule  
**Time / weeks:** **[`time-system.md`](time-system.md)** — exhibition, RS 1–32, byes, postseason weeks  
**Season review:** **[`season-review.md`](season-review.md)** — end of year before offseason  
**Postseason:** **[`postseason.md`](postseason.md)** — Selection Sunday, NCBA / NCI / Heritage  
**Selection engine:** **[`selection-engine.md`](selection-engine.md)** — poll formula, seed index, cuts  
**Postseason UI:** **[`postseason-frame.md`](postseason-frame.md)** — March Mayhem hub, NCBA/NCI/HC seed labels  
**Document type:** Planning GDD (implementation-ready outline)  
**Default era:** **2026–27** at new dynasty start (**Season 1**)  
**Last updated:** 2026-11-09

---

## Contents

1. [Vision & pillars](#vision--pillars)
2. [Core loop](#core-loop)
3. [Simulation engine (brief)](#simulation-engine-brief)
4. [In-season game frame (brief)](#in-season-game-frame-brief)
5. [Identity packs & save model](#identity-packs--save-model)
6. [World scale & league simulation](#world-scale--league-simulation)
7. [Roster, eligibility & redshirt](#roster-eligibility--redshirt)
8. [Offseason (authoritative calendar)](#offseason-authoritative-calendar)
9. [Recruiting engine](#recruiting-engine)
10. [NIL & program economy](#nil--program-economy)
11. [Coaching carousel & contracts](#coaching-carousel--contracts)
12. [Facilities, fundraising & upgrades](#facilities-fundraising--upgrades)
13. [Conference realignment](#conference-realignment)
14. [Preseason](#preseason)
15. [Scheduling & regular season](#scheduling--regular-season)
16. [Season gameplay & prep](#season-gameplay--prep)
17. [Postseason & selection](#postseason--selection)
18. [Awards & media](#awards--media)
19. [Mobile UX, automation & notifications](#mobile-ux-automation--notifications)
20. [Engineering milestones](#engineering-milestones)
21. [Glossary](#glossary)
22. [Document history](#document-history)

---

## Vision & pillars

### Vision

A **program dynasty** where running a D-I men's hoops school feels like a year-round job: **offseason** portal wars and recruiting, **preseason** schedule craft and hype, **season** survival on the calendar, **March** as the payoff. The default pack runs **365** active D-I programs across **32 conferences** ([`league-branding.md`](league-branding.md), [`2026-NCAA-Directory.md`](2026-NCAA-Directory.md)); **4** remain in **D-II→D-I reclass** and are **postseason-ineligible** until the **4-year** transition completes. Real conference mechanics (schedule size, tiebreakers, tournament shapes) while **display identity** stays swappable and save-safe.

### Pillars

| Pillar | Commitment |
|--------|------------|
| **Scale** | **365** D-I programs in default pack (4 reclass — no postseason until transition done) |
| **Structure** | Real conference sizes & rules (data-driven templates) |
| **Roster law** | 15-man cap, scholarship flexibility, walk-ons |
| **Calendar** | One master timeline for every game year |
| **Offseason depth** | Recruiting + portal + carousel + facilities = co-equal with games |
| **Identity** | Generic default; optional JSON/URL packs; no save corruption on swap |
| **Era** | 2026–27 bootstrap content; dynasty can advance many years |

### Non-goals

- Real-time player-controlled on-court action (inputs are **prep only** — see simulation doc).
- Licensed NCAA / school trademarks in the base APK.
- Real player likenesses or real NIL deals tied to individuals.
- Online multiplayer, cloud saves, or always-on internet.
- Women's basketball (future SKU / expansion).

---

## Core loop

```
OFFSEASON → PRESEASON → REGULAR SEASON → POSTSEASON → (advance year)
     ↑__________________________________________________|
```

**Player fantasy beats**

1. **Build the roster** — portal, recruiting, NIL, cuts, pro-day/draft fallout.
2. **Build the program** — facilities, grades, fundraising, conference trajectory.
3. **Build the résumé** — schedule smart, win the right games, own March.
4. **Build the legend** — coach progression, carousel drama, multi-decade archives.

**Session types (mobile)**

| Session | Typical length | Activity |
|---------|----------------|----------|
| **Micro** | 1–3 min | Sim one day, one recruiting action, one upgrade |
| **Standard** | 10–15 min | Clear a week, recruiting board, schedule tweak |
| **Event** | 20+ min | Selection Sunday, conference tournament run, bracket play |

---

## Simulation engine (brief)

**Possession game sim only:** [`simulation-engine.md`](simulation-engine.md). Dynasty layer owns calendar, saves, NET, recruiting.

| Topic | Summary |
|-------|---------|
| **Style** | Text **2D**; **40:00** clock; **3–30 sec** game time per possession |
| **User leverage** | Lineup, rotation, **gameplan** before tip |
| **Output** | Full box score; live text during sim |
| **League** | Same `simulateGame` for user watch and batch; **when** to call it = calendar (below) |
| **Saves** | **Week advance** checkpoints local file; **replay** simmed games if app closed before then |

---

## In-season game frame (brief)

Full navigation and screens: **[`game-frame.md`](game-frame.md)**.

| Area | Summary |
|------|---------|
| **Bottom tabs** | Roster · Calendar · **Dashboard** · News (headlines only) |
| **Dashboard** | Team overview, notifications, last game, next game + **game preview** (win %, predicted score, team compare, H2H) |
| **Hamburger hubs** | School, Strategy, Statistics, Rankings, News (full), History |
| **School hub** | Overview, roster, schedule, stats, history, records, HOF, drafted players (same layout for other schools view-only) |

---

## Identity packs & save model

### Design lock

| Rule | Detail |
|------|--------|
| **Default ship** | Generic names, generic logos, full **structural** fidelity (IDs, conferences, formats). |
| **Import** | Local **JSON** or remote **URL** (HTTPS); same schema as default. |
| **Swap** | Revert to default or re-import anytime; **never** rewrite dynasty history. |
| **Keys** | All simulation entities use **opaque stable IDs** (`teamId`, `confId`); display strings live in pack overlay. |

### Pack schema (conceptual)

Packs are validated **atomically**. Invalid pack → reject; no partial apply.

**Code:** [`IdentityPackCodec`](../android/core/src/main/kotlin/dev/novux/dynasty/cbb/core/pack/IdentityPackCodec.kt) + [`IdentityPackValidator`](../android/core/src/main/kotlin/dev/novux/dynasty/cbb/core/pack/IdentityPackValidator.kt) (`packVersion` **1**). **Overlay:** [`PackRepository`](../android/app/src/main/kotlin/dev/novux/dynasty/cbb/data/pack/PackRepository.kt) + [`IdentityPackIndex`](../android/core/src/main/kotlin/dev/novux/dynasty/cbb/core/pack/IdentityPackIndex.kt). **Import (JSON/TXT/URL):** [`IdentityPackImportPipeline`](../android/core/src/main/kotlin/dev/novux/dynasty/cbb/core/pack/import/IdentityPackImportPipeline.kt) + [`assets/pack/identity-import-template.txt`](../assets/pack/identity-import-template.txt).

```json
{
  "packVersion": 1,
  "membershipYear": "2026-27",
  "disclaimerAcceptedRequired": true,
  "conferences": [
    {
      "id": "conf-sec",
      "displayName": "Southeast Conference",
      "leagueGameCount": 20,
      "standingsTiebreakers": ["h2h", "h2h-point-diff", "record-vs-top-teams"],
      "tournamentTemplateId": "tpl-sec-16",
      "ncaaAutoBidSlot": 1
    }
  ],
  "teams": [
    {
      "id": "team-001",
      "conferenceId": "conf-sec",
      "displayName": "State University",
      "shortName": "ST",
      "homeCity": "Springfield",
      "homeState": "ST",
      "postseasonEligible": true,
      "reclassTransitionYear": null,
      "colors": { "primary": "#003366", "secondary": "#CCCCCC" },
      "arenaBaseName": "Arena",
      "logoUri": null
    }
  ],
  "tournamentTemplates": [],
  "rulesetYear": 2026
}
```

**Required templates in default pack**

- Conference **round-robin or matrix** schedule generators per `leagueGameCount`.
- **TournamentTemplate** per conference (bracket size, byes, reseeding, venue neutral/home).
- **NCBA** Opening Round (24 at-large, 12 games) + 64-team bracket — [`postseason.md`](postseason.md).

### Save partitioning

| Save layer | Pack-dependent? | Notes |
|------------|-------------------|--------|
| Dynasty metadata | No | Start year, difficulty, coach career |
| Program state | IDs only | Prestige, facility grades, budgets |
| People | No likeness | Generated players/coaches |
| History | IDs only | Results, stats archive |
| Display cache | Yes | Rebuilt on pack swap |

**Post-swap behavior:** Recompute display names/logos; log `packId` + `membershipYear` in dynasty metadata; show non-blocking warning if pack year ≠ current dynasty year.

### Legal UX (ship checklist)

- [ ] Import screen: user attestation + unofficial disclaimer.
- [ ] URL fetch: **domain allowlist**, **5 MB** cap, optional **signature** verification on pack manifest.
- [ ] Store listing: no implied NCAA endorsement.

---

## World scale & league simulation

### Programs

- **365** teams in the default league graph; each **calendar day** only scheduled teams play.
- **User role:** Head coach (single program dynasty); AD-style actions folded into coach UI.

### When games run vs when the save commits

| Action | Effect |
|--------|--------|
| **Sim user game** | Possession sim runs; result held in **current week** session state |
| **Advance week** | **Write save**; week’s results, standings, stats, and league outcomes become **final**; batch-sim other schools’ games in that **week index** (no player-facing **Advance day**) |
| **Advance without playing** | Allowed — confirmation warns unplayed user games will **fast-sim** and lock ([`game-frame.md`](game-frame.md#advance-week-confirmation-sim-week)); same step can apply **AI user-school recruiting** for unfinished optional recruiting tasks |

**UI:** **Required** tasks on **Dashboard** block **Advance week**; **optional** tasks (games, recruiting, etc.) surface work but may be skipped via sim-week confirm — [`game-frame.md`](game-frame.md#dashboard-task-gate).

Until **week advance**, nothing from that week is treated as permanent on disk.

### Replays

- **Fully close and reopen** the app **before** week advance → user may **replay** games already simmed this week (new lineup/gameplan allowed).
- **After** week advance → those games are **locked** in the local save; no replay without cheat/sandbox.

In-week variety is normal (different sim runs, different scores). **Locked** only after the weekly checkpoint.

### Ruleset evolution

- `rulesetYear` in pack is the **era label** for the league snapshot (pairs with `membershipYear`). **Balance and sim** load from embedded [`balance-2026.json`](../assets/balance/balance-2026.json) / [`sim-tuning.json`](../assets/sim/sim-tuning.json); revisions are **pinned on the dynasty at create**, not swapped each season — [ADR-002](../docs/ADR-002-balance-ruleset-bundle.md).
- Portal windows, scholarship limits, and redshirt thresholds live in the **balance bundle** `calendar` section (constants), not in the identity pack.

### Offseason sim

- Batch recruit AI for non-user schools between user sessions (offline, local saves).

---

## Roster, eligibility & redshirt

### Hard limits

| Rule | Value |
|------|--------|
| Roster max | **15** |
| Scholarships max | **15** (may use fewer) |
| Walk-ons | Allowed; count toward 15; see [Walk-ons](#walk-ons) |

### Walk-ons

| Rule | Detail |
|------|--------|
| **Who** | Any program; especially common at low-prestige schools |
| **When** | **Offseason week 7** (roster lock): **walk-on tryouts** — [`offseason.md`](offseason.md) |
| **Pool** | **5–15** local prospects per school, `homeState` weighted to program |
| **Ratings** | Local band — mostly **2★** with rare **3★**; hidden 0–99 in low-60s band |
| **Scholarship** | **No** initial aid; may **earn scholarship** next offseason if minutes / dev thresholds met |
| **Cap** | Up to **2 new walk-ons** per year; still subject to **15-man** roster max |

Walk-ons use the same **player card** / roster pipeline as scholarship players ([`recruiting-engine.md`](recruiting-engine.md#walk-ons)).

### Class & eligibility (2026 ruleset)

| Rule | Detail |
|------|--------|
| **Five-year window** | Continuous eligibility period from **initial full-time enrollment** / age-based start — portal and stay transfers use **immediate eligibility** when time remains in window ([`transfer-portal.md`](transfer-portal.md#eligibility--roster-law-2026)) |
| **Player record** | `eligibilityClockStart`, seasons/games consumed, **eligible next season** flag |
| **Academic standing** | Eligible / at-risk / ineligible per game |
| **Incompletes** | Term boundary can bench for postseason (difficulty setting) |

### Redshirt (development)

Dynasty still supports a **0-game redshirt season** for development (exhibitions do not count as RS games). Under **2026 NCAA-style** rules in balance bundle, the **eligibility clock does not pause** for that choice — document on card as **development year**, not extra eligibility time.

| Type | Trigger |
|------|---------|
| **Standard redshirt** | Preseason elect; **0** RS games |
| **Medical redshirt** | In-season injury; **≤ 15** RS games played; schedule game **≤ 20** when &lt; 15 played; **none** from RS **Week 21+** — [`injuries.md`](injuries.md) |

### Procedural roster events

- **Graduation** — fixed class year.
- **Early draft entry** — **declare intent roll** at Offseason Week 1 (loyalty + promises + mock rank — **not** automatic for high prospects); pro days + withdrawal deadline before draft results week — [`player-loyalty-promises.md`](player-loyalty-promises.md).
- **Coach promises** — **one** career-long promise per player at offer; **broken** → transfer risk; **kept** → loyalty + stay vs declare.
- **Portal in/out** — authoritative [`transfer-portal.md`](transfer-portal.md); broken promises feed portal intent.
- **Cuts** — offseason window; must land ≤15 by roster lock before exhibitions.

---

## Offseason (authoritative calendar)

**Authoritative spec:** [`offseason.md`](offseason.md). **New dynasties** begin **Offseason Week 1** (not preseason).

**Summary**

| Phase | When |
|-------|------|
| **Coaching Carousel** | Before Week 1, **Season 2+** only (not “Week 0” in UI) |
| **Week 1** | Players Leaving — grads, **≥1** user transfer intent, retention, **pro days** |
| **Weeks 2–5** | Recruiting + **Transfer Portal**; **Week 2** = **pro draft results** |
| **Week 6** | **Signing Day** (hard sign, rankings, positions) |
| **Week 7** | Summer workouts (auto) |
| **Week 8** | Cuts & roster lock (≤15); walk-on tryouts (≤2) |
| **Week 9** | Fundraising & upgrades |
| **Week 10** | Conference offers (**skip Season 1**) → **Preseason** |
| → | **Preseason** ([`preseason.md`](preseason.md)) — single setup week ([`offseason.md`](offseason.md)) |

**Pro days:** Week 1 for seniors + draft-declared undergrads; **draft results** Week 2. Early entrants who stay through retention remain eligible; drafted / undrafted exit per draft model at results posting.

---

## Recruiting engine

**Authoritative spec:** [`recruiting-engine.md`](recruiting-engine.md) — year-long cycle, prestige-tier **weekly points**, 2–4K HS + 1.5K JUCO, portal sub-tab, NIL, letter grades, prestige 0–99.

**Brief:** HS/JUCO **seeded in preseason**; **year-long cycle**; **weekly points** (1k–5k/wk by prestige, **fixed grant** each week) buy **DM / call / house / offer / visits / bonus** — **NIL $** separate; **refunded** offer & visit reservations; **Signing Day** OS week 6; **Recommended** default; **50%** unspent carryover; crunch **1.5×** OS weeks 2–5. Interest weights: [`recruiting-engine.md`](recruiting-engine.md#interest--offers). Arc: [`recruiting-engine.md`](recruiting-engine.md#year-long-recruiting-cycle-design-lock).

---

## NIL & program economy

**Authoritative for recruit/retention $** — separate from **recruiting points** ([`recruiting-engine.md`](recruiting-engine.md#resources-recruiting-points--nil)).

### Annual collective pool (design lock)

| Rule | Detail |
|------|--------|
| **Currency** | Abstract **USD / season** on UI (no real persons) — same number on [`game-start.md`](game-start.md) contract preview and player Overview |
| **Fixed per cycle** | **Annual NIL pool** set when the recruiting cycle opens (preseason); unchanged week-to-week like weekly points |
| **Drivers** | **Conference tier** × **`programPrestige` (0–99)** — interpolate inside band |
| **Next year** | Recomputed after **[End of Season](season-review.md)** prestige/conference moves (preview on review) |
| **Spend** | Allocate to recruits at offer/commit and **retention** bumps ([`offseason.md`](offseason.md)); unspent rules in balance JSON |

**Anchor — blue blood (prestige 90–99, power-tier conferences only):** **~$9.7M – $13.2M** — example band, not tied to one real league.

| Driver | Behavior |
|--------|----------|
| **Conference tier** | Each `confId` maps to **power / upper / mid / low** in `balance-2026.json` → `nil.annualPoolByConferenceTier` |
| **Prestige &lt; 90** | Linear **min→max** annual pool inside that tier (`programPrestige` / 99) |
| **Prestige 90–99** | If tier `blueBloodEligible`: use **`blueBloodPoolBand`** lerp (9.7M→13.2M); else cap at tier max |

[`game-start.md`](game-start.md) **contract sheet** previews pool for the chosen school.

### Abstract NIL (no real persons)

| Concept | Behavior |
|---------|----------|
| **Collective budget** | Single **annual $ pool** per program (table above) |
| **Per-player allocation** | Slice of pool committed on recruit sheet / retention UI → Overview **NIL valuation** on card |
| **Recruit demand** | Each recruit has **`nilAskUsd`**; underfunding lowers **interest** ([`recruiting-engine.md`](recruiting-engine.md#interest--offers)) |
| **Retention** | Upperclassmen request raises after breakout seasons — same **NIL Budget** sheet (offseason Week 1) |
| **Fundraising** | Week 8 can add **one-time** pool bump or next-year seed — [`offseason.md`](offseason.md) |

### NIL Budget workflow (design lock)

Single **collective ledger** for the active recruiting cycle — previewed on [`game-start.md`](game-start.md) **NIL budget** line and surfaced in **Recruiting**, **portal**, and **retention** UIs under the label **NIL Budget**.

**No separate finance minigame:** assign **$/yr** per target; the only global constraint is **unspent pool**.

#### Ledger (per program, per cycle)

| Field | Meaning |
|-------|---------|
| `nilPoolAnnualUsd` | Total annual collective $ for this cycle ([annual pool](#annual-collective-pool-design-lock)) |
| `nilCommittedUsd` | Sum of **signed roster deals** for this cycle year (returning players at cycle open + hard commits) |
| `nilReservedUsd` | Sum of **pending offers** (target has an open scholarship offer with a NIL $ on the table, not yet hard signed) |
| `nilRemainingUsd` | `nilPoolAnnualUsd − nilCommittedUsd − nilReservedUsd` (clamp ≥ 0) |

Fundraising / offseason bumps may increase **`nilPoolAnnualUsd`** once per rules in [`offseason.md`](offseason.md); rebalance **remaining** after bump.

#### Per-target offer (recruits & portal)

| Field | Meaning |
|-------|---------|
| **`nilAskUsd`** | Player’s **desired** annual deal (shown on recruit sheet — seeded from stars / rank; balance `nil.askByStarBand`) |
| **`nilOfferUsd`** | User’s current **proposed** annual deal (0 = none) |
| **Per-target cap** | `nilOfferUsd ≤ nilAskUsd × maxOfferMultiplierOfAsk` (default **3×**) **and** `nilOfferUsd ≤ nilRemainingUsd + thisTargetPriorReservation` |

**Example (illustrative):** pool **$10M**, **$0** committed. A **5★** wants **`nilAskUsd` = $2M** → slider max **$6M** (3×). Offer **$6M** → **$4M** left for other targets. Offer **$2M** (meets ask) → **$8M** left.

#### UX (same pattern everywhere)

| Surface | Header strip |
|---------|----------------|
| **Recruiting hub** | **NIL Budget:** `{remaining}` **remaining** of `{pool}` |
| **Recruit / portal sheet** | Ask line + **Assign NIL** control (stepper or slider); show **Max this player: min(3× ask, remaining)** |
| **Retention (OS Week 1)** | Current deal + **raise ask**; assign bump from same remaining ledger |
| **School hub** (optional) | Read-only cycle summary |

**Assign NIL** does **not** spend recruiting points. Changing offer updates **`nilReservedUsd`** immediately while the scholarship offer stands. **Revoke offer** or **drop target** frees that reservation ([`recruiting-engine.md`](recruiting-engine.md#recruiting-ui)).

#### Interest & signing

| Offer vs ask | Interest |
|--------------|----------|
| `nilOfferUsd ≥ nilAskUsd` | Full NIL credit toward interest ([`recruiting-engine.md`](recruiting-engine.md#scoring)) |
| `0 < nilOfferUsd < nilAskUsd` | Scales down (e.g. ratio `nilOfferUsd / nilAskUsd`, exponent `nil.interestNilRatioExponent`) |
| No offer | NIL factor neutral / penalized for high asks |

How much **`nil`** weighs against minutes, prestige, and home is the recruit interest model ([base weights](recruiting-engine.md#base-weights)). Those weights are **proposals** and change **sensitivity** only. **`nil.askByStarBand`**, the **3×** cap (`nil.maxOfferMultiplierOfAsk`), and the annual pool are unchanged ([conflicts](recruiting-engine.md#conflicts--non-overrides)).

**Hard sign / portal join:** freeze **`nilOfferUsd`** on the player → Overview **NIL valuation** ([`player-cards.md`](player-cards.md)); move $ from **reserved** to **committed**. [`transfer-portal.md`](transfer-portal.md) — departures return committed deal to **remaining**.

**In-season retention top-ups:** rare, small; only from **uncommitted** remainder; no new annual pool mid-RS except balance-tuned exceptions.

#### AI programs

Same ledger, cap, and 3× rule; automation spends **`nilRemainingUsd`** down **Recommended** list ([`recruiting-engine.md`](recruiting-engine.md#ai-recruiting)).

### Boosters & fundraising

- Offseason **fundraising mini-phase**: choose events (gala, alumni game, donor drive).
- Success → **cash** + **collective boost** + **facility seed money**.
- **Arena naming rights:** one-time cash infusion; slight prestige hit if name is silly; reversible in fiction only via rebrand cost.

### Academic grades (program)

**Program academics grade (0–100)** computed each spring from:

| Input | Weight (tunable) |
|-------|----------------|
| Team GPA | 30% |
| APR-style retention | 40% |
| Eligibility incidents | −20% penalty stack |
| Academic facility grade | +10% cap bonus |

**Weekly recruiting points:** fixed **per recruiting cycle** from **prestige tier** table — authoritative spec [`recruiting-engine.md`](recruiting-engine.md#prestige-tier-weekly-pool-design-lock). **End of Season** recap prestige/grade moves set the **next** cycle’s pool; coach/facility upgrades affect **efficiency**, not the tier band.

**Upgrades in offseason week 8** apply per [`offseason.md`](offseason.md) (grades → prestige path for future cycles).

### Cash sinks

- Coach buyouts (carousel)
- Facility construction
- NIL pool (in-season retention top-ups limited)

---

## Coaching carousel & contracts

### Coach entity

- **Overall** + **skill tree** (recruiting, player dev, in-game tactics, portal retention, NIL charm).
- **Skill points:** earned on level-up; spend preseason + **each Sunday** in season; **auto-spend** optional with priority list.

### Contract fields

| Field | Notes |
|-------|-------|
| Years remaining | 1–6 typical |
| Salary | Affects AD satisfaction |
| Buyout | User or AI pays to leave |
| Performance goals | Super 16, .500 conf, etc. |

### Carousel AI (behavior spec)

| Factor | Effect on AI |
|--------|----------------|
| Last season wins | Job security |
| Postseason run | Extension + poach immunity short window |
| Prestige mismatch | Mid-major success → power offers |
| Age / tenure | Regression to mean on firing |
| Portal exodus | Firing risk |

**User job security** (UI on coach card only — [`coach-card.md`](coach-card.md#job-security-user-head-coach)): weekly **0–100** score from goals pace, postseason, AD trust, demeanor, portal losses, contract year. Drives firing events and extension offers; distinct from **AD trust** meter on School **Staff** tab.

**User interaction**

- Accept extension, renegotiate, resign.
- **Interview offers** from other schools (prestige check).
- Firing or resignation → **Job center** (offseason peak; lighter in-season):

| Choice | Behavior |
|--------|----------|
| **Accept offer** | Immediate move; prior program continues under AI HC |
| **Decline all & wait** | **Unemployed coach** — league keeps simming; you get **weekly offer cards** in carousel window; no roster/recruiting/schedule control until hired |
| **Take interim / mid-major offer** | Any open posting on the board; prestige gates still apply |

**Unemployed mode:** watch any game, browse jobs, read news, advance calendar. Dynasty **does not pause** the world.

### Assistant coaches & staff labor market

Full **assistant simulation** — not a single aggregate number hiding individuals ([`coach-card.md`](coach-card.md)).

| Element | Spec |
|---------|------|
| **Slots** | **3** assistants default; **4th** slot when `programPrestige ≥ 85` |
| **Coach cards** | Each assistant: **Recruiting / Development / Scouting** (0–99), contract, history snapshots |
| **Staff grade** | Weighted blend of filled roles + HC skill; drives development tick and **recruiting pitch efficiency** (no campus visit system) |
| **Support staff** | Trainers, video, ops — **Staff tab rows only**; `eligibleForHeadCoach: false` |
| **Hiring** | Offseason **carousel weeks** + Staff **Manage**; poach with buyout |
| **Promotion** | **GA → assistant** after **2** seasons on staff track; assistants can enter **HC candidate pool** via ratings + tenure |
| **AI** | Schools hire/fire/poach assistants; user assistants can be poached |

Assistants feed **recruiting AI**, **summer development**, and **scout letter accuracy** on the board.

---

## Facilities, fundraising & upgrades

### Facility categories (grades 1–10)

| Category | Affects |
|----------|---------|
| **Arena** | Home revenue, attendance, recruit wow factor |
| **Practice gym** | Development, scheme prep |
| **Strength & sports science** | Injury recovery, summer gains |
| **Medical** | Medical redshirt odds, injury duration |
| **Academic center** | Grades, eligibility recovery |
| **Recruiting lounge** | Recruit pitch efficiency, NIL presentation, budget efficiency |

### Upgrade flow

- Spend **cash** + **weeks** (construction overlaps recruiting late weeks).
- **Naming rights** on arena: +cash, display name override in pack layer (`arenaDisplayName`).

---

## Conference realignment

**Design lock:** **No relegation.** Conferences can **invite** programs; user/AI **accept or decline**.

### Invite score (AI conference commissioner)

```
inviteScore = 0.35 * onCourtSuccess
            + 0.25 * programPrestige
            + 0.20 * marketSize
            + 0.10 * academicsGrade
            + 0.10 * facilityGrade
```

- Top conferences poach from mids when **inviteScore > threshold** and **conference has slot** (real membership caps from pack).
- User receives **offer card** offseason **week 9** (when enabled) with **pros/cons** (travel, competition, recruiting tier).

### Schedule impact

- Conference change effective **next season**; conference games regenerate from template.

---

## Preseason

**Authoritative spec:** [`preseason.md`](preseason.md).

**Summary:** One **setup week** after offseason. **Eight tasks** — see [`preseason.md`](preseason.md): includes **Tournament Invites** (before scheduling), exhibitions, regular-season builder. Enter [`game-frame.md`](game-frame.md) at **first exhibition** or **first regular-season** game.

**Reports:** Preseason / **pregame** / **postgame** share the same mini-article pattern ([`preseason.md`](preseason.md#preseason-report--game-reports)).

### Exhibitions (brief)

| Type | Record | Redshirt games |
|------|--------|----------------|
| **Exhibition** | No | No |
| **Closed scrimmage** | No | No (narrative only) |

Exhibitions: morale, light poll momentum, injuries possible.

---

## Scheduling & regular season

### Caps

**Design lock:** Every team plays up to **32 regular-season games** (exhibitions **not** included). **28** is **not** the default season length — it is only the **remaining conf + non-conf builder slots** after you accept a **4-game** early event (typically Weeks **1–4** on the RS calendar).

| Constraint | Value |
|------------|-------|
| **RS total** | **32** max (record counts) |
| **No early event** | Up to **32** from conference template + non-conf picker ([`preseason.md`](preseason.md#8--schedule-regular-season-games)) |
| **Early-season event accepted** | **4** event games + **28** other RS (conf + non-conf) = **32** |
| Conference games | Per **conference template** (`leagueGameCount`, repeat opponents allowed — see below) |
| Early-season event | **Up to 4** games toward the **32** cap; invite flow in [`preseason.md`](preseason.md#6--tournament-invites) |

### Scheduling UX

- **Tournament invites (preseason task):** Prestige-driven **0..N** invites; low programs see **No invites available**; accept **one** event or none ([`ncba-branding.md`](ncba-branding.md)).
- **Conference:** Template lists opponents and **meetings per rival** (single or **double** — two games vs same conference team when pack says so). Conference block **visible** in preseason schedule UI; not hand-picked opponent-by-opponent unless template uses open slots.
- **Non-conference:** **Slot count varies by team and conference** — derived as `32 − conferenceGames − tournamentGames` (and pack min/max non-conf rules per league).
- **User picks:** non-conf opponents in allowed windows (Nov–Dec; limited Jan slots for small conferences).
- **Validation:** Preseason **schedule builder** enforces **32** cap, per-team non-conf budget, no double-booked dates, one event per season (not a separate “solver product” — task **8** UX + rules).

### In-season calendar

**Authoritative week model:** [`time-system.md`](time-system.md) — **Exhibition Weeks 1–3**, **Regular Season Weeks 1–32**, conference tournament weeks (= max rounds), **7** national postseason weeks (Opening Round → National Championship).

Narrative months (Nov–Mar) are display-only; **week advance** uses week indices above.

### Conference standings & tiebreakers

**Data-driven stack** per conference in pack (`standingsTiebreakers` array).

**Supported tiebreaker modules (implement in order)**

1. **Head-to-head** (mini-league if 3+ tied)
2. **Head-to-head point differential**
3. **Conference record** (already tied at this step)
4. **Record vs top N** in conference
5. **Record vs common opponents**
6. **Point differential** (all conf games)
7. **Coin flip** (logged for absurdities)

**NCBA** seeding uses **conference tournament champion** as auto bid; remaining at-large from résumé.

---

## Season gameplay & prep

Gameplay is **sim-only**; see **[`simulation-engine.md`](simulation-engine.md)** for possession loop, clock, and archives.

| Mode | Behavior |
|------|----------|
| **Watch sim** | **2s** per possession; **halftime** adjustments ([`strategy-schemes.md`](strategy-schemes.md)) |
| **Fast sim** | Same engine, no wall-clock delay |
| **Prep** | Lineup, rotation, gameplan before tip |

### In-season systems (all in scope)

| System | Spec summary |
|--------|----------------|
| **Injuries** | Weeks out (calendar) — [`injuries.md`](injuries.md) |
| **Discipline** | Random weekly conduct + academic sheets; games suspended — [`in-season-systems.md`](in-season-systems.md) |
| **Portal** | **Offseason only** — [`transfer-portal.md`](transfer-portal.md) |
| **Chemistry** | Team 0–100; clutch sim — [`in-season-systems.md`](in-season-systems.md#chemistry) |
| **Fatigue** | Stamina drain / recovery — [`in-season-systems.md`](in-season-systems.md#fatigue) |

### NCBA NET, human polls & selection (authoritative)

**Formulas, weights, NET vs Media/Coaches vs seed:** [`selection-engine.md`](selection-engine.md).

**Summary:** **NCBA NET** ranks **all 365** via **`netScore`** (efficiency, **`sosRating` 0–99**, quads, form, record — **no** prestige). **Media** and **Coaches** polls rank **365** with **similar** inputs plus prestige, inertia, and narrative. **Selection Sunday** uses **final NET only** for NCBA/NCI/Heritage; **Seed Index** sets **NCBA seeds 1–52** (merit). **T25** = Media and Coaches **#1–#25** callouts ([`game-frame.md`](game-frame.md)).

### Weekly rhythm

- **Sunday:** coach skill points (notification).
- **Tue–Sat:** games per schedule.
- **Monday:** optional news digest (injuries, polls).

---

## Postseason & selection

**Authoritative spec:** [`postseason.md`](postseason.md) — conference tournaments → Selection Sunday → **NCBA** (Opening Round + 64-team bracket) → **NCI** (32) → **National Heritage Classic** (16).

### Summary

| National event | Size | Selection |
|----------------|------|-----------|
| **NCBA Men's BB Tournament** | **76** in event · **64** in Round 1 | **32** autos + **20** at-large (seeds 1–52) + **24** at-large Opening (**12** games) |
| **NCI** | **32** | Highest **NET rank** not in NCBA |
| **National Heritage Classic** | **16** | Highest **NET rank** not in NCBA or NCI |

**Reclass** (`postseasonEligible: false`): **conference tournaments** and **Heritage** yes; **NCBA** and **NCI** no (conf tourney win does not confer NCBA bid — pass-over per conference template).

**Transition clock / Season 1 bootstrap:** see [`postseason.md`](postseason.md#postseason-eligibility-reclass) and [`league-branding.md`](league-branding.md#provisional-transition-programs).

### Conference tournaments

**National slots:** **124** teams per year (**76** NCBA + **32** NCI + **16** Heritage); **241** programs end without a national invite — see [`postseason.md`](postseason.md#season-end-census-365-teams).

Each conference uses a **TournamentTemplate** (real **2026–27** formats in default pack). Example shape:

```json
{
  "id": "tpl-sec-16",
  "bracketSize": 16,
  "seeds": 16,
  "byes": [],
  "reseedingAfterRound": false,
  "sitePolicy": "neutral-primary",
  "qualificationPolicy": "top-14",
  "rounds": [
    { "name": "R1", "games": 8 },
    { "name": "QF", "games": 4 },
    { "name": "SF", "games": 2 },
    { "name": "F", "games": 1 }
  ]
}
```

Universe round labels: [`ncba-branding.md`](ncba-branding.md).

---

## Awards & media

**Authoritative player awards:** [`awards.md`](awards.md) — UI **Team MVP** & **NCBA MVP**; win contribution is sim-only ([`simulation-engine.md`](simulation-engine.md#season-win-contribution-design-lock)).

### Cadence

| Award | When |
|-------|------|
| **Weekly honors** | **Weekly** during conference play (player + conference POTW) |
| **All-conference** | After conference championships |
| **National awards** | After national postseason (**NCBA MVP**, All-America) |
| **Team MVP** | End of Season (top win contribution on roster) |
| **Hall of fame (dynasty)** | Career archive milestones — [`history-hub.md`](history-hub.md) |

### Media systems

- **Rankings:** weekly **NCBA NET** (365) + **Media** & **Coaches** polls (365; T25 callouts).
- **Bracketology:** updates weekly in February in news feed.
- **User stories:** win streaks, upset alerts, portal steals — portal product [`news-hub.md`](news-hub.md).

---

## Mobile UX, automation & notifications

### Automation toggles (global)

| System | Auto options |
|--------|----------------|
| Coach skills | Auto-spend with priority tree |
| Summer/preseason workouts | Always auto |
| Recruiting | Auto-maintain from **Recommended** list (caps) — [`recruiting-engine.md`](recruiting-engine.md#recommended-tab-default) |
| Non-user games | Always batch sim |

### Manual must-feel-good

- Recruiting points & NIL on borderline targets
- Opening-round / tournament rounds for user team
- Conference accept/decline
- Schedule non-conference rivals

### In-app notifications (Dashboard feed)

**No OS push** — authoritative catalog: [`notifications.md`](notifications.md).

| Tier | Examples |
|------|----------|
| High | User injury, commit/decommit, portal sign, carousel must-act, Selection Sunday (you) |
| Medium | Sunday coach points, game this week, medical RS, Signing Day |
| Low | Facilities, user NET/poll spike |

League stories → **News** only, not the notification feed.

### Accessibility

- Large tap targets on calendar.
- Color-blind safe pack colors; pattern overlays for jersey sim.

---

## Engineering milestones

Ordered build tracks toward **shippable game** (not a separate “demo SKU”).

| Track | Delivers |
|-------|----------|
| **Calendar & saves** | Day/week kernel, local persistence, stable IDs, pack overlay |
| **Possession sim** | 40-min engine, coherence checks, box scores — [`simulation-engine.md`](simulation-engine.md) |
| **Dynasty systems** | Offseason, recruiting, portal, facilities |
| **Full league** | 365-team scheduling, daily batch sim, derived résumé |
| **March** | Conference templates, NCBA, **NCI**, **Heritage** — [`postseason.md`](postseason.md) |
| **Ship** | Full 2D UI, automation toggles, in-app notification feed, **JSON/URL identity import** |

**Code:** `android/` — `:app`, `:core`, `:database`; see [`docs/ARCHITECTURE.md`](../docs/ARCHITECTURE.md). **Build order:** [`docs/BUILD_CHECKLIST.md`](../docs/BUILD_CHECKLIST.md).

### Remaining product decisions (short list)

- ~~Local save format (SQLite vs files).~~ **SQLite/Room** — [`docs/ADR-001-dynasty-save-backend.md`](../docs/ADR-001-dynasty-save-backend.md).
- ~~Balance vs sim tuning storage.~~ **Split embedded bundles**, frozen at dynasty create — [`docs/ADR-002-balance-ruleset-bundle.md`](../docs/ADR-002-balance-ruleset-bundle.md), [`docs/ADR-003-sim-tuning-bundle.md`](../docs/ADR-003-sim-tuning-bundle.md).
- URL pack **signing** for optional one-time import while offline play remains default.

---

## Glossary

| Term | Meaning |
|------|---------|
| **Pack** | Identity overlay JSON (teams, names, templates) |
| **Ruleset year** | Real-world rule configuration for that calendar year |
| **Resume score** | Composite for bubble/seeding |
| **NCBA** | National College Basketball Association (universe NCAA) |
| **NCI** | National College Invitational — 32 teams after NCBA field |
| **Opening Round** | 24 at-large bubble · 12 games → seeds 53–64 (76-team event) |
| **March Mayhem** | Universe brand for March postseason |
| **Grade multiplier** | Academics → recruiting budget |

---

## Document history

| Date | Change |
|------|--------|
| 2025-11-07 | Initial organization from game design prep notes |
| 2025-11-07 | Identity import (JSON/URL), preseason scope, sim layers, 2026–27 default era |
| 2025-11-07 | Deep finish: offseason calendar, recruiting/NIL, selection math, tiebreakers, UX, milestones |
| 2025-11-08 | Linked `simulation-engine.md`; text-based offline sim, day-advance league batching |
| 2025-11-08 | NET realism note; save integrity vs per-game variety (simulation doc) |
| 2025-11-08 | `simulation-engine.md` scoped to possession game sim only |
| 2025-11-08 | Save on week advance; replay games if reopened before checkpoint |
| 2025-11-08 | Linked `game-frame.md` (in-season UI shell) |
| 2025-11-08 | 365-team pack; reclass postseason ineligibility until 4-year transition |
| 2025-11-08 | Season 1 = 2026–27; reclass years anchored to dynasty start |
| 2025-11-08 | Remove product v1/v2 scope language; design locks only |
| 2025-11-08 | Restore full-product specs (staff, walk-ons, secondary postseason, unemployed coach) |
| 2025-11-08 | HS/JUCO recruit seed moved to preseason (Top 60 in-season) |
| 2025-11-08 | NCBA/NCI/March Mayhem; early events; round labels — `ncba-branding.md` |
| 2025-11-08 | `postseason.md` — Selection Sunday, Heritage, reclass conf tourney |
| 2025-11-09 | Offseason calendar → `offseason.md`; start Week 1; carousel label |
| 2025-11-09 | Preseason → `preseason.md`; 7 tasks; report pattern |
| 2025-11-09 | Postseason census 124 national / 241 done (see `postseason.md`) |
| 2025-11-09 | Poll=#1–365; pass-over runner-up; parallel NCI/HC |
| 2025-11-08 | Linked `player-cards.md` |
| 2026-11-09 | NIL interest points at recruit interest weights; ask bands, 3× cap, and annual pool unchanged |
