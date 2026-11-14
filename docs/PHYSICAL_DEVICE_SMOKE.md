# Physical device smoke (MEGA-QA-127)

Product gate before Play promote: **mid-tier Android 13+**, about **6 GB RAM**, full career smoke on a **retail device** (not only emulator).

Reference constants: `PhysicalDeviceSmokePath` in `:core`.

## Target device profile

| Attribute | Requirement |
|-----------|-------------|
| OS | Android **13+** (API **33+**) |
| RAM | **≥ 6 GB** |
| Build | `:app:assembleDebug` or internal testing build |
| Network | Optional; offline-capable path must work after identity pack is present |

## Manual smoke script

| # | Action | Pass |
|---|--------|------|
| 1 | Install debug (or internal) APK | Install succeeds |
| 2 | **New game** → wizard → **Sign** | Offseason/shell loads; slot saved |
| 3 | Advance through signing/preseason → **Regular Season week 1** | In-season shell; header matches RS |
| 4 | **Sim week** × **4** (confirm dialogs as shown) | No ANR/crash; week index increases by 4 |
| 4b | On **RS week ≥ 5**, one sim week — check logcat `SimWeekAdvancePerf` | Perceived advance **≤ 2.5 s** ([MEGA-QA-128]) |
| 5 | Background app 30s, return | UI phase/week still correct |
| 6 | Force-stop → relaunch → **Continue** same slot | Loads; RS week ≥ 5; roster intact |

Record device model, Android version, and APK `versionName` in the PR or release notes when executing this gate.

## Automation (BOT / JVM)

CI cannot replace a physical device for this row, but regression coverage mirrors the **four RS sim weeks** compute path:

- `FourRegularSeasonWeeksDeviceSmokeTest` — checkpoint + `LeagueWeekBatchSimEngine` for four consecutive RS weeks.

Emulator baseline remains **MEGA-QA-119** ([`PLAY_PRE_LAUNCH_MATRIX.md`](PLAY_PRE_LAUNCH_MATRIX.md)).
