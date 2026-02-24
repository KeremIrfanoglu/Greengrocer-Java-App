@echo off
echo ============================================
echo   QuickCommerce System - Starting...
echo ============================================
echo.

REM Check if Java is available
java -version >nul 2>&1
if errorlevel 1 (
    echo ERROR: Java is not installed or not on PATH!
    echo Please install JDK 21 from: https://adoptium.net
    echo.
    pause
    exit /b 1
)

echo Building and running the application...
echo.

call mvnw.cmd javafx:run
if errorlevel 1 (
    echo.
    echo ERROR: Application failed to start.
    echo Make sure you have JDK 21 installed.
    echo.
    pause
)
