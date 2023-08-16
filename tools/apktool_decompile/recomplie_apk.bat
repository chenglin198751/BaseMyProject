@echo on

set CMD_PATH=
for %%P in (%0) do set CMD_PATH=%%~dpP
cd /d "%CMD_PATH%"

java -jar tools/apktool.jar -v b -f %1 -o unsign.apk

@echo --------------start sign apk....--------------

java -jar tools/apksigner.jar sign --ks tools/keystore_debug.jks --ks-key-alias young_debug --ks-pass pass:123abc --key-pass pass:123abc --out signed_output.apk unsign.apk

@echo --------------compile apk successful--------------
@echo --------------------------------------------------------

del unsign.apk
del signed_output.apk.idsig

pause
