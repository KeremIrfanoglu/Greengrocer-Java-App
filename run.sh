#!/bin/bash
echo "============================================"
echo "  Greengrocer Java App - Starting..."
echo "============================================"
echo

# ============================================
#  Auto-detect JDK 21 (no JAVA_HOME required)
# ============================================

find_jdk() {
    
    if [ -n "$JAVA_HOME" ] && [ -x "$JAVA_HOME/bin/java" ]; then
        echo "[OK] Using JAVA_HOME: $JAVA_HOME"
        return 0
    fi

    
    if command -v java &> /dev/null; then
        echo "[OK] Found Java on PATH"
        return 0
    fi

    
    echo "[INFO] JAVA_HOME not set and Java not on PATH. Searching for JDK..."
    echo

    SEARCH_DIRS=()

    #macOS paths
    if [ -d "/Library/Java/JavaVirtualMachines" ]; then
        for d in /Library/Java/JavaVirtualMachines/temurin-21*/Contents/Home \
                 /Library/Java/JavaVirtualMachines/jdk-21*/Contents/Home \
                 /Library/Java/JavaVirtualMachines/amazon-corretto-21*/Contents/Home \
                 /Library/Java/JavaVirtualMachines/zulu-21*/Contents/Home \
                 /Library/Java/JavaVirtualMachines/liberica-jdk-21*/Contents/Home; do
            [ -d "$d" ] && SEARCH_DIRS+=("$d")
        done
    fi

    
    for d in /usr/lib/jvm/java-21-openjdk* \
             /usr/lib/jvm/jdk-21* \
             /usr/lib/jvm/temurin-21* \
             /usr/lib/jvm/java-21-amazon-corretto* \
             /usr/lib/jvm/zulu-21* \
             /usr/java/jdk-21*; do
        [ -d "$d" ] && SEARCH_DIRS+=("$d")
    done

    
    for d in "$HOME/.sdkman/candidates/java/"*21* \
             "$HOME/.jdks/jdk-21"* \
             "$HOME/.jdks/temurin-21"* \
             "$HOME/.jdks/corretto-21"*; do
        [ -d "$d" ] && SEARCH_DIRS+=("$d")
    done

    
    if [ -d "/opt/homebrew/opt/openjdk@21" ]; then
        SEARCH_DIRS+=("/opt/homebrew/opt/openjdk@21")
    fi
    if [ -d "/usr/local/opt/openjdk@21" ]; then
        SEARCH_DIRS+=("/usr/local/opt/openjdk@21")
    fi

    
    for jdk_dir in "${SEARCH_DIRS[@]}"; do
        if [ -x "$jdk_dir/bin/java" ]; then
            echo "[OK] Found JDK at: $jdk_dir"
            export JAVA_HOME="$jdk_dir"
            export PATH="$jdk_dir/bin:$PATH"
            return 0
        fi
    done

    return 1
}

if ! find_jdk; then
    echo
    echo "============================================"
    echo "  ERROR: JDK 21 not found!"
    echo "============================================"
    echo
    echo "  Please install JDK 21:"
    echo
    echo "  macOS:  brew install openjdk@21"
    echo "  Ubuntu: sudo apt install openjdk-21-jdk"
    echo "  Fedora: sudo dnf install java-21-openjdk-devel"
    echo
    echo "  Or download from: https://adoptium.net"
    echo
    echo "  After installing, run this script again."
    echo "  (No environment variable setup needed!)"
    echo
    exit 1
fi

echo
echo "Building and running the application..."
echo

chmod +x mvnw 2>/dev/null
./mvnw javafx:run
