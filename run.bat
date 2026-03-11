@echo off
setlocal enabledelayedexpansion

echo ============================================
echo   Greengrocer Java App - Starting...
echo ============================================
echo.


for /f "tokens=3" %%g in ('java -version 2^>^&1 ^| findstr /i "version"') do (
    set "JAVA_VER=%%g"
    set "JAVA_VER=!JAVA_VER:"=!"
    for /f "delims=. tokens=1" %%v in ("!JAVA_VER!") do set "MAJOR_VER=%%v"
)

if defined MAJOR_VER (
    if !MAJOR_VER! geq 21 (
        echo [OK] Java !MAJOR_VER! found on PATH.
        goto :run
    )
)


echo [INFO] Compatible JDK not on PATH. Searching common directories...
set "FOUND_JAVA="


for %%V in (24 23 22 21) do (
    for %%D in (
        "%ProgramFiles%\Java\jdk-%%V*"
        "%ProgramFiles%\Eclipse Adoptium\jdk-%%V*"
        "%ProgramFiles%\Microsoft\jdk-%%V*"
        "%ProgramFiles%\Amazon Corretto\jdk-%%V*"
        "%LocalAppData%\Programs\Eclipse Adoptium\jdk-%%V*"
        "%UserProfile%\.jdks\jdk-%%V*"
    ) do (
        if exist "%%~D\bin\java.exe" (
            set "FOUND_JAVA=%%~D"
            goto :found
        )
    )
)

:found
if defined FOUND_JAVA (
    echo [OK] Found JDK at: %FOUND_JAVA%
    set "JAVA_HOME=%FOUND_JAVA%"
    set "PATH=%FOUND_JAVA%\bin;%PATH%"
    goto :run
)


echo.
echo ============================================
echo   ERROR: COMPATIBLE JDK NOT FOUND!
echo ============================================
echo.
echo   Bu programin calismasi icin en az JDK 21 gereklidir. [cite: 7]
echo   Lutfen bir JDK yukleyin: https://adoptium.net
echo.
pause
exit /b 1

:run
echo Building and running the application... [cite: 8]
echo.


call mvnw.cmd javafx:run
if %errorlevel% neq 0 (
    echo.
    echo ============================================
    echo   ERROR: Application failed to start.
    echo ============================================
    pause
)