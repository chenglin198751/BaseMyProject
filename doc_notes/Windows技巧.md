# Windows技巧

**1、windows11右键菜单显示全部：**

    reg add "HKCU\Software\Classes\CLSID\{86ca1aa0-34aa-4e8b-a509-50c905bae2a2}\InprocServer32" /f /ve
    taskkill /f /im explorer.exe & start explorer.exe

**2、cmd命令：**

    1、windows：
    删除文件夹：rmdir /s /q D:\eclipse-workspace
    复制文件夹：
    robocopy D:\eclipse-workspace E:\eclipse-workspace /E
    xcopy D:\eclipse-workspace E:\eclipse-workspace /E /Y
    
    2、linux：
    删除文件夹：rm -rf D:\eclipse-workspace
    复制文件夹：cp -rf D:\eclipse-workspace E:\eclipse-workspace /E /Y

**3、删除搜索框热门搜索：**

	以管理员模式运行cmd，执行以下命令，重启生效：
	reg add HKCU\Software\Policies\Microsoft\Windows\explorer /v DisableSearchBoxSuggestions /t reg_dword /d 1 /f