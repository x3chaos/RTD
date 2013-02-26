@echo off
cd ..
python run_test.py -keepconfig
cd server
call launch.bat
pause