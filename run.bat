@echo off
setlocal enabledelayedexpansion

echo ============================================
echo   Greengrocer Java App - Starting...
echo ============================================
echo.

:: 1. Adim: PATH uzerinde dogru Java surumu var mi?
java -version 2>&1 | findstr "21." >nul
if %errorlevel% equ 0 (
    echo [OK] Java 21 found on PATH.
    goto :run
)

:: 2. Adim: JAVA_HOME zaten tanimli ve dogru mu?
if defined JAVA_HOME (
    if exist "%JAVA_HOME%\bin\java.exe" (
        "%JAVA_HOME%\bin\java" -version 2>&1 | findstr "21." >nul
        if !errorlevel! equ 0 (
            echo [OK] Using JAVA_HOME: %JAVA_HOME%
            goto :run
        )
    )
)

:: 3. Adim: Bilgisayardaki yaygin JDK 21 dizinlerini otomatik tara
echo [INFO] Searching for JDK 21 in common directories...
set "FOUND_JAVA="

for %%D in (
    "%ProgramFiles%\Java\jdk-21*"
    "%ProgramFiles%\Eclipse Adoptium\jdk-21*"
    "%ProgramFiles%\Microsoft\jdk-21*"
    "%ProgramFiles%\Amazon Corretto\jdk-21*"
    "%LocalAppData%\Programs\Eclipse Adoptium\jdk-21*"
    "%UserProfile%\.jdks\jdk-21*"
    "%UserProfile%\.jdks\temurin-21*"
) do (
    if exist "%%~D\bin\java.exe" (
        set "FOUND_JAVA=%%~D"
        goto :found
    )
)

:found
if defined FOUND_JAVA (
    echo [OK] Found JDK 21 at: %FOUND_JAVA%
    set "JAVA_HOME=%FOUND_JAVA%"
    set "PATH=%FOUND_JAVA%\bin;%PATH%"
    goto :run
)

:: 4. Adim: Hicbir sey bulunamazsa hata goster ve indirme linki ver
echo.
echo ============================================
echo   ERROR: JDK 21 NOT FOUND!
echo ============================================
echo.
echo   Bu programin calismasi icin JDK 21 gereklidir.
echo   Lutfen sunu indirin: https://adoptium.net
echo.
pause
exit /b 1

:run
echo.
echo Building and running the application...
echo.

:: mvnw.cmd'yi calistirirken JAVA_HOME'un kesinlikle tanimli oldugundan emin oluyoruz
call mvnw.cmd javafx:run
if %errorlevel% neq 0 (
    echo.
    echo ============================================
    echo   ERROR: Application failed to start.
    echo ============================================
    echo.
    echo   Olası sebepler:
    echo   - Internet baglantisi (Ilk calistirmada Maven dosya indirir)
    echo   - Veritabani baglantisi (db.properties ayarlarini kontrol edin)
    echo.
    pause
)