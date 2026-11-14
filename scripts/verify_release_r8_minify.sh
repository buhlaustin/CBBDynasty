#!/usr/bin/env bash
# Confirms release build has R8 minify enabled and emits mapping (MEGA-QA-151).
set -euo pipefail

repo_root="$(cd "$(dirname "$0")/.." && pwd)"
gradle_file="$repo_root/android/app/build.gradle.kts"

for flag in "isMinifyEnabled = true" "isShrinkResources = true"; do
  if ! grep -qF "$flag" "$gradle_file"; then
    echo "Missing $flag in $gradle_file" >&2
    exit 1
  fi
done
echo "Gradle release minify flags OK"

bash "$repo_root/scripts/install_android_sdk.sh" >/dev/null
cd "$repo_root/android"
./gradlew :app:bundleRelease --no-daemon -q

mapping="$repo_root/android/app/build/outputs/mapping/release/mapping.txt"
if [[ ! -s "$mapping" ]]; then
  echo "R8 mapping not found at $mapping" >&2
  exit 1
fi
lines=$(wc -l <"$mapping")
echo "R8 mapping.txt present ($lines lines)"

rules="$repo_root/android/app/proguard-rules.pro"
if [[ ! -f "$rules" ]]; then
  echo "Missing $rules" >&2
  exit 1
fi
echo "proguard-rules.pro present"

echo "Release R8 minify gate OK (MEGA-QA-151)"
