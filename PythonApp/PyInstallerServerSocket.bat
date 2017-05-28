echo Creating program
pyinstaller --additional-hooks-dir=. --hiddenimport pywt --clean --log-level INFO --onefile ServerSocket.py
pause
exit