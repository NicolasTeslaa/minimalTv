#!/bin/zsh
set -euo pipefail

SCRIPT_DIR="${0:A:h}"
PROJECT_DIR="${SCRIPT_DIR:h}"
INSTALL_DIR="$HOME/Applications"
INSTALL_APP="$INSTALL_DIR/Minimal Voice.app"

cd "$PROJECT_DIR"
swift build --product MinimalVoiceApp
BIN_DIR="$(swift build --show-bin-path)"
BUILD_APP="$BIN_DIR/Minimal Voice.app"

mkdir -p "$INSTALL_DIR"
rm -rf "$INSTALL_APP"
ditto "$BUILD_APP" "$INSTALL_APP"

# Mantém o bundle instalável e identificável pelo macOS.
codesign --force --deep --sign - "$INSTALL_APP"
open "$INSTALL_APP"
echo "Minimal Voice instalado e iniciado: $INSTALL_APP"
