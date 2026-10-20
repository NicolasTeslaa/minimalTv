#!/bin/zsh
set -euo pipefail

SCRIPT_DIR="${0:A:h}"
PROJECT_DIR="${SCRIPT_DIR:h}"
cd "$PROJECT_DIR"

swift build --product MinimalVoiceApp
BIN_DIR="$(swift build --show-bin-path)"
APP_DIR="$BIN_DIR/Minimal Voice.app"

rm -rf "$APP_DIR"
mkdir -p "$APP_DIR/Contents/MacOS" "$APP_DIR/Contents/Resources"
cp "$BIN_DIR/MinimalVoiceApp" "$APP_DIR/Contents/MacOS/MinimalVoiceApp"
cp "$PROJECT_DIR/Resources/Info.plist" "$APP_DIR/Contents/Info.plist"
cp "$PROJECT_DIR/Resources/MinimalVoice.icns" "$APP_DIR/Contents/Resources/MinimalVoice.icns"

open "$APP_DIR"
echo "Minimal Voice iniciado: $APP_DIR"
