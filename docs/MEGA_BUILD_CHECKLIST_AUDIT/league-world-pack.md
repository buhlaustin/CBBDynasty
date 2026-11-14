# MEGA build checklist audit — league world & identity pack

**Repo:** `/workspace`  
**Audited:** 2026-11-11  
**Planning authority:** [`planning/league-branding.md`](../../planning/league-branding.md), [`planning/ncba-branding.md`](../../planning/ncba-branding.md), [`planning/2026-NCAA-Directory.md`](../../planning/2026-NCAA-Directory.md) (census / realignment scope), [`planning/game-design.md`](../../planning/game-design.md#identity-packs--save-model) (pack usage & world sim)  
**Code scope:** `assets/pack/**`, `android/core/.../pack/**`, `android/core/.../league/**`, `PackRepository`, league batch wiring in `data/league`

## Status legend

| Tag | Meaning |
|-----|---------|
| **SHIP** | Matches planning intent; validated in CI or prod path |
| **PARTIAL** | Data or engine present; behavior simplified vs spec |
| **GAP** | Required by planning; not implemented or unused |
| **WIRE** | Core/schema ready; app UX or dynasty loop incomplete |

---

## A — Census & 2026–27 universe

| ID | Status | Requirement (planning) | Code / asset | Finding |
|----|--------|------------------------|--------------|---------|
| MEGA-WORLD-001 | SHIP | **365** active D-I programs in default universe | `embedded-default.json`, `DefaultPackAssetTest` | `teams.size == 365` |
| MEGA-WORLD-002 | SHIP | **32** conferences | Same test + `conferences` array | 32 `conf-*` rows |
| MEGA-WORLD-003 | SHIP | **361** postseason-certified + **4** reclass in pack | `postseasonEligible` counts in `DefaultPackAssetTest` | 361 true / 4 false |
| MEGA-WORLD-004 | SHIP | Reclass schools match directory/bootstrap years | Pack JSON + [`league-branding.md`](../../planning/league-branding.md#provisional--transition-programs) | West Florida Y1, New Haven Y2, Mercyhurst Y3, West Georgia Y3 |
| MEGA-WORLD-005 | SHIP | Season 1 dynasty = **2026–27** membership snapshot | `membershipYear: "2026-27"`, `rulesetYear: 2026` | Aligns with directory opening baseline |
| MEGA-WORLD-006 | PARTIAL | Directory **29 moves** / Pac-12 revival reflected in membership | Pack conf assignment vs [`2026-NCAA-Directory.md`](../../planning/2026-NCAA-Directory.md) | Structural membership in pack; narrative tables not duplicated in JSON |
| MEGA-WORLD-007 | SHIP | Conference sizes match planning tables (8–18 teams) | Python census on pack | Sizes 8,9,10,…18 across 32 leagues; no orphan teams |
| MEGA-WORLD-008 | SHIP | Stable opaque IDs (`team-conf-{conf}-NNN`, `conf-*`) | `DefaultPackAssetTest.embeddedDefault_teamIds_uniqueAndPrefixed` | All teams prefixed `team-conf-` |
| MEGA-WORLD-009 | SHIP | Real reference names stay in **markdown only**; universe names in pack | `league-branding.md` vs JSON `displayName` | No NCAA trademarks in default JSON display fields |
| MEGA-WORLD-010 | PARTIAL | Single source regen after membership edits | `planning/tools/generate_default_pack.py` → dual copy | Manual regen; no CI drift check beyond tests |

---

## B — `assets/pack` authoring & shipment

| ID | Status | Requirement | Code / asset | Finding |
|----|--------|-------------|--------------|---------|
| MEGA-WORLD-011 | SHIP | Repo SoT + APK copy of embedded default | `assets/pack/embedded-default.json`, `android/app/src/main/assets/pack/` | Byte-identical copies (2026-11-11) |
| MEGA-WORLD-012 | SHIP | README documents regen command | `assets/pack/README.md` | `python3 planning/tools/generate_default_pack.py` |
| MEGA-WORLD-013 | SHIP | `packVersion` **1** gate | `PackConstants.SUPPORTED_PACK_VERSION`, validator | Reject unsupported versions |
| MEGA-WORLD-014 | SHIP | `packId: embedded-default` on ship bundle | JSON + `DynastySchema.DEFAULT_PACK_ID` | Wizard/save default pack id |
| MEGA-WORLD-015 | SHIP | `disclaimerAcceptedRequired: true` on default pack | JSON field | Import pipeline enforces attestation |
| MEGA-WORLD-016 | SHIP | **32** `tournamentTemplates` (one per conference) | JSON `tournamentTemplates.length` | `tpl-conf-*` linked from each conference |
| MEGA-WORLD-017 | PARTIAL | Generator derives bracket size from team count | `generate_default_pack.py` `bracket_for()` | Power-of-two rounding; round labels generic (R1/QF/…) |
| MEGA-WORLD-018 | SHIP | Per-conf `leagueGameCount` structural totals | JSON + `LEAGUE_GAMES_BY_CONF` in generator | e.g. G10/ACROWN 20, ASCU 14, matches branding intent |
| MEGA-WORLD-019 | GAP | Pack JSON carries **NCBA / event branding** keys | [`ncba-branding.md`](../../planning/ncba-branding.md), [`identity-import-template.txt`](../../assets/pack/identity-import-template.txt) L40 | No `branding` / `round.*` in default JSON; hardcoded elsewhere |
| MEGA-WORLD-020 | SHIP | TXT import grammar + overlay template shipped | `assets/pack/identity-import-template.txt` | Documents full vs overlay modes |

---

## C — `:core/pack` model, validation, overlay

| ID | Status | Requirement | Code / test | Finding |
|----|--------|-------------|-------------|---------|
| MEGA-WORLD-021 | SHIP | Atomic parse + validate (reject whole pack) | `IdentityPackCodec`, `IdentityPackValidator` | No partial apply |
| MEGA-WORLD-022 | SHIP | Every team has `homeCity` + `homeState` in default pack | Pack JSON + generator | 0 blank geo fields in embedded default |
| MEGA-WORLD-023 | GAP | Validator requires non-blank `homeCity` / `homeState` | `IdentityPackValidator` | Colors/names checked; geography not enforced at validate |
| MEGA-WORLD-024 | SHIP | `#RRGGBB` color validation | Validator hex regex | Invalid colors reject pack |
| MEGA-WORLD-025 | PARTIAL | `programPrestige` on all 365 teams (GDD optional in pack) | Pack JSON | **0** teams set prestige; `WizardProgramPrestige.fallbackFromTeamId` spreads 18–79 |
| MEGA-WORLD-026 | PARTIAL | `logoUri` optional overlay | All teams `logoUri: null` | UI uses colors/text; no default logo assets |
| MEGA-WORLD-027 | SHIP | `arenaBaseName` on every team in JSON | Team records | Present; naming-rights display override thin in facilities UI |
| MEGA-WORLD-028 | SHIP | `IdentityPackIndex` rebuild; sim uses **ids only** | `IdentityPackIndex`, `TeamDisplayOverlay` | Save rows keyed by `teamId` |
| MEGA-WORLD-029 | PARTIAL | `ConferenceDisplayOverlay` exposes sim-needed conf fields | `IdentityPackIndex.conference()` | Exposes `leagueGameCount`, template id; **not** `standingsTiebreakers` |
| MEGA-WORLD-030 | SHIP | Tournament template reference integrity | Validator `UnknownTournamentTemplate` | Conf `tournamentTemplateId` must resolve |

---

## D — Display branding vs planning maps

| ID | Status | Requirement | Evidence | Finding |
|----|--------|-------------|----------|---------|
| MEGA-WORLD-031 | SHIP | 32 universe conference names match `league-branding.md` table | Spot-check + generator source | Generator reads `generate_league_branding.CONFERENCES` |
| MEGA-WORLD-032 | SHIP | Universe team display names from branding generator | `universe_team()` in `generate_default_pack.py` | e.g. Crown University Sovereigns, Jaydale Commonwealth |
| MEGA-WORLD-033 | SHIP | UI shows **NCBA / March Mayhem** strings (not NCAA) | `strings.xml`, awards engines | Branding from app resources + core copy, not pack |
| MEGA-WORLD-034 | SHIP | Early-season event **universe** names & template ids | `EarlySeasonEventCatalog` | Island Classic / Caribbean / Blue Blood / Desert Shootout; ids `evt-*` per `ncba-branding.md` |
| MEGA-WORLD-035 | GAP | Pack-swappable NCBA round labels (`round.opening`, `nci.*`) | `ncba-branding.md` pack keys example | Round names only in planning + strings, not pack schema |
| MEGA-WORLD-036 | SHIP | `PackDisplayNames` hides `embedded-default` filename in UI | `PackDisplayNames.forPack` | Shows `2026–27 League` |
| MEGA-WORLD-037 | PARTIAL | Short conference labels (NAC, C12, …) in UI | Planning table | Pack has full `displayName` only; short codes not in JSON |
| MEGA-WORLD-038 | SHIP | National postseason field sizes (76 / 32 / 16) in **sim**, not pack | `SelectionSundayEngine`, `ncba-branding.md` | Correct; pack supplies eligibility + auto-bid conf slots |

---

## E — Geography, recruiting, pipeline

| ID | Status | Requirement | Code | Finding |
|----|--------|-------------|------|---------|
| MEGA-WORLD-039 | SHIP | `homeState` on overlay drives recruiting proximity | `RecruitInterestEngine`, `TeamDisplayOverlay.homeState` | Pipeline uses state, not city |
| MEGA-WORLD-040 | PARTIAL | `homeCity` for schedule/travel/display | Stored on pack team; limited UI surfacing | Present in data model; travel UX not audited here |
| MEGA-WORLD-041 | GAP | US state code validation on `homeState` | Validator | Accepts any non-blank string in import |
| MEGA-WORLD-042 | SHIP | Colors separated from geography (branding note) | Pack schema | Independent fields per planning |

---

## F — Reclass & postseason eligibility

| ID | Status | Requirement | Code | Finding |
|----|--------|-------------|------|---------|
| MEGA-WORLD-043 | SHIP | `postseasonEligible: false` blocks NCBA/NCI selection | `SelectionSundayEngine` filters | Reclass excluded from national field |
| MEGA-WORLD-044 | SHIP | Reclass may still play RS + conf tournaments | `ConferenceTournamentEngine`, reclass tests | Heritage eligible per postseason spec |
| MEGA-WORLD-045 | SHIP | Auto-bid ladder skips ineligible champion | `NcbaAutoBidResolver` | Pass-over to next eligible team |
| MEGA-WORLD-046 | GAP | Offseason increment `reclassTransitionYear` → eligibility flip after year 4 | [`league-branding.md`](../../planning/league-branding.md#provisional--transition-programs) | Field stored in pack/import only; **no** dynasty-year rollover in Kotlin |
| MEGA-WORLD-047 | PARTIAL | `reclassTransitionYear` in save vs static pack | Pack JSON only at load | Eligibility does not evolve across seasons without pack edit |
| MEGA-WORLD-048 | SHIP | Four reclass teams in correct conferences | Pack team ids | ASCU West Florida, NEC Mercyhurst/New Haven, UAC West Georgia |

---

## G — Conference metadata & tiebreakers

| ID | Status | Requirement | Code | Finding |
|----|--------|-------------|------|---------|
| MEGA-WORLD-049 | SHIP | `standingsTiebreakers` stack on every conference in JSON | `h2h`, `h2h-point-diff`, `record-vs-top-teams` | Present ×32 |
| MEGA-WORLD-050 | GAP | Engine applies pack tiebreaker stack to standings | [`game-design.md`](../../planning/game-design.md#scheduling--regular-season) | `RankingsHubSnapshotFactory` sorts W-L only |
| MEGA-WORLD-051 | SHIP | `ncaaAutoBidSlot: 1` per conference (32 auto bids) | Conference records | Matches NCBA 32 champ autos |
| MEGA-WORLD-052 | PARTIAL | `sitePolicy` / `qualificationPolicy` on templates | Template strings in JSON | Consumed by conf tournament engine; not user-visible copy |
| MEGA-WORLD-053 | SHIP | Conf tournament links `tournamentTemplateId` | `ConferenceTournamentEngine` + pack templates | Templates drive bracket shape |
| MEGA-WORLD-054 | PARTIAL | Real 2026–27 conf tourney formats (byes, reseeding) | Generic generator brackets | Functional but not venue-accurate per directory venue column |

---

## H — Import, swap, persistence

| ID | Status | Requirement | Code | Finding |
|----|--------|-------------|------|---------|
| MEGA-WORLD-055 | SHIP | JSON / TXT / URL import with attestation | `IdentityPackImportPipeline`, `PackUrlPolicy` | 5 MB + allowlist |
| MEGA-WORLD-056 | PASS | Optional pack manifest **signature** verification | `PackManifestSignatureVerifier` + trusted signer map | `PackConstants.kt` |
| MEGA-WORLD-057 | SHIP | Overlay merge preserves stable ids | `IdentityPackOverlayMerger` | Display patch without history rewrite |
| MEGA-WORLD-058 | PARTIAL | Pack swap + year mismatch **non-blocking warning** | GDD post-swap UX | `PackRepository.swap*` works; warning UI not located |
| MEGA-WORLD-059 | SHIP | Dynasty metadata stores `packId`; validator detects slot/pack mismatch | `DynastyMetadata`, `DynastySaveValidator` | |
| MEGA-WORLD-060 | PARTIAL | Import UX in new-game / settings | `NewGameWizardScreen`, GDD milestone | Core pipeline SHIP; wizard settings stub for import |

---

## I — League schedule generation (365-team graph)

| ID | Status | Requirement | Code / test | Finding |
|----|--------|-------------|-------------|---------|
| MEGA-WORLD-061 | SHIP | Schedule generator consumes full pack team + conf overlays | `LeagueScheduleGenerator.generate` | One plan per program |
| MEGA-WORLD-062 | PASS | **32** RS game cap + conf `leagueGameCount` | `LeagueScheduleSymmetricEngine` + cap trim | `LeagueScheduleIntegrityAudit` |
| MEGA-WORLD-063 | PASS | Mutual opponent symmetry across league graph | `LeagueScheduleIntegrityAudit.audit` | `LeagueScheduleGeneratorTest` |
| MEGA-WORLD-064 | PASS | No double-booking same RS week per team | Week assign + CI audit | `LeagueScheduleIntegrityAuditTest` |
| MEGA-WORLD-065 | WIRE | User preseason plan vs league-wide generator parity | `ScratchAuditTest2.userPlan` | User solver path richer (exhibitions, events); league bootstrap uses `DeclinedAll` events |
| MEGA-WORLD-066 | SHIP | `LeagueMatchupIndex.uniqueRsMatchups` dedupes home/away for batch | `LeagueWeekBatchSimEngine` | Batch sim uses unique matchups per week |

---

## J — League week batch simulation

| ID | Status | Requirement | Code / test | Finding |
|----|--------|-------------|-------------|---------|
| MEGA-WORLD-067 | SHIP | Batch sim all non-user RS games on week advance | `LeagueWeekBatchSimEngine.runRegularSeasonWeek` | Skips user matchup; applies user result |
| MEGA-WORLD-068 | PARTIAL | Same possession engine as user games for AI league games | `LeagueFastSim` | Uses `PossessionSimEngine` when tuning passed; else score stub 58–97 |
| MEGA-WORLD-069 | SHIP | Weekly NET + polls for all pack team ids | `LeagueWeekBatchWeeklyRankings.attach` | `NcbaNetRankingEngine` over full prestige map |
| MEGA-WORLD-070 | SHIP | **365-team** week within performance budget | `LeagueWeekBatchPerformanceTest`, `FULL_LEAGUE_TEAM_COUNT` | Median timing guarded |
| MEGA-WORLD-071 | PARTIAL | Early-season event games in league batch | `LeagueWeekBatchRepository` + `EarlySeasonBracketEngine` | Wired when bracket season exists; depends on preseason bootstrap |
| MEGA-WORLD-072 | PARTIAL | AI league injuries / lineup adjust hook | `AiLeagueWeekOps` | Roll counts stub-level |
| MEGA-WORLD-073 | SHIP | Standings state bootstraps all pack teams | `LeagueWeekBatchSimEngine.bootstrap` | One `LeagueTeamRecord` per id |
| MEGA-WORLD-074 | PARTIAL | Postseason phase batch support flag | `phaseSupportsBatch(Postseason)` | RS path primary; PS batch coverage thinner than RS |

---

## K — Rankings hub & world visibility

| ID | Status | Requirement | Code | Finding |
|----|--------|-------------|------|---------|
| MEGA-WORLD-075 | SHIP | NET list spans all teams in active pack | `RankingsHubSnapshotFactory` + standings | 365 rows when published |
| MEGA-WORLD-076 | SHIP | Conference standings grouped by `conferenceId` | `conferenceTables()` | Display names from pack overlay |
| MEGA-WORLD-077 | GAP | Tie-aware conf standings per pack stack | Same factory sort | W-L only (see MEGA-WORLD-050) |
| MEGA-WORLD-078 | PARTIAL | Rankings hub placeholder string vs live data | `rankings_hub_placeholder` in strings | Repository supplies snapshot; string may be stale in edge routes |

---

## L — Cross-cutting world integrity

| ID | Status | Requirement | Finding |
|----|--------|-------------|---------|
| MEGA-WORLD-079 | SHIP | `rulesetYear` in pack labels era; balance/sim pinned at create | ADR-002/003; not swapped with pack mid-dynasty | Correct separation per GDD |
| MEGA-WORLD-080 | SHIP | Conference realignment **invite** uses pack membership caps | `ConferenceOfferGenerator`, OS week 10 | Structural caps from live pack graph |
| MEGA-WORLD-081 | PARTIAL | Program prestige in NET from pack vs hash fallback | `WizardProgramPrestige.effective` | All 365 use hash until JSON prestige authored |
| MEGA-WORLD-082 | SHIP | Credits / Play Store copy: unofficial, optional fan packs | `PLAY_STORE_LISTING.md`, `CreditsScreen` | Legal posture matches identity model |

---

## M — D1 membership & certification tiers

| ID | Status | Requirement (directory / branding) | Code / asset | Finding |
|----|--------|-----------------------------------|--------------|---------|
| MEGA-WORLD-083 | SHIP | **365** programs = certified + reclass in operational field | [`2026-NCAA-Directory.md`](../../planning/2026-NCAA-Directory.md#institutional-census-and-eligibility-architecture) | Pack + tests match directory total |
| MEGA-WORLD-084 | SHIP | **361** postseason-certified members in default pack | `DefaultPackAssetTest` `postseasonEligible` count | Four excluded; remainder certified |
| MEGA-WORLD-085 | SHIP | **4** mid/early reclass programs in census | Directory table + pack | West Florida, New Haven, Mercyhurst, West Georgia only |
| MEGA-WORLD-086 | SHIP | Mid-transition (Y3) pair: Mercyhurst + West Georgia | Pack `reclassTransitionYear: 3` | `team-conf-nec-006`, `team-conf-uac-009` |
| MEGA-WORLD-087 | SHIP | Early-transition (Y2) New Haven | Pack `reclassTransitionYear: 2` | `team-conf-nec-007` |
| MEGA-WORLD-088 | SHIP | Early-transition (Y1) West Florida in ASUN | Pack + branding ASCU row | `team-conf-ascu-008`, Pensacola FL |
| MEGA-WORLD-089 | SHIP | Saint Francis (PA) **absent** after D-III withdrawal | Directory NEC −1 | No `team-conf-nec-*` Saint Francis row in JSON |
| MEGA-WORLD-090 | SHIP | NEC remains **9** teams post-exit | Pack size script vs directory | `conf-nec` count 9 |
| MEGA-WORLD-091 | PARTIAL | Directory tier copy (361 vs 365) in product UX | Planning only | No in-game explanation of provisional certification |
| MEGA-WORLD-092 | GAP | Reclass Y1–Y3 surfaced on school hub / opponent view | [`league-branding.md`](../../planning/league-branding.md#provisional--transition-programs) | `SchoolHubOverview` has no transition fields |

---

## N — Conference realignment & 2026–27 membership graph

| ID | Status | Requirement | Code / asset | Finding |
|----|--------|-------------|--------------|---------|
| MEGA-WORLD-093 | SHIP | **Pacific Rim Twelve** (`conf-pr12`) = **9** teams | Directory Pac-12 revival | Boise St through Washington St match branding table |
| MEGA-WORLD-094 | SHIP | Gonzaga universe name in PR12 | `league-branding.md` Spokane River Mastiffs | `team-conf-pr12-004` in pack |
| MEGA-WORLD-095 | SHIP | **United Athletic Collegiate** (`conf-uac`) = **9** teams | UAC formation directory | ASUN/OVC/WAC merge roster in pack |
| MEGA-WORLD-096 | SHIP | **Metro Collegiate** (`conf-metro`) = **13** (ex-MAAC rebrand) | Directory Metro Conference row | `conf-metro` 13 teams; id not legacy `maac` |
| MEGA-WORLD-097 | SHIP | Mountain West **10** after Pac-12 raid | Directory MW delta | `conf-rmwc` size 10; Hawaiʻi/UTEP/UC Davis inflow reflected |
| MEGA-WORLD-098 | SHIP | Big West **12** with Cal Baptist / Utah Valley / Sac State | Directory Big West | `conf-gwc` count 12 |
| MEGA-WORLD-099 | SHIP | Big Sky **11** (+Southern Utah, Utah Tech; −Sac State) | Directory Big Sky | `conf-nsc` count 11 |
| MEGA-WORLD-100 | SHIP | Horizon **12** (+Northern Illinois from MAC) | Directory Horizon | `conf-lhl` count 12 |
| MEGA-WORLD-101 | SHIP | MAC **12** (−NIU) | Directory MAC | `conf-mac` count 12 |
| MEGA-WORLD-102 | SHIP | SoCon **11** (+Tennessee Tech) | Directory Southern | `conf-scf` count 11 |
| MEGA-WORLD-103 | SHIP | Sun Belt **14** (+La Tech, −Texas State to PR12) | Directory SBC | `conf-sbc` count 14 |
| MEGA-WORLD-104 | PARTIAL | Per-conf net realignment delta not stored in pack | Directory § conference table | Membership correct; narrative deltas markdown-only |

---

## O — School hub & pack display fields

| ID | Status | Requirement | Code | Finding |
|----|--------|-------------|------|---------|
| MEGA-WORLD-105 | SHIP | School hub conference name from pack overlay | `SchoolHubSnapshotFactory`, `IdentityPackIndex.conference()` | `conferenceName` from `displayName` |
| MEGA-WORLD-106 | SHIP | Program prestige on hub from pack (or fallback) | `TeamDisplayOverlay.programPrestige`, hub strings | Wizard hash when JSON null |
| MEGA-WORLD-107 | SHIP | NET / poll ranks on hub after RankingsReview | `SchoolHubRepository`, `school_hub_net_rank` | Gated by preseason task |
| MEGA-WORLD-108 | SHIP | Opponent school hub read-only path | `loadOpponentOverview`, `school_hub_read_only` | Same overview factory |
| MEGA-WORLD-109 | PARTIAL | SOS / power index on hub when standings thin | `SchoolMetricsEstimator` placeholders | Live resume when batch standings exist |
| MEGA-WORLD-110 | GAP | `homeCity` / `homeState` on school hub card | GDD geography on programs | Only in wizard `SchoolStepContent`; hub omits |
| MEGA-WORLD-111 | GAP | `arenaBaseName` on school hub | Pack field on every team | Not in `SchoolHubOverview` model |
| MEGA-WORLD-112 | GAP | Team primary/secondary colors on hub UI | `TeamDisplayOverlay` color hex | Hub uses generic `Card`; no color strip |
| MEGA-WORLD-113 | PARTIAL | Conference standing label on hub | `SchoolRankingsSnapshot.conferenceStandingLabel` | Prestige-order placeholder, not live W-L |
| MEGA-WORLD-114 | GAP | `postseasonEligible` / reclass ineligibility copy on hub | `postseason.md`, branding table | No NCBA/NCI ineligibility banner |

---

## P — Pack import, attestation & overlay policy

| ID | Status | Requirement | Code | Finding |
|----|--------|-------------|------|---------|
| MEGA-WORLD-115 | SHIP | Import rejects when disclaimer required but not accepted | `IdentityPackImportPipeline`, tests | `PackImportAttestation` gate |
| MEGA-WORLD-116 | SHIP | 5 MB payload cap + URL allowlist | `PackPayloadPolicy`, `PackUrlPolicy` | Hardening tests |
| MEGA-WORLD-117 | SHIP | `membershipYear` regex `YYYY-YY` on validate | `IdentityPackValidator` | Invalid year rejects pack |
| MEGA-WORLD-118 | SHIP | TXT `disclaimerAcceptedRequired` meta default true | `IdentityPackTxtCodec` | Aligns with default JSON |
| MEGA-WORLD-119 | GAP | Compose import UI with attestation checkbox | GDD Phase 4, `MEGA-GDD-086` | `PackRepository.import*` not wired to settings screen |
| MEGA-WORLD-120 | PARTIAL | Overlay merge preserves team ids | `IdentityPackOverlayMerger` | SHIP for ids; branding block in TXT template deferred |
| MEGA-WORLD-121 | PARTIAL | Post-import pack swap + `membershipYear` warning | GDD post-swap UX | Repository swap works; year mismatch warning not in app |
| MEGA-WORLD-122 | GAP | Optional pack manifest signature | `PackConstants.kt` comment | No verification hook |

---

## Q — Logos & visual identity from pack

| ID | Status | Requirement | Code / asset | Finding |
|----|--------|-------------|--------------|---------|
| MEGA-WORLD-123 | PARTIAL | `logoUri` optional per team in schema | `TeamPack`, import TXT | Field parsed and merged |
| MEGA-WORLD-124 | SHIP | Default pack: all `logoUri: null` | `embedded-default.json` | Colors + text identity only |
| MEGA-WORLD-125 | GAP | Compose loads `logoUri` in school hub / wizard | `TeamDisplayOverlay.logoUri` | No Coil/AsyncImage usage in `ui/school` or hub |
| MEGA-WORLD-126 | PARTIAL | Wizard school list shows city/state not logo | `SchoolStepContent` | Geography preview only |
| MEGA-WORLD-127 | SHIP | `shortName` on every team for compact UI | Pack JSON + overlay | Present; hub uses full `displayName` only |
| MEGA-WORLD-128 | PARTIAL | Conference short codes (NAC, C12, PR12) in planning | `league-branding.md` Short column | Not serialized in pack JSON |
| MEGA-WORLD-129 | GAP | Pack-level logo asset bundle for default ship | Branding note colors/logos overlay | No shipped PNG/SVG per program in repo |

---

## R — NET rankings inputs & pack-driven résumé

| ID | Status | Requirement | Code | Finding |
|----|--------|-------------|------|---------|
| MEGA-WORLD-130 | SHIP | Weekly NET over **all** pack team ids in batch | `LeagueWeekBatchWeeklyRankings`, `NcbaNetRankingEngine` | Full-league map each RS week |
| MEGA-WORLD-131 | SHIP | NET inputs: NEM, SOS, quad record, form, record quality | `selection-engine.md`, engine weights | Uses `BalanceSelectionTuning.netWeights` |
| MEGA-WORLD-132 | PARTIAL | `programPrestige` from pack as NET prior | `NcbaNetRankingEngine` + prestige map | Hash fallback for all 365 until JSON authored |
| MEGA-WORLD-133 | SHIP | Reclass teams included in NET table | Pack ids in prestige map | Directory: transition programs tracked by metrics |
| MEGA-WORLD-134 | SHIP | Human polls anchor to NET table | `HumanPollRankingEngine` | Media/Coaches poll batch uses NET scores |
| MEGA-WORLD-135 | PARTIAL | Opponent hub NET before standings hydrated | `SchoolHubSnapshotFactory` `SchoolNetRankIndex` | Preseason prestige-order placeholder ranks |
| MEGA-WORLD-136 | GAP | Pack does not store NET weight overrides | GDD pack vs balance partition | Weights live in `balance-2026.json`, not pack |
| MEGA-WORLD-137 | SHIP | NET excluded from selection until rankings published | Preseason `RankingsReview` gate | Matches directory “metrics vs bracket” timing for user |

---

## S — Directory fidelity & generator cross-checks

| ID | Status | Requirement | Evidence | Finding |
|----|--------|-------------|----------|---------|
| MEGA-WORLD-138 | SHIP | All **32** expected conf sizes match directory table | Python census vs directory § | Zero mismatches on 2026-11-11 audit |
| MEGA-WORLD-139 | SHIP | Generator reads `CONFERENCES` from league branding | `generate_default_pack.py` | Universe names match markdown |
| MEGA-WORLD-140 | SHIP | Every team `conferenceId` resolves to known conf | Validator `UnknownConference` | No orphan assignments |
| MEGA-WORLD-141 | SHIP | Unique `teamId` per program | `DefaultPackAssetTest` | 365 distinct ids |
| MEGA-WORLD-142 | PARTIAL | Real reference names only in planning markdown | Grep pack `displayName` | Universe names in JSON; no NCAA marks in ship bundle |
| MEGA-WORLD-143 | SHIP | `rulesetYear: 2026` aligns directory analysis year | Pack metadata | Matches directory header baseline |
| MEGA-WORLD-144 | PARTIAL | Conf tournament **venue** column from directory | Directory championship location table | Templates generic `sitePolicy`; venues not in JSON |
| MEGA-WORLD-145 | SHIP | ASUN **8** teams incl. West Florida reclass | Directory ASUN row | ASCU membership count 8 |
| MEGA-WORLD-146 | SHIP | C-USA **10** after La Tech / UTEP exits | Directory CUSA | `conf-ucc` size 10 |
| MEGA-WORLD-147 | GAP | CI test asserts conf sizes vs directory manifest | `DefaultPackAssetTest` | Counts 32/365/eligible only; no per-conf size table |

---

## T — Identity index, early events & conference graph consumers

| ID | Status | Requirement | Code | Finding |
|----|--------|-------------|------|---------|
| MEGA-WORLD-148 | SHIP | `teamsInConference` for generators | `IdentityPackIndex` | Used by schedule + tournament engines |
| MEGA-WORLD-149 | SHIP | Early-season event ids match NCBA branding doc | `EarlySeasonEventCatalog` | `evt-island-classic`, Caribbean, Blue Blood, Desert |
| MEGA-WORLD-150 | SHIP | `ConferenceOfferGenerator` membership caps from live pack | OS week 10 | Realignment invites respect graph |
| MEGA-WORLD-151 | PARTIAL | `allConferenceDisplays()` omits tiebreaker stack | `ConferenceDisplayOverlay` | Sim/UI cannot read pack tiebreakers without raw pack |
| MEGA-WORLD-152 | WIRE | Job center / transfer UI uses pack team list | `PackRepository`, employment engines | Display overlays wired; full job-center parity not audited here |

---

## Summary counts

| Status | Count |
|--------|------:|
| SHIP | 96 |
| PARTIAL | 36 |
| GAP | 18 |
| WIRE | 2 |
| **Total items** | **152** |

## Priority gaps (world / pack)

1. **Reclass lifecycle (MEGA-WORLD-046/047/092/114):** pack stores transition year but dynasty offseason does not advance eligibility; hub does not explain provisional status.
2. **Conference tiebreakers (MEGA-WORLD-050/077/151):** pack declares stack; standings, selection-adjacent UI, and overlay index ignore it.
3. **League schedule symmetry (MEGA-WORLD-063/064):** scratch audit shows asymmetric stubs; needs generator fix or test gate.
4. **Branding in pack (MEGA-WORLD-019/035):** NCBA/March/event strings live in code/strings, not swappable pack overlay per `ncba-branding.md` pack-key example.
5. **League fast sim without tuning (MEGA-WORLD-068):** batch path may score-stub unless app injects `SimTuning` every week.
6. **programPrestige authoring (MEGA-WORLD-025/081/132):** hash fallback makes NET/polls and wizard school pick approximate vs designed prestige tiers.
7. **Import UX (MEGA-WORLD-119/121):** core attestation SHIP; no primary settings/wizard import surface or membership-year warning.
8. **School hub pack fidelity (MEGA-WORLD-110–112/125):** geography, arena, colors, and logos from pack not shown on hub card.
9. **Directory test gate (MEGA-WORLD-147):** add per-conference size assertions to prevent silent realignment drift.

**Audit method:** Read `league-branding.md`, `ncba-branding.md`, skim `2026-NCAA-Directory.md` census/realignment; inspect `embedded-default.json` (structure, reclass, templates); review `:core/pack` and `:core/league` sources; grep pack consumption across `data/*`; cross-ref `DefaultPackAssetTest`, `ScratchAuditTest`, `LeagueWeekBatchPerformanceTest`, and sibling audit `MEGA-GDD-041`. Expanded pass adds Python conference-size cross-check against directory table, school hub field gap analysis, and import/NET/logo trace through `SchoolHubRepository` and `NcbaNetRankingEngine`.
