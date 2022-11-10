# Windows装Linux子系统

**1、ubuntu系统安装 ：**

    1、Microsoft Store 搜索：ubuntu 下载安装
    2、Microsoft Store 搜索：Windows Terminal 下载安装
    3、控制面板 -- 程序和功能 -- 启动或关闭Windows功能 -- 勾选：适用于Linux的Windows子系统
    4、右键选择以管理员身份运行cmd命令行：wsl --set-default-version 1
    5、先点击Ubuntu图标打开，让其自动执行Installing
    6、点击第2步下载的 Windows Terminal 终端 ，点击左上方下拉箭头选择：Ubuntu 。此时所有安装步骤完成。

    说明：
    1、修改配色方案：Ubuntu -- 外观 -- 配色方案：Tango Dark
    2、路径是这种：/mnt/e/AndroidCode/360_android_mobile_game_sdk

**2、Windows的子Linux系统安装Python2.7 ：**

    1、sudo add-apt-repository universe
    2、sudo apt update
    3、sudo apt install python2.7
    4、curl https://bootstrap.pypa.io/pip/2.7/get-pip.py --output get-pip.py
    5、sudo python2.7 get-pip.py
    6、pip install Pillow
    7、python2.7 --version

**3、Windows的子Linux系统搜索并安装 Java JDK ：**

	1、sudo apt-cache search jdk
	2、sudo apt-get install openjdk-11-jdk-headless

**4、Windows的子Linux系统设置字符编码 ：**

    1、sudo localedef -i en_US -f UTF-8 en_US.UTF-8
    2、sudo localedef -i en_US -f UTF-8 en_US.utf-8
    3、sudo localedef -i zh_CN -f UTF-8 zh_CN.UTF-8
    4、sudo localedef -i zh_CN -f UTF-8 zh_CN.utf-8
    5、source ~/.bashrc

**5、Windows的子Linux系统安装把windows文件的字符编码设置为Linux的字符编码 ：**

    sudo apt install dos2unix
    使用说明：dos2unix /mnt/e/360portalgamesdk/portal-sdk-tool/tool/linux/d8 
