@echo off

set defaultloc=%cd%
set serverloc="./server/"
set craftbukkit="craftbukkit.jar"

:BUILD
echo Initializing test server...
python setup_env.py
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
call run.bat %craftbukkit%
if %errorlevel%==0 goto COPY else goto FAILURE

:COPY
cd %defaultloc%
python copy_world.py
goto FAILURE