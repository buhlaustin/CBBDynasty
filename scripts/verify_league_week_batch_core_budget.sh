#!/usr/bin/env bash
# MEGA-QA-015 — 365-team RS week median < 12 s (JVM CI budget).
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT/android"
./gradlew :core:test --tests 'dev.novux.dynasty.cbb.core.league.LeagueWeekBatchPerformanceTest' --no-daemon -q
echo "LeagueWeekBatchPerformanceTest PASS (MEGA-QA-015)"
