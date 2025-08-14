@echo off
setlocal

:: 配置变量
set develop_xmkw=D:\AndroidCode\zhushou
set master_xmkw=D:\AndroidCode\master_xmkw

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
echo 1.start build %PLUGIN_JAR%
cd /d %develop_xmkw%
call gradlew :plugins:%PLUGIN_LIBRARY%:app:assembleRelease --stacktrace --no-daemon || (echo gradle build failed & exit /b 1)
cd /d %INIT_DIR%
set RELEASE_APK=%develop_xmkw%\plugins\%PLUGIN_LIBRARY%\app\build\outputs\apk\release
del "%RELEASE_APK%\%PLUGIN_JAR%" >nul 2>&1
ren "%RELEASE_APK%\*.apk" %PLUGIN_JAR%
copy /Y "%RELEASE_APK%\%PLUGIN_JAR%" "%master_xmkw%\app\src\main\assets\plugins\"

:: 使用gradlew编译master主apk
cd /d %master_xmkw%
call gradlew :app:assembleRelease --stacktrace --no-daemon || (echo gradle build failed & exit /b 1)
cd /d %master_xmkw%

:: 配置所需工具
set ADB=adb.exe

:: 安装 APK
echo 5.Installing APK...
%ADB% install -r -t "%SRC_DIR%\app\build\outputs\apk\target26Gxb\debug"

pause
