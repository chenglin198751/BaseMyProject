@echo off
setlocal

set DEVELOP_DIR=D:\AndroidCode\develop_xmkw
set MASTER_DIR=D:\AndroidCode\master_xmkw

:: 主页列表
:: set PLUGIN_JAR=com.qihoo.plugin.modulation.jar
:: set PLUGIN_LIBRARY=modulation

:: 软件详情页
set PLUGIN_JAR=recommend.jar
set PLUGIN_LIBRARY=app_info


:: 1.编译插件
echo 1.execute plugin gradlew...
cd /d %DEVELOP_DIR%
call gradlew :plugins:%PLUGIN_LIBRARY%:app:assembleDebug

:: 2.复制到主程并重命名为jar
echo 2.copy apk and rename to jar...
copy /Y "%DEVELOP_DIR%\plugins\%PLUGIN_LIBRARY%\app\build\outputs\apk\debug\app-debug.apk" "%MASTER_DIR%\app\src\main\assets\plugins\%PLUGIN_JAR%"

:: 3.编译主程
echo 3.execute master gradlew...
cd /d %MASTER_DIR%
call gradlew :app:updateConfFile :app:assembleTarget26GxbDebug

:: 4.安装 APK
echo 4.Installing APK...
adb install -r -t "%MASTER_DIR%\app\build\outputs\apk\target26Gxb\debug\appstore-300101335.apk"

pause
