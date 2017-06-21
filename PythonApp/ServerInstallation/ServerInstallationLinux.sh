#!/bin/bash
clear
echo "Updating apt-get"
sudo apt-get update
echo "Installing pip for Python 3"
sudo apt-get -y install python3-pip
echo "Installing numpy"
pip3 install -U numpy
echo "Installing freetype"
pip3 install -U freetype-py
echo "Installing pypng"
pip3 install -U pypng
echo "Installing matplotlib"
sudo apt-get install python-matplotlib
echo "Installing Scikit-Image"
pip3 install -U scikit-image==0.13.0
echo "Installation completed!"
