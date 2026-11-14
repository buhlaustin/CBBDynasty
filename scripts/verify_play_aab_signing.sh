#!/usr/bin/env bash
# Verify release AAB/APK signing for Play upload (MEGA-QA-137).
set -euo pipefail

repo_root="$(cd "$(dirname "$0")/.." && pwd)"
aab="${1:-$(ls -1 "$repo_root/android/app/build/outputs/bundle/release/"*.aab 2>/dev/null | head -1)}"
apk="${2:-$repo_root/android/app/build/outputs/sideload/release/universal.apk}"

if [[ ! -f "$aab" ]]; then
  echo "AAB not found: $aab" >&2
  exit 1
fi

bash "$repo_root/scripts/install_android_sdk.sh" >/dev/null
bundletool_jar="$(bash "$repo_root/scripts/install_bundletool.sh")"

echo "Validating bundle structure…"
java -jar "$bundletool_jar" validate --bundle="$aab"

echo "Verifying AAB JAR signatures…"
jarsigner_out="$(jarsigner -verify "$aab" 2>&1)" || true
if ! echo "$jarsigner_out" | grep -qi 'jar verified'; then
  echo "$jarsigner_out" >&2
  echo "AAB jarsigner verification failed" >&2
  exit 1
fi
echo "jarsigner: AAB signature OK"

sdk_root="${ANDROID_HOME:-$HOME/Android/Sdk}"
apksigner="$(ls -1 "$sdk_root/build-tools"/*/apksigner 2>/dev/null | sort -V | tail -1)"

if [[ -f "$apk" && -x "$apksigner" ]]; then
  echo "Verifying universal APK with apksigner…"
  "$apksigner" verify --verbose "$apk" >/dev/null
  echo "apksigner: universal APK signature OK"
else
  echo "Skipping apksigner (no universal APK at $apk)"
fi

echo "Play signed AAB integrity OK (MEGA-QA-137)"
