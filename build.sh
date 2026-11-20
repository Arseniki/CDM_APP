#!/bin/bash
set -euo pipefail

rm -rf out
mkdir -p out
javac -cp "lib/*" -d out $(find src -name "*.java")
echo "Compilation OK"