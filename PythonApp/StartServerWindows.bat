@echo off
cls
cd | find "PythonApp"
IF %ERRORLEVEL%==0 (
	echo "Starting Server..."
	python src\ServerSocket.py
) ELSE (
	echo "Starting Server..."
	python PythonApp\src\ServerSocket.py
)
echo Press any key to close this window
pause >nul
exit