@echo off
setlocal

:: 配置变量
set APK_NAME=original.apk
set PLUGIN_JAR=news.jar
set ASSET_PATH=assets/plugins/%PLUGIN_JAR%
set OUT_APK=modified_aligned_signed.apk
set KEYSTORE=tools\keystore_debug.jks
set ALIAS=young_debug
set STOREPASS=123abc
set KEYPASS=123abc

:: 路径工具
set ZIPALIGN=tools\zipalign.exe
set APKSIGNER_JAR=tools\apksigner.jar
set SEVEN_ZIP=tools\7z.exe
set ADB=adb.exe

:: 拷贝原 APK 到新文件，避免直接覆盖
copy /Y %APK_NAME% %OUT_APK%

:: 替换 APK 中的 assets/plugins/news.jar 文件
echo 1.Updating %ASSET_PATH% in APK...
%SEVEN_ZIP% u -tzip %OUT_APK% %PLUGIN_JAR% -spf2 -ssc

:: 对齐 APK
echo 2.Aligning APK...
%ZIPALIGN% -f 4 %OUT_APK% aligned.apk

:: 签名 APK
echo 3.Signing APK...
java -jar %APKSIGNER_JAR% sign ^
  --v1-signing-enabled true ^
  --v2-signing-enabled true ^
  --ks %KEYSTORE% ^
  --ks-key-alias %ALIAS% ^
  --ks-pass pass:%STOREPASS% ^
  --key-pass pass:%KEYPASS% ^
  --out signed.apk ^
  aligned.apk

del unsign.apk signed.apk.idsig modified_aligned_signed.apk aligned.apk >nul 2>&1

:: 安装 APK
echo 4.Installing APK...
%ADB% install -r signed.apk


pause
