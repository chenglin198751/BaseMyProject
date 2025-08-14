@echo off
setlocal

:: 配置变量
set SRC_DIR=D:\AndroidCode\develop_xmkw
set APK_NAME=host.apk

:: 主页列表
:: set PLUGIN_JAR=com.qihoo.plugin.modulation.jar
:: set PLUGIN_LIBRARY=modulation

:: 软件详情页
set PLUGIN_JAR=recommend.jar
set PLUGIN_LIBRARY=app_info

set INIT_DIR=%cd%
set ASSET_PATH=assets/plugins/%PLUGIN_JAR%
set OUT_APK=modified_aligned_signed.apk
set KEYSTORE=tools\debug.keystore
set ALIAS=androiddebugkey
set STOREPASS=android
set KEYPASS=android

:: 使用gradlew编译插件apk，并把apk重命名为jar
::echo 1.start build %PLUGIN_JAR%
::cd /d %SRC_DIR%
::call gradlew :plugins:%PLUGIN_LIBRARY%:app:assembleRelease --stacktrace --no-daemon || (echo gradle build failed & exit /b 1)
cd /d %INIT_DIR%
set RELEASE_APK=%SRC_DIR%\plugins\%PLUGIN_LIBRARY%\app\build\outputs\apk\release
del "%RELEASE_APK%\%PLUGIN_JAR%" >nul 2>&1
ren "%RELEASE_APK%\*.apk" %PLUGIN_JAR%
if not exist assets\plugins (
    mkdir assets\plugins
)
copy /Y "%RELEASE_APK%\%PLUGIN_JAR%" assets\plugins\

:: 配置所需工具
set ZIPALIGN=tools\zipalign.exe
set APKSIGNER_JAR=tools\apksigner.jar
set SEVEN_ZIP=tools\7z.exe
set ADB=adb.exe

:: 复制一份APK，避免覆盖原文件
copy /Y %APK_NAME% %OUT_APK%

:: 替换 APK 中的 assets/plugins/.jar 文件
echo 2.Updating %ASSET_PATH% in APK...
%SEVEN_ZIP% a -tzip "%OUT_APK%" "%ASSET_PATH%" -spf2 -ssc

:: 对齐 APK
echo 3.Aligning APK...
%ZIPALIGN% -f 4 %OUT_APK% aligned.apk

:: 签名 APK
echo 4.Signing APK...
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
echo 5.Installing APK...
%ADB% install -r signed.apk

pause
