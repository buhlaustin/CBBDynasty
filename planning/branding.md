# Branding — Dynasty: College Basketball

**Last updated:** 2026-11-11

---

## Product identity

| Field | Value |
|-------|--------|
| **Game title** | **Dynasty: College Basketball** |
| **Repo / code name** | `CBBDynasty` (workspace) |
| **Studio** | **Novux Games** |
| **Studio URL** | [https://novux.dev](https://novux.dev) |

Use **Dynasty: College Basketball** on store listings, splash, and in-app title bar unless a shorter label is required (e.g. “Dynasty CBB” for icon-safe text — TBD).

---

## Logo & assets

| Asset | Path | Use |
|-------|------|-----|
| **Primary app logo** | [`Public-Assets/app-logo.svg`](../Public-Assets/app-logo.svg) | In-app header, marketing, **game splash** |
| **Splash screen reference** | [`Public-Assets/screen-previews/splash-screen.png`](../Public-Assets/screen-previews/splash-screen.png) | Marketing / QA mock (1080×2400) |
| **Novux logos** | `Public-Assets/Novux_Logos/` | **Studio screen** lockups (`2.svg` = light on `#181a25`) |
| **Studio screen reference** | [`Public-Assets/screen-previews/studio-screen.png`](../Public-Assets/screen-previews/studio-screen.png) | Marketing / QA mock (1080×2400) |
| **Novux icons** | `Public-Assets/Novux_Icons/` | Studio mark variants |

**Implementation notes**

- Prefer **vector** `app-logo.svg` for scalable UI; generate raster mipmaps for Android as needed.
- Keep logo **clear space** ≥ ½ logo height on marketing; min touch target unrelated (UI chrome).

---

## Color

| Role | Hex | Use |
|------|-----|-----|
| **App / adaptive icon background** | `#181a25` | Android adaptive icon background layer, default **game chrome** backdrop (menus, loading, empty states) |

**`#181a25`** — dark blue-gray; primary **brand field**. Pair with light text and accent colors when UI palette doc exists.

Suggested contrast (for implementation QA):

- Body text on `#181a25`: **#FFFFFF** or **#E8EAED** primary; **#A8AEB8** secondary.
- Accent / CTA: define in UI theme doc (not locked here).

---

## Adaptive icon (Android)

Android adaptive layers in [`Public-Assets/adaptive-icon/`](../Public-Assets/adaptive-icon/):

| File | Role |
|------|------|
| `adaptive-icon-background.svg` | Background layer — `#181a25` |
| `adaptive-icon-foreground.png` | Foreground layer — transparent logo (432×432); mark centered in **safe zone** |
| `adaptive-icon-canvas.png` | `#181A25` + `adaptive-icon-foreground.png` composited (QA preview) |

**Safe zone (design lock):** On the 108×108 dp adaptive layer, logo art must fit the central **~66 dp** circle. Generator scales `app-logo.svg` to **58%** of 432 px and centers it — do not ship a full-bleed 432 px raster.

**Generate & sync (source of truth → Android):**

```bash
python3 planning/tools/generate_adaptive_icon.py
# or: bash scripts/sync_android_brand_assets.sh
```

Requires `pip install cairosvg pillow` (maintainer / CI only). CI **check**: `python3 planning/tools/generate_adaptive_icon.py --check`.

**Android:** copy of `adaptive-icon-foreground.png` → `android/app/.../drawable-nodpi/ic_launcher_foreground.png`; background `@color/brand_background` (`#181A25`). Adaptive XML references the foreground **directly** (no runtime inset hacks).

---

## Legal / attribution

- Repository: **open code / closed art** — [`docs/LICENSING.md`](../docs/LICENSING.md), [`Public-Assets/LICENSE`](../Public-Assets/LICENSE).
- Splash footer + Home **Credits**: **© Novux Games**, [novux.dev](https://novux.dev) (`CreditsScreen`).
- Game is **unofficial** college basketball sim (see [`game-design.md`](game-design.md) identity packs).

---

## Related planning

| Doc | Link |
|-----|------|
| Game systems | [`game-design.md`](game-design.md) |
| In-season UI | [`game-frame.md`](game-frame.md) |
| League names (real ↔ universe) | [`league-branding.md`](league-branding.md) · reference [`2026-NCAA-Directory.md`](2026-NCAA-Directory.md) |
| NCBA / March Mayhem / early events | [`ncba-branding.md`](ncba-branding.md) |
| Postseason brackets & Selection Sunday | [`postseason.md`](postseason.md) |
| Studio & splash | [`app-shell.md`](app-shell.md) |

---

## UI copy (in-game)

**Product voice:** coach / fan facing — never engineering, checklist, or repo jargon.

| Rule | Detail |
|------|--------|
| **Single source** | All on-screen text in `android/app/src/main/res/values/strings.xml` (no hard-coded `Text("…")` in Compose). |
| **Internal IDs** | Never show `packId`, file paths, slot indices in error prose, schema versions, or feature-phase names. |
| **League identity** | [`PackDisplayNames`](../android/core/src/main/kotlin/dev/novux/dynasty/cbb/core/pack/PackDisplayNames.kt) for league labels (e.g. **2026–27 League**), not “default pack”. |
| **Banned tone** | bootstrap, scaffold, stub, fallback, MVP, placeholder, unavailable, unnamed, transaction, “ships later”, “app shell”, TBD in player strings. |
| **CI** | `python3 scripts/lint_player_strings.py` on every core-test workflow run. |

Planning docs and code comments may use technical terms; **the APK may not**.

---

## Document history

| Date | Change |
|------|--------|
| 2025-11-08 | Initial branding: title, logo path, studio, `#181a25` |
| 2025-11-08 | Official title: **Dynasty: College Basketball** |
| 2025-11-08 | Link `ncba-branding.md` |
| 2025-11-09 | Novux Games; `app-shell.md` studio vs splash |
| 2025-11-09 | UI copy rules + `lint_player_strings.py` |
| 2026-11-11 | Adaptive icon generator + safe-zone lock; `planning/tools/generate_adaptive_icon.py` |
