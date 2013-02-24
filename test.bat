@echo off

set serverloc="./server/"
set craftbukkit="craftbukkit.jar"

:BUILD
echo Initializing test server...
python test.py
if %errorlevel%==0 goto SUCCESS else goto FAILURE

:FAILURE
echo.
pause
goto :eof

:SUCCESS
echo.
echo Done.
echo.
echo Starting test server...
echo.
cd %serverloc%
./run.bat %craftbukkit%
if %errorlevel%==0 goto :eof else goto FAILURE