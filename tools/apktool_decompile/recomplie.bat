@echo on

set CMD_PATH=
for %%P in (%0) do set CMD_PATH=%%~dpP
cd /d "%CMD_PATH%"

java -jar tools/apktool.jar b -f %1 -o unsign.apk

@echo --------------start sign apk....--------------

tools\zipalign.exe -f 4 unsign.apk zipalign.apk

java -jar tools\apksigner.jar sign --v1-signing-enabled true --v2-signing-enabled true --v3-signing-enabled true --v4-signing-enabled false --ks tools\keystore_debug.jks --ks-key-alias young_debug --ks-pass pass:123abc --key-pass pass:123abc --out signed_output.apk zipalign.apk

@echo --------------compile apk successful--------------
@echo --------------------------------------------------------

del unsign.apk
del zipalign.apk

pause
