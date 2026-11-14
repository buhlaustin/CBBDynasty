#!/usr/bin/env bash
# Downloads Google bundletool for universal APK sideload smoke (MEGA-QA-134).
set -euo pipefail

VERSION="${BUNDLETOOL_VERSION:-1.17.2}"
JAR_DIR="${BUNDLETOOL_HOME:-$HOME/.cache/bundletool}"
JAR="$JAR_DIR/bundletool-${VERSION}.jar"
URL="https://github.com/google/bundletool/releases/download/${VERSION}/bundletool-all-${VERSION}.jar"

mkdir -p "$JAR_DIR"
if [[ ! -f "$JAR" ]]; then
  curl -fsSL "$URL" -o "$JAR"
fi
echo "$JAR"
