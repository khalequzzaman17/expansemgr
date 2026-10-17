@echo off
setlocal enabledelayedexpansion

title Building Expense Tracker App...

echo ============================================================
echo           EXPENSE TRACKER - ANDROID BUILD SCRIPT
echo ============================================================
echo.

:: 1. Locate Java (JDK 21 or Android Studio JBR)
if exist "C:\Program Files\Android\Android Studio\jbr\bin\java.exe" (
    set "JAVA_HOME=C:\Program Files\Android\Android Studio\jbr"
) else if exist "C:\Program Files\Java\jdk-21\bin\java.exe" (
    set "JAVA_HOME=C:\Program Files\Java\jdk-21"
) else if not defined JAVA_HOME (
    echo [ERROR] JAVA_HOME is not set and Android Studio JBR was not found.
    echo Please install JDK 21 or set JAVA_HOME.
    goto :FAIL
)

set "PATH=%JAVA_HOME%\bin;%PATH%"
echo [INFO] Using Java from: %JAVA_HOME%
"%JAVA_HOME%\bin\java.exe" -version
echo.

:: 2. Determine Build Task
set "BUILD_TYPE=%~1"
if not "%BUILD_TYPE%"=="" goto :CHECK_TARGET

:CHOOSE_MENU
echo ============================================================
echo                   SELECT BUILD OPTION
echo ============================================================
echo   [1] Debug APK             - assembleDebug [Default]
echo   [2] Release APK           - assembleRelease
echo   [3] Android App Bundle    - bundleRelease
echo   [4] Clean + Debug APK     - clean assembleDebug
echo   [5] Clean + Release APK   - clean assembleRelease
echo   [6] Clean Only            - clean
echo ============================================================
set "CHOICE="
set /p "CHOICE=Enter choice [1-6] (Press Enter for Debug APK): "
if "%CHOICE%"=="" set "CHOICE=1"
set "BUILD_TYPE=%CHOICE%"

:CHECK_TARGET
if /i "%BUILD_TYPE%"=="1" goto :SET_DEBUG
if /i "%BUILD_TYPE%"=="debug" goto :SET_DEBUG
if /i "%BUILD_TYPE%"=="2" goto :SET_RELEASE
if /i "%BUILD_TYPE%"=="release" goto :SET_RELEASE
if /i "%BUILD_TYPE%"=="3" goto :SET_BUNDLE
if /i "%BUILD_TYPE%"=="bundle" goto :SET_BUNDLE
if /i "%BUILD_TYPE%"=="4" goto :SET_CLEAN_DEBUG
if /i "%BUILD_TYPE%"=="clean_debug" goto :SET_CLEAN_DEBUG
if /i "%BUILD_TYPE%"=="5" goto :SET_CLEAN_RELEASE
if /i "%BUILD_TYPE%"=="clean_release" goto :SET_CLEAN_RELEASE
if /i "%BUILD_TYPE%"=="6" goto :SET_CLEAN_ONLY
if /i "%BUILD_TYPE%"=="clean" goto :SET_CLEAN_ONLY

:: Custom Gradle Task
set "TASK=%BUILD_TYPE%"
echo.
echo [INFO] Custom task: %TASK%
goto :RUN_BUILD

:SET_DEBUG
set "TASK=assembleDebug"
echo.
echo [INFO] Build target: Debug APK (assembleDebug)
goto :RUN_BUILD

:SET_RELEASE
set "TASK=assembleRelease"
echo.
echo [INFO] Build target: Release APK (assembleRelease)
goto :RUN_BUILD

:SET_BUNDLE
set "TASK=bundleRelease"
echo.
echo [INFO] Build target: Android App Bundle (bundleRelease)
goto :RUN_BUILD

:SET_CLEAN_DEBUG
set "TASK=clean assembleDebug"
echo.
echo [INFO] Build target: Clean + Debug APK
goto :RUN_BUILD

:SET_CLEAN_RELEASE
set "TASK=clean assembleRelease"
echo.
echo [INFO] Build target: Clean + Release APK
goto :RUN_BUILD

:SET_CLEAN_ONLY
set "TASK=clean"
echo.
echo [INFO] Build target: Clean project
goto :RUN_BUILD

:RUN_BUILD
echo.
echo ============================================================
echo [START] Running Gradle: gradlew.bat %TASK%
echo ============================================================
echo.

call "%~dp0gradlew.bat" "-Dorg.gradle.java.home=%JAVA_HOME%" %TASK% --stacktrace

if errorlevel 1 goto :FAIL

echo.
echo ============================================================
echo [SUCCESS] BUILD COMPLETED SUCCESSFULLY!
echo ============================================================
echo.

if exist "%~dp0app\build\outputs\apk\debug\app-debug.apk" (
    echo [APK Location]
    echo   %~dp0app\build\outputs\apk\debug\app-debug.apk
    echo.
)
if exist "%~dp0app\build\outputs\apk\release\app-release.apk" (
    echo [Release APK Location]
    echo   %~dp0app\build\outputs\apk\release\app-release.apk
    echo.
)
if exist "%~dp0app\build\outputs\apk\release\app-release-unsigned.apk" (
    echo [Release APK Location]
    echo   %~dp0app\build\outputs\apk\release\app-release-unsigned.apk
    echo.
)
if exist "%~dp0app\build\outputs\bundle\release\app-release.aab" (
    echo [App Bundle Location]
    echo   %~dp0app\build\outputs\bundle\release\app-release.aab
    echo.
)

goto :END

:FAIL
echo.
echo ============================================================
echo [ERROR] BUILD FAILED!
echo Check the logs above for error details.
echo ============================================================
echo.

:END
pause
exit /b %ERRORLEVEL%
