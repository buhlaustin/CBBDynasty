#!/usr/bin/env bash
# API 34 x86_64 emulator packages for MEGA-QA-119 smoke CI.
set -euo pipefail

SDK_ROOT="${ANDROID_HOME:-${ANDROID_SDK_ROOT:-$HOME/Android/Sdk}}"
export ANDROID_HOME="$SDK_ROOT"
export ANDROID_SDK_ROOT="$SDK_ROOT"
export PATH="$SDK_ROOT/cmdline-tools/latest/bin:$SDK_ROOT/platform-tools:$SDK_ROOT/emulator:$PATH"

if [[ ! -x "$SDK_ROOT/cmdline-tools/latest/bin/sdkmanager" ]]; then
  echo "Run scripts/install_android_sdk.sh first" >&2
  exit 1
fi

for _ in $(seq 1 100); do echo y; done | sdkmanager --licenses >/dev/null || true
sdkmanager --install \
  "emulator" \
  "platforms;android-34" \
  "system-images;android-34;google_apis;x86_64"

echo "API 34 emulator system image ready"
