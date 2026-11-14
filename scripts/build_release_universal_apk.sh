#!/usr/bin/env bash
# Build release AAB and extract a universal APK for device sideload (MEGA-QA-134).
set -euo pipefail

repo_root="$(cd "$(dirname "$0")/.." && pwd)"
android_dir="$repo_root/android"
out_dir="${RELEASE_SIDELOAD_OUT:-$android_dir/app/build/outputs/sideload/release}"
mkdir -p "$out_dir"

bash "$repo_root/scripts/install_android_sdk.sh" >/dev/null

cd "$android_dir"
if [[ -z "${ANDROID_KEYSTORE_PATH:-}" ]]; then
  echo "No ANDROID_KEYSTORE_PATH — using Gradle debug signing fallback for release smoke"
fi
./gradlew :app:bundleRelease --no-daemon -q

aab="$(ls -1 "$android_dir/app/build/outputs/bundle/release/"*.aab | head -1)"
bundletool_jar="$(bash "$repo_root/scripts/install_bundletool.sh")"
apks="$out_dir/universal.apks"
apk="$out_dir/universal.apk"

build_apks_args=(
  --bundle="$aab"
  --output="$apks"
  --mode=universal
  --overwrite
)

ks_path="${ANDROID_KEYSTORE_PATH:-}"
if [[ -n "$ks_path" && -f "$ks_path" ]]; then
  build_apks_args+=(
    --ks="$ks_path"
    --ks-pass="pass:${ANDROID_KEYSTORE_PASSWORD:?}"
    --ks-key-alias="${ANDROID_KEY_ALIAS:?}"
  )
  if [[ -n "${ANDROID_KEY_PASSWORD:-}" ]]; then
    build_apks_args+=(--key-pass="pass:$ANDROID_KEY_PASSWORD")
  fi
else
  build_apks_args+=(--ks="$HOME/.android/debug.keystore" --ks-pass=pass:android --ks-key-alias=androiddebugkey)
fi

java -jar "$bundletool_jar" build-apks "${build_apks_args[@]}"
unzip -p "$apks" universal.apk >"$apk"

sdk_root="${ANDROID_HOME:-$HOME/Android/Sdk}"
aapt="$(ls -1 "$sdk_root/build-tools"/*/aapt 2>/dev/null | sort -V | tail -1)"
if [[ -x "$aapt" ]]; then
  "$aapt" dump badging "$apk" | head -5
fi

echo "Universal release APK: $apk"
echo "Source AAB: $aab"
