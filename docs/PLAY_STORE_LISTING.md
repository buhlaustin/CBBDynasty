# Play Store listing — Dynasty: College Basketball

**Canonical copy:** [`PlayStoreListingCopy.kt`](../android/core/src/main/kotlin/dev/novux/dynasty/cbb/core/legal/PlayStoreListingCopy.kt) (CI-tested)  
**Branding:** [`planning/branding.md`](../planning/branding.md)  
**Privacy:** [`PRIVACY_POLICY.md`](PRIVACY_POLICY.md)

## Listing fields

| Field | Value |
|-------|--------|
| **App name** | **Dynasty: College Basketball** |
| **Short description** | See [Short description](#short-description) (≤ 80 characters) |
| **Full description** | See [Full description](#full-description) |
| **Privacy policy URL** | `https://novux.dev/privacy/dynasty-college-basketball` |
| **Category** | Games → Simulation |
| **Content rating** | Complete IARC questionnaire (no real-money gambling; fictional sports) |

### Short description

Paste into Play Console (**Store presence → Main store listing → Short description**):

```
Run a 365-team college hoops dynasty offline. Recruit, NIL, March.
```

(58 characters — validated by `PlayStoreListingCopyTest`.)

### Full description

Paste into Play Console (**Full description**). Line breaks and `•` bullets are supported.

```
Dynasty: College Basketball is a year-round head-coach sim from Novux Games—unofficial, offline-first, and built for phone play.

YOUR PROGRAM, EVERY SEASON
• Staff, schemes, and player development across offseason, preseason, and the calendar
• High school and portal recruiting, NIL offers, and Signing Day closes
• Schedule craft, weekly NET and polls, conference tournaments, and résumé building
• Selection Sunday, Opening Round, March Mayhem, and national brackets

FULL LEAGUE
• A 365-team universe sims around your school while you play or fast-forward your games
• Text-based possession sim—watch the feed or sim to the final score
• News, awards, history, and a dashboard that keeps the job feeling alive year-round

OFFLINE SAVES
• Progress stays on your device; no account required
• Optional one-time HTTPS pack import can rename the league; save IDs stay stable

Not affiliated with the NCAA or any real school or conference. Custom identity packs are optional fan overlays only.
```

## Offline & legal (must appear in listing or “Data safety”)

| Topic | Copy |
|-------|------|
| Offline play | Core dynasty play is fully offline. Saves stay on your device. Network access is only used when you optionally import a league identity pack from an HTTPS URL. |
| Unofficial / no endorsement | Dynasty: College Basketball is an unofficial college basketball simulation. Novux Games is not affiliated with, endorsed by, or sponsored by the NCAA, any conference, or any school. |
| Design lock | **No implied NCAA or school endorsement** ([`game-design.md`](../planning/game-design.md)) |

In-app mirrors: `CreditsScreen` strings `credits_offline_play`, `credits_legal_disclaimer`, `credits_privacy_policy`.

## Graphic assets

| Asset | Spec | Repo source |
|-------|------|-------------|
| **App icon** | Adaptive icon layers | Regenerate: `python3 planning/tools/generate_adaptive_icon.py` → syncs [`Public-Assets/adaptive-icon/`](../Public-Assets/adaptive-icon/) + `android/.../ic_launcher_foreground.png` (checklist **1.5**) |
| **Feature graphic** | 1024×500 PNG/JPG | Compose: `#181a25` background + [`Public-Assets/app-logo.svg`](../Public-Assets/app-logo.svg) + title lockup (export at ship) |
| **Phone screenshots** | Min 2; 16:9 or device frames | [`Public-Assets/screen-previews/splash-screen.png`](../Public-Assets/screen-previews/splash-screen.png), [`studio-screen.png`](../Public-Assets/screen-previews/studio-screen.png); add in-dynasty shots before submission |
| **Promo video** | Optional | — |

## Data safety form (Google Play)

Declare:

- **No data collected** for core offline play (align with `PRIVACY_POLICY.md`).
- **Optional network** for user-initiated pack URL import only.
- Dynasty files **not shared** with third parties by the app.

## Pre-submission checklist

- [ ] Host privacy policy at `PRIVACY_POLICY_URL` (or update constant + this doc).
- [ ] Short description ≤ 80 characters (`PlayStoreListingCopyTest`).
- [ ] Feature graphic exported to `docs/store/exports/` or Play Console upload buffer (not committed if large binaries preferred).
- [ ] Store listing proofread for banned endorsement language (no “official NCAA”).

## Document history

| Date | Change |
|------|--------|
| 2026-11-10 | Initial listing copy, asset map, privacy cross-link (checklist **16.5**) |
| 2026-11-10 | Short/full descriptions refreshed for 365-team league, NIL, March, offline saves |
