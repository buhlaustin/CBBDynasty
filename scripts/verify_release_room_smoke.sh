#!/usr/bin/env bash
# Room entity / DAO release smoke (MEGA-QA-153).
set -euo pipefail

repo_root="$(cd "$(dirname "$0")/.." && pwd)"
rules="$repo_root/android/app/proguard-rules.pro"

grep -q '@androidx.room.Entity class \*' "$rules" || {
  echo "proguard-rules.pro missing Room @Entity keep rule" >&2
  exit 1
}
grep -q 'extends androidx.room.RoomDatabase' "$rules" || {
  echo "proguard-rules.pro missing RoomDatabase keep rule" >&2
  exit 1
}

cd "$repo_root/android"
./gradlew :database:testDebugUnitTest \
  --tests dev.novux.dynasty.cbb.database.qa.ReleaseRoomR8SmokeTest \
  --no-daemon -q

mapping="$repo_root/android/app/build/outputs/mapping/release/mapping.txt"
if [[ -f "$mapping" ]]; then
  if ! grep -q 'DynastyDatabase' "$mapping"; then
    echo "mapping.txt missing DynastyDatabase reference" >&2
    exit 1
  fi
  echo "R8 mapping retains DynastyDatabase"
fi

echo "Release Room smoke OK (MEGA-QA-153)"
