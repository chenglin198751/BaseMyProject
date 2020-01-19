@echo off

set CMD_PATH=
for %%P in (%0) do set CMD_PATH=%%~dpP
cd /d "%CMD_PATH%"

java -jar VasDolly.jar put -c channel.txt %1 apk_channels


pause
