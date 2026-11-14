# Play pre-launch matrix (smoke)

Manual and CI smoke paths before Play open testing. Primary gate: **MEGA-QA-119** (API 34 emulator).

## MEGA-QA-119 — Pixel 6 API 34 x86_64

| Step | Action | Pass criteria |
|------|--------|----------------|
| 1 | Install `:app:assembleDebug` on **Pixel 6**, **API 34**, **google_apis**, **x86_64** | APK installs without error |
| 2 | Cold launch → dismiss studio intro if shown → **Tap to Play** → home | No crash; home menu visible |
| 3 | **New game** → complete wizard → **Sign** | Dynasty shell/offseason loads; save slot occupied |
| 4 | Advance through **offseason / signing** as required → **preseason** → **Start season** | Enters in-season shell |
| 5 | Confirm header shows **Regular Season week 1** (or exhibition then RS per schedule) | `CalendarPhase.RegularSeason` week index ≥ 1 |

**Automation (BOT):**

- GitHub Actions: [`.github/workflows/android-emulator-api34-smoke.yml`](../.github/workflows/android-emulator-api34-smoke.yml) — `connectedDebugAndroidTest` on API 34.
- JVM path check: `PlayPreLaunchSmokePathTest` — documents milestone order to RS W1.

**Local:**

```bash
bash scripts/install_android_sdk.sh
bash scripts/install_android_emulator_api34.sh
cd android && ./gradlew :app:assembleDebug
# Start AVD, then:
./gradlew :app:connectedDebugAndroidTest
```

## MEGA-QA-127 — Physical device (Android 13+, 6 GB)

See [`PHYSICAL_DEVICE_SMOKE.md`](PHYSICAL_DEVICE_SMOKE.md) for the retail device script (new game → four RS sim weeks).

**Automation (BOT):** `FourRegularSeasonWeeksDeviceSmokeTest` mirrors league sim + week checkpoint for four RS weeks.

## MEGA-QA-134 — Release sideload parity

See [`RELEASE_SIDELOAD_SMOKE.md`](RELEASE_SIDELOAD_SMOKE.md) — `bundleRelease` → universal APK → repeat MEGA-QA-119 smoke on device.

**Automation (BOT):** `android-release-universal-smoke.yml`; `ReleaseSideloadSmokePath`.

## MEGA-QA-135 — Play open testing (fresh install)

See [`PLAY_OPEN_TESTING_SMOKE.md`](PLAY_OPEN_TESTING_SMOKE.md) — install from **Open testing** track; launch past studio/splash.

**Automation (BOT):** `verify_play_fresh_install_launch.sh` on release universal APK; `PlayOpenTestInstallPath`.

## MEGA-QA-136 — Play update over prior build

See [`PLAY_OPEN_TESTING_SMOKE.md`](PLAY_OPEN_TESTING_SMOKE.md#mega-qa-136--update-over-prior-open-test-build) — version code bump + Continue after Room migration.

**Automation (BOT):** `verify_play_update_version_bump.sh`; migration + `SaveLoadAfterUpdateFixtureTest`.

## MEGA-QA-137 — Signed AAB / Play App Signing

`verify_play_aab_signing.sh` after `bundleRelease` — `bundletool validate`, `jarsigner`, `apksigner` on universal APK.

## MEGA-QA-146 — Open test 30-minute session

Rostered tester Gmail → Play install → 30 min script in [`PLAY_OPEN_TESTING_SMOKE.md`](PLAY_OPEN_TESTING_SMOKE.md#mega-qa-146--open-test-30-minute-session).
