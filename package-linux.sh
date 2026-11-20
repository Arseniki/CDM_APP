#!/bin/bash
set -euo pipefail

ROOT_DIR="$(cd -- "$(dirname -- "$0")" && pwd)"
APP_NAME="CoupeDuMonde"
DIST_DIR="$ROOT_DIR/dist"
APP_IMAGE="$DIST_DIR/$APP_NAME"
ARCHIVE="$DIST_DIR/$APP_NAME-linux-x86_64.zip"
STAGING_DIR="$(mktemp -d)"

cleanup() {
    rm -rf "$STAGING_DIR"
}
trap cleanup EXIT

cd "$ROOT_DIR"
./build.sh

jar --create --file "$STAGING_DIR/$APP_NAME.jar" -C "$ROOT_DIR/out" .
cp "$ROOT_DIR/lib/mysql-connector-j-26.10.0.jar" "$STAGING_DIR/"

mkdir -p "$DIST_DIR"
if [ -e "$APP_IMAGE" ]; then
    rm -rf "$APP_IMAGE"
fi

jpackage \
    --type app-image \
    --name "$APP_NAME" \
    --app-version 1.0.0 \
    --vendor "CDM_APP" \
    --description "Gestion de la Coupe du Monde 2022" \
    --input "$STAGING_DIR" \
    --main-jar "$APP_NAME.jar" \
    --main-class ui.MainFrame \
    --dest "$DIST_DIR" \
    --java-options "-Dfile.encoding=UTF-8"

chmod +x "$APP_IMAGE/bin/$APP_NAME"
if [ -e "$ARCHIVE" ]; then
    rm -f "$ARCHIVE"
fi
(cd "$DIST_DIR" && zip -qr "$ARCHIVE" "$APP_NAME")

printf 'Lanceur exécutable : %s\n' "$APP_IMAGE/bin/$APP_NAME"
printf 'Archive transférable : %s\n' "$ARCHIVE"
