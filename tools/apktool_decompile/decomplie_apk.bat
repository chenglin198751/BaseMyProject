@echo on

set CMD_PATH=
for %%P in (%0) do set CMD_PATH=%%~dpP
cd /d "%CMD_PATH%"

java -jar tools/apktool.jar -v d -b -f %1 

pause
