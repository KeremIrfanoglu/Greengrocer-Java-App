@echo off
echo ============================================
echo   QuickCommerce System - Starting...
echo ============================================
echo.

REM ============================================
REM  Auto-detect JDK 21 (no JAVA_HOME required)
REM ============================================

REM 1. If JAVA_HOME is already set and valid, use it
if defined JAVA_HOME (
    if exist "%JAVA_HOME%\bin\java.exe" (
        echo [OK] Using JAVA_HOME: %JAVA_HOME%
        goto :run
    )
)

REM 2. Check if java is on PATH and is version 21+
where java >nul 2>&1
if %errorlevel%==0 (
    for /f "tokens=3" %%v in ('java -version 2^>^&1 ^| findstr /i "version"') do (
        set "JAVA_VER=%%~v"
    )
    echo [OK] Found Java on PATH: %JAVA_VER%
    goto :run
)

REM 3. Auto-search common JDK installation directories
echo [INFO] JAVA_HOME not set and Java not on PATH. Searching for JDK...
echo.

set "FOUND_JAVA="

REM Search patterns for common JDK 21 installations
for %%D in (
    "%ProgramFiles%\Java\jdk-21*"
    "%ProgramFiles%\Eclipse Adoptium\jdk-21*"
    "%ProgramFiles%\Microsoft\jdk-21*"
    "%ProgramFiles%\Amazon Corretto\jdk21*"
    "%ProgramFiles%\BellSoft\LibericaJDK-21*"
    "%ProgramFiles%\Zulu\zulu-21*"
    "%ProgramFiles%\SapMachine\jdk-21*"
    "%ProgramFiles%\OpenJDK\jdk-21*"
    "%ProgramFiles(x86)%\Java\jdk-21*"
    "%LocalAppData%\Programs\Eclipse Adoptium\jdk-21*"
    "%UserProfile%\.jdks\jdk-21*"
    "%UserProfile%\.jdks\corretto-21*"
    "%UserProfile%\.jdks\temurin-21*"
) do (
    if exist %%D (
        for /d %%F in (%%D) do (
            if exist "%%F\bin\java.exe" (
                set "FOUND_JAVA=%%F"
            )
        )
    )
)

if defined FOUND_JAVA (
    echo [OK] Found JDK at: %FOUND_JAVA%
    set "JAVA_HOME=%FOUND_JAVA%"
    set "PATH=%FOUND_JAVA%\bin;%PATH%"
    goto :run
)

REM 4. Nothing found - show error
echo.
echo ============================================
echo   ERROR: JDK 21 not found!
echo ============================================
echo.
echo   Please install JDK 21 from one of these:
echo.
echo   - Eclipse Temurin (recommended):
echo     https://adoptium.net
echo.
echo   - Oracle JDK:
echo     https://www.oracle.com/java/technologies/downloads/
echo.
echo   - Amazon Corretto:
echo     https://aws.amazon.com/corretto/
echo.
echo   After installing, run this script again.
echo   (No environment variable setup needed!)
echo.
pause
exit /b 1

:run
echo.
echo Building and running the application...
echo.

call mvnw.cmd javafx:run
if errorlevel 1 (
    echo.
    echo ============================================
    echo   ERROR: Application failed to start.
    echo ============================================
    echo.
    echo   Possible causes:
    echo   - JDK version is not 21 or higher
    echo   - Database is not running (check db.properties)
    echo   - Missing internet connection (first run needs Maven downloads)
    echo.
    pause
)
