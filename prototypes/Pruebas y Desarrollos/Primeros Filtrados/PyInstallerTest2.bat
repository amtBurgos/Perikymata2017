echo Creating program
pyinstaller --additional-hooks-dir=. --clean --log-level INFO --onefile PyinstallerTest2.py
pause
exit