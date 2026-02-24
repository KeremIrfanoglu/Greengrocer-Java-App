#!/bin/bash
echo "============================================"
echo "  QuickCommerce System - Starting..."
echo "============================================"
echo

# Check if Java is available
if ! command -v java &> /dev/null; then
    echo "ERROR: Java is not installed!"
    echo "Please install JDK 21:"
    echo "  macOS:  brew install openjdk@21"
    echo "  Linux:  sudo apt install openjdk-21-jdk"
    exit 1
fi

echo "Building and running the application..."
echo

chmod +x mvnw 2>/dev/null
./mvnw javafx:run
