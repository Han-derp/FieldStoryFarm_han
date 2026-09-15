@echo off
setlocal
set "DIR=src\main\java\com\fieldstory\farm\service\economy\impl"
if not exist "%DIR%" (
  echo [SKIP] %DIR% not found.
  exit /b 0
)
for %%F in ("%DIR%\Basic*.java") do if exist "%%~fF" del /q "%%~fF"
if exist "%DIR%\CropMemoryFactRecorder.java" del /q "%DIR%\CropMemoryFactRecorder.java"
echo [OK] Duplicate Basic service copies removed. EconomyServiceImpl.java was kept.
endlocal
