echo Creating program
pyinstaller --additional-hooks-dir=. --clean --log-level INFO --onefile ServerSocket.py
pause
exit