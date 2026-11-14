# Google Play open testing — fresh install (MEGA-QA-135)

**Gate:** First-time install from the **open testing** track must cold-launch through studio (if enabled) and reach **splash / home** without crash.

Constants and checklist ids: `PlayOpenTestInstallPath` (`:core`).

## Play Console setup (once per release)

1. Upload signed AAB from [Android release bundle](../.github/workflows/android-bundle-release.yml) (workflow_dispatch or version tag).
2. **Testing → Open testing** → create release with the new AAB; roll out to testers.
3. Confirm **version code** increased vs prior open-test build ([MEGA-QA-136]).
4. Ensure at least one Gmail tester is on the open-testing list.

## Tester script (MEGA-QA-135)

| # | Step | Pass |
|---|------|------|
| 1 | Uninstall any existing `com.dynasty.cbb` sideload | Clean package state |
| 2 | Open Play Store link for open testing → **Install** | Install from Play (not APK) |
| 3 | Launch from launcher | No immediate crash |
| 4 | Complete **Novux Games** studio intro if shown | Reaches title / **Tap to Play** |
| 5 | **Tap to Play** | Home menu (New Game, Load, Settings) |

Capture screenshot or screen recording for release notes if anything fails.

## Automation (BOT) — binary readiness

CI cannot install from Play; it verifies the **same AAB-derived binary** is launchable:

```bash
bash scripts/build_release_universal_apk.sh
bash scripts/verify_play_fresh_install_launch.sh
```

Checks: `launchable-activity` = `MainActivity`, package `com.dynasty.cbb`.

Workflow: [`.github/workflows/android-release-universal-smoke.yml`](../.github/workflows/android-release-universal-smoke.yml).

## MEGA-QA-136 — Update over prior open-test build

| # | Step | Pass |
|---|------|------|
| 1 | Keep a dynasty in progress on **prior** open-test APK | Occupied slot |
| 2 | Publish new AAB with **`versionCode` >** `android/play_open_test_baseline_version_code.txt` | Play accepts upload |
| 3 | Play Store **Update** on tester device | Installs without parse/signing errors |
| 4 | Launch → **Continue** | Same dynasty loads; no migration crash |
| 5 | Sim week or open shell | Calendar/metadata coherent |

After each Play promote, bump `play_open_test_baseline_version_code.txt` to the **shipped** `versionCode`.

**Automation (BOT):** `verify_play_update_version_bump.sh`; `:database:testDebugUnitTest` occupied-slot migration; `SaveLoadAfterUpdateFixtureTest`.

## MEGA-QA-137 — Signed AAB integrity

Before upload, confirm the release bundle is signed and structurally valid (avoids `INSTALL_PARSE_FAILED` on Play-distributed builds):

```bash
cd android && ./gradlew :app:bundleRelease
bash ../scripts/build_release_universal_apk.sh   # optional, for apksigner pass
bash ../scripts/verify_play_aab_signing.sh
```

Play Console: **App integrity → Play App Signing** enabled; upload key matches GitHub Actions secrets when using production signing.

**Automation (BOT):** `verify_play_aab_signing.sh`; `PlaySignedAabIntegrityPath`.

## MEGA-QA-146 — Open test 30-minute session

**Who:** Gmail accounts on the **open testing** roster (not internal-only).

**Goal:** Install from Play and run ~**30 minutes** of dynasty play **without** Google account sign-in (offline-first product).

| Phase | Activity | Budget |
|-------|----------|--------|
| Roster | Tester email enrolled in Console | 2 min |
| Install | Play Store open-track install/update | 3 min |
| Launch | Past studio/splash → home | 5 min |
| Dynasty | New game or Continue | 10 min |
| Hubs | Recruiting / Rankings / News round-trip | 5 min |
| Advance | ≥2 Sim Weeks | 5 min |

Print script for QA lead: `bash scripts/print_play_open_test_session_checklist.sh`.

Constants: `PlayOpenTestThirtyMinuteSessionPath`.

**Automation (BOT):** session plan unit test; no Play API in CI — rostered human session still required for REL sign-off.

## Related

- Sideload parity: [`RELEASE_SIDELOAD_SMOKE.md`](RELEASE_SIDELOAD_SMOKE.md) (MEGA-QA-134)
- Pre-launch matrix: [`PLAY_PRE_LAUNCH_MATRIX.md`](PLAY_PRE_LAUNCH_MATRIX.md)
- Listing copy: [`PLAY_STORE_LISTING.md`](PLAY_STORE_LISTING.md)
