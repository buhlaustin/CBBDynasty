#!/usr/bin/env bash
# Sync closed art into android/res per planning/branding.md (adaptive icon).
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
python3 "$ROOT/planning/tools/generate_adaptive_icon.py"
