#!/usr/bin/env bash
# kotlinx.serialization release smoke (MEGA-QA-152).
set -euo pipefail

repo_root="$(cd "$(dirname "$0")/.." && pwd)"
cd "$repo_root/android"

./gradlew :core:test --tests dev.novux.dynasty.cbb.core.qa.ReleaseSerializationR8SmokeTest --no-daemon -q

mapping="$repo_root/android/app/build/outputs/mapping/release/mapping.txt"
if [[ -f "$mapping" ]]; then
  if ! grep -q 'ProvisionalGameBoxArchive' "$mapping"; then
    echo "mapping.txt missing ProvisionalGameBoxArchive — R8 may have stripped serializers" >&2
    exit 1
  fi
  echo "R8 mapping retains ProvisionalGameBoxArchive"
fi

rules="$repo_root/android/app/proguard-rules.pro"
grep -q 'kotlinx.serialization.KSerializer' "$rules" || {
  echo "proguard-rules.pro missing kotlinx.serialization keep rules" >&2
  exit 1
}

echo "Release serialization smoke OK (MEGA-QA-152)"
