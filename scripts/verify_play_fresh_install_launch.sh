#!/usr/bin/env bash
# Verifies release APK/AAB artifact is Play-install launchable (MEGA-QA-135 BOT gate).
set -euo pipefail

repo_root="$(cd "$(dirname "$0")/.." && pwd)"
apk="${1:-$repo_root/android/app/build/outputs/sideload/release/universal.apk}"
expected_pkg="com.dynasty.cbb"
expected_activity="dev.novux.dynasty.cbb.MainActivity"

if [[ ! -f "$apk" ]]; then
  echo "APK not found: $apk (run scripts/build_release_universal_apk.sh first)" >&2
  exit 1
fi

bash "$repo_root/scripts/install_android_sdk.sh" >/dev/null
sdk_root="${ANDROID_HOME:-$HOME/Android/Sdk}"
aapt="$(ls -1 "$sdk_root/build-tools"/*/aapt 2>/dev/null | sort -V | tail -1)"
if [[ ! -x "$aapt" ]]; then
  echo "aapt not found under $sdk_root/build-tools" >&2
  exit 1
fi

badging="$("$aapt" dump badging "$apk")"
echo "$badging" | head -8

echo "$badging" | grep -q "package: name='$expected_pkg'" || {
  echo "Expected package $expected_pkg" >&2
  exit 1
}

echo "$badging" | grep -q "launchable-activity: name='$expected_activity'" || {
  echo "Expected launchable activity $expected_activity" >&2
  exit 1
}

echo "Play fresh-install launch readiness OK ($expected_pkg → $expected_activity)"
