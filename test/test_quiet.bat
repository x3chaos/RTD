@echo off
cd ..
python run_test.py -quiet
cd server
call launch.bat
pause