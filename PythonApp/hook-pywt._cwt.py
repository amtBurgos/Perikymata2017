from PyInstaller.utils.hooks import collect_data_files, collect_submodules
# 'pywt._extensions._cwt'
datas = collect_data_files("pywt._cwt")
hiddenimports = collect_submodules('pywt._cwt')