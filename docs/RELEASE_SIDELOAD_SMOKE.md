# Release sideload smoke (MEGA-QA-134)

Validate **release** bits (R8/minify) on a device by sideloading a **universal APK** extracted from `:app:bundleRelease` — same user path as debug smoke ([`PLAY_PRE_LAUNCH_MATRIX.md`](PLAY_PRE_LAUNCH_MATRIX.md)).

## Build universal APK

```bash
bash scripts/install_android_sdk.sh
# Optional Play-equivalent signing:
# export ANDROID_KEYSTORE_PATH=... ANDROID_KEYSTORE_PASSWORD=... ANDROID_KEY_ALIAS=...
bash scripts/build_release_universal_apk.sh
```

Output: `android/app/build/outputs/sideload/release/universal.apk` (from the release AAB).

Without upload-keystore env vars, Gradle and bundletool use the **debug keystore** so CI and local agents can still produce an installable release binary for smoke (not for Play upload).

## Device parity script

| Step | Action | Pass |
|------|--------|------|
| 1 | `adb install -r universal.apk` | Install succeeds |
| 2 | Repeat **MEGA-QA-119** steps 2–5 on **release** build | Same UX as debug; no R8 crash |
| 3 | Optional: RS week 5 sim week | Perceived ≤ 2.5 s ([MEGA-QA-128]); logcat `SimWeekAdvancePerf` only in **debug** — use Profiler on release |

Constants: `ReleaseSideloadSmokePath` in `:core`.

## Automation (BOT)

- Workflow [`.github/workflows/android-release-universal-smoke.yml`](../.github/workflows/android-release-universal-smoke.yml) — `bundleRelease`, universal APK, `aapt` badging check.
- `ReleaseSideloadSmokePathTest` — application id + parity checklist.
