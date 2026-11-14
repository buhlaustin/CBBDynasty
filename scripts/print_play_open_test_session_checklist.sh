#!/usr/bin/env bash
# Prints MEGA-QA-146 open-testing 30-minute session script for rostered testers.
set -euo pipefail

cat <<'EOF'
MEGA-QA-146 — Open test roster 30-minute session
================================================
Prerequisites:
  - Gmail on Play Console → Open testing tester list
  - Install from Play (not sideload) unless doing parity check first

Session (~30 min, no Google sign-in required):

  1. [2 min] Confirm you received the open-testing invite and Install appears in Play Store
  2. [3 min] Install or update com.dynasty.cbb from open testing
  3. [5 min] Cold launch → studio intro (if any) → Tap to Play → home
  4. [10 min] New game (wizard sign) OR Continue an existing dynasty
  5. [5 min] From shell: open Recruiting, Rankings, or News; navigate back without crash
  6. [5 min] Sim Week at least twice (confirm dialogs as shown)

Pass: no force-close, no ANR >10s, session reaches step 6 on device.

Reference: docs/PLAY_OPEN_TESTING_SMOKE.md#mega-qa-146--open-test-30-minute-session
EOF
