@echo off
setlocal

:: 配置变量
set SRC_DIR=D:\AndroidCode\zhushou
set APK_NAME=original.apk
set PLUGIN_JAR=news.jar
set PLUGIN_LIBRART=modulation

set ASSET_PATH=assets/plugins/%PLUGIN_JAR%
set OUT_APK=modified_aligned_signed.apk
set KEYSTORE=tools\keystore_debug.jks
set ALIAS=young_debug
set STOREPASS=123abc
set KEYPASS=123abc

:: 使用gradlew编译插件apk，并把apk重命名为jar
"%SRC_DIR%\gradlew" :plugins:%PLUGIN_LIBRART%:app:assembleRelease --stacktrace --no-daemon
set RELEASE_APK=%SRC_DIR%\plugins\%PLUGIN_LIBRART%\app\build\outputs\apk\release
del "%RELEASE_APK%\%PLUGIN_JAR%" >nul 2>&1
ren "%RELEASE_APK%\*.apk" %PLUGIN_JAR%
copy /Y "%RELEASE_APK%\%PLUGIN_JAR%" .

:: 配置所需工具
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
