#!/bin/bash
clear
ps -A | grep "python3"
if [ $? -eq 0 ]
then
	pkill python3
	echo "Server stopped."
else
	echo "Process not running."
fi
