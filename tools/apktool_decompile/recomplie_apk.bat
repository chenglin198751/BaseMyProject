@echo on

set CMD_PATH=
for %%P in (%0) do set CMD_PATH=%%~dpP
cd /d "%CMD_PATH%"

java -jar tools/apktool.jar -v b -f %1 -o unsign.apk

@echo --------------start sign apk....--------------

java -jar tools/apksigner.jar sign --v1-signing-enabled true --v2-signing-enabled true --v3-signing-enabled false --v4-signing-enabled false --ks tools/tianming.jks --ks-key-alias tianming_alias --ks-pass pass:360tianming --key-pass pass:tianming360 --out signed_output.apk unsign.apk

@echo --------------compile apk successful--------------
@echo --------------------------------------------------------

del unsign.apk

pause
