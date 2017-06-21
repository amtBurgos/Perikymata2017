@echo off
echo Custom python environment will be installed
echo Operations may take a while
echo Previous installation will be removed
conda info --env | findstr "perikymata"
if %ERRORLEVEL% == 0 (
	echo Removing previous environment installation...
	conda remove --yes --name perikymata --all
	echo Previous installation removed
)
echo Installing new environment...
conda env create -f perikymata.yml
@echo Installation completed, press any key to close...
pause>null