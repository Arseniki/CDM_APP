#!/bin/bash
rm -rf out
mkdir -p out
javac -cp "lib/*" -d out $(find src -name "*.java")
if [ $? -eq 0 ]; then
    echo "Compilation OK"
else
    echo "Compilation echouee"
fi